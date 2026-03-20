package io.cockroachdb.bootcamp.transactions;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import io.cockroachdb.bootcamp.annotation.TransactionExplicit;
import io.cockroachdb.bootcamp.annotation.TransactionImplicit;
import io.cockroachdb.bootcamp.model.Customer;
import io.cockroachdb.bootcamp.model.Product;
import io.cockroachdb.bootcamp.model.PurchaseOrder;
import io.cockroachdb.bootcamp.model.ShipmentStatus;
import io.cockroachdb.bootcamp.repository.CustomerRepository;
import io.cockroachdb.bootcamp.repository.OrderRepository;
import io.cockroachdb.bootcamp.repository.ProductRepository;
import io.cockroachdb.bootcamp.util.AssertUtils;

@Service
public abstract class AbstractOrderService implements OrderService {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private ObjectProvider<OrderService> selfInvocationProvider;

    @TransactionImplicit
    @Override
    public Page<Product> findProducts(Pageable pageable) {
        AssertUtils.assertNoTransaction();
        return productRepository.findAll(pageable);
    }

    @TransactionImplicit
    @Override
    public Page<Customer> findCustomers(Pageable pageable) {
        AssertUtils.assertNoTransaction();
        return customerRepository.findAll(pageable);
    }

    @Override
    @TransactionImplicit
    public Page<PurchaseOrder> findOrders(Pageable pageable) {
        AssertUtils.assertNoTransaction();
        return orderRepository.findAll(pageable);
    }

    @Override
    @TransactionImplicit
    public Optional<PurchaseOrder> findOrderById(UUID id) {
        AssertUtils.assertNoTransaction();
        return orderRepository.findById(id);
    }

    @Override
    @TransactionImplicit
    public Optional<PurchaseOrder> findOrderDetailById(UUID id) {
        AssertUtils.assertNoTransaction();
        return orderRepository.findOrderDetailsById(id);
    }

    @Override
    @TransactionImplicit
    public List<PurchaseOrder> findOrderDetails() {
        AssertUtils.assertNoTransaction();
        return orderRepository.findAllOrderDetails();
    }

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

    @Override
    @TransactionImplicit
    public PurchaseOrder placeOrderWithLongWait(PurchaseOrder order) throws BusinessException {
        AssertUtils.assertNoTransaction();

        // Pre-validate order item products outside of DB txn scope
        order.getOrderItems().forEach(orderItem -> {
            UUID productId = Objects.requireNonNull(orderItem.getProduct().getId());
            inventoryService.verifyProductInventory(productId,
                    orderItem.getUnitPrice(),
                    orderItem.getQuantity());
        });

        return selfInvocationProvider.getObject().placeOrder(order);
    }
}


