package io.cockroachdb.bootcamp.performance;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import io.cockroachdb.bootcamp.annotation.FollowerRead;
import io.cockroachdb.bootcamp.annotation.FollowerReadType;
import io.cockroachdb.bootcamp.annotation.TransactionExplicit;
import io.cockroachdb.bootcamp.annotation.TransactionImplicit;
import io.cockroachdb.bootcamp.model.Product;
import io.cockroachdb.bootcamp.model.PurchaseOrder;
import io.cockroachdb.bootcamp.model.PurchaseOrderItem;
import io.cockroachdb.bootcamp.model.ShipmentStatus;
import io.cockroachdb.bootcamp.repository.OrderRepository;
import io.cockroachdb.bootcamp.repository.ProductRepository;
import io.cockroachdb.bootcamp.util.AssertUtils;
import io.cockroachdb.bootcamp.util.StreamUtils;

/**
 * Business service facade for the order system. This service represents the
 * transaction boundary and gateway to all business functionality such as order
 * placement.
 */
@Service
public class DefaultOrderService implements OrderService {
    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ObjectProvider<OrderService> objectProvider;

    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public BigDecimal sumOrderTotals() {
        AssertUtils.assertNoTransaction();
        return orderRepository.sumOrderTotal(ShipmentStatus.placed);
    }

    @Override
    @TransactionExplicit(readOnly = true)
    @FollowerRead(type = FollowerReadType.EXACT_STALENESS_READ)
    public BigDecimal sumOrderTotalsHistoricalQuery() {
        return orderRepository.sumOrderTotal(ShipmentStatus.placed);
    }

    @Override
    @TransactionImplicit
    public BigDecimal sumOrderTotalsHistoricalNativeQuery() {
        return orderRepository.sumOrderTotalNativeQuery(ShipmentStatus.placed.name());
    }

    @Override
    @TransactionExplicit
    public void placeOrder(PurchaseOrder order) throws BusinessException {
        AssertUtils.assertReadWriteTransaction();

        try {
            // Update product inventories for each line item
            order.getOrderItems().forEach(orderItem -> {
                Product product = productRepository.getReferenceById(
                        Objects.requireNonNull(orderItem.getProduct().getId()));
                product.addInventoryQuantity(-orderItem.getQuantity());
            });

            order.setStatus(ShipmentStatus.placed);
            order.setTotalPrice(order.subTotal());

            orderRepository.saveAndFlush(order); // flush to surface any constraint violations
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException("Constraint violation", e);
        }
    }

    @Override
    @TransactionImplicit
    public void placeOrders(Collection<PurchaseOrder> orders, int batchSize, Consumer<Integer> consumer) {
        AssertUtils.assertNoTransaction();

        final OrderService selfProxy = objectProvider.getObject();

        StreamUtils.chunkedStream(orders.stream(), batchSize)
                .forEach(chunk -> {
                    selfProxy.placeOrderChunk(chunk);
                    consumer.accept(chunk.size());
                });
    }

    /**
     * Shouldn't be a public method, but needed for transactional AOP advice.
     *
     * @param chunk a batch/chunk of orders to move atomically from transient to persistent state
     */
    @TransactionExplicit
    public void placeOrderChunk(Collection<PurchaseOrder> chunk) {
        AssertUtils.assertReadWriteTransaction();

        try {
            // Collect all unique product IDs from order line items
            Set<UUID> ids = chunk.stream()
                    .flatMap(purchaseOrder -> purchaseOrder.getOrderItems().stream()
                            .map(PurchaseOrderItem::getProduct)
                            .map(Product::getId)
                            .collect(Collectors.toSet())
                            .stream())
                    .collect(Collectors.toSet());

            // Use IN predicate
            List<Product> products = productRepository.findAllById(ids);

            // Update product inventories for each line item
            chunk.forEach(order -> {
                order.getOrderItems().forEach(purchaseOrderItem -> {
                    Product product = products.stream()
                            .filter(x -> Objects.requireNonNull(x.getId())
                                    .equals(purchaseOrderItem.getProduct().getId()))
                            .findFirst()
                            .orElseThrow();
                    product.addInventoryQuantity(-purchaseOrderItem.getQuantity());
                });

                order.setStatus(ShipmentStatus.placed);
                order.setTotalPrice(order.subTotal());
            });

            orderRepository.saveAllAndFlush(chunk); // flush to surface any constraint violations
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException("Constraint violation", e);
        }
    }
}
