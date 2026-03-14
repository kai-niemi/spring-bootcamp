package io.cockroachdb.bootcamp.caching;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import io.cockroachdb.bootcamp.annotation.TransactionExplicit;
import io.cockroachdb.bootcamp.model.Product;
import io.cockroachdb.bootcamp.model.PurchaseOrder;
import io.cockroachdb.bootcamp.model.ShipmentStatus;
import io.cockroachdb.bootcamp.repository.OrderRepository;
import io.cockroachdb.bootcamp.repository.ProductRepository;
import io.cockroachdb.bootcamp.util.AssertUtils;

@Service
public class OrderService {
    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @TransactionExplicit(readOnly = true)
    @Cacheable(value = "orders", key = "#id")
    public Optional<PurchaseOrder> findOrderById(UUID id) {
        try {
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        AssertUtils.assertReadOnlyTransaction();
        return orderRepository.findOrderDetailsById(id);
    }

    @TransactionExplicit
    @CacheEvict(value = "orders", key = "#id")
    public void deleteOrder(UUID id) {
        orderRepository.deleteById(id);
    }

    @TransactionExplicit
    public PurchaseOrder placeOrder(PurchaseOrder order) {
        AssertUtils.assertReadWriteTransaction();

        try {
            // Update product inventories for each line item
            order.getOrderItems().forEach(orderItem -> {
                UUID productId = Objects.requireNonNull(orderItem.getProduct().getId());
                Product product = productRepository.getReferenceById(productId);
                product.addInventoryQuantity(-orderItem.getQuantity());
            });

            order.setStatus(ShipmentStatus.placed);
            order.setTotalPrice(order.subTotal());

            orderRepository.saveAndFlush(order); // flush to surface any constraint violations

            return order;
        } catch (DataIntegrityViolationException e) {
            throw new IllegalStateException("Constraint violation", e);
        }
    }
}
