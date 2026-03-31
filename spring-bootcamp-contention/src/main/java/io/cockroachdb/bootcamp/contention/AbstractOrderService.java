package io.cockroachdb.bootcamp.contention;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
public abstract class AbstractOrderService implements OrderService {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Override
    @TransactionExplicit(readOnly = true)
    public Optional<PurchaseOrder> findOrderById(UUID id) {
        AssertUtils.assertReadOnlyTransaction();
        return orderRepository.findById(id);
    }

    /**
     * Place a single purchase order.
     *
     * @param order the order detail in detached state
     */
    @Override
    @TransactionExplicit
    public PurchaseOrder placeOrder(PurchaseOrder order) throws BusinessException {
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
            throw new BusinessException("Constraint violation", e);
        }
    }
}

