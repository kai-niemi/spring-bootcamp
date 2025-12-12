package io.cockroachdb.bootcamp.transactions;

import java.time.LocalDateTime;
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
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;

import io.cockroachdb.bootcamp.annotation.TransactionExplicit;
import io.cockroachdb.bootcamp.annotation.TransactionImplicit;
import io.cockroachdb.bootcamp.aspect.TransientExceptionClassifier;
import io.cockroachdb.bootcamp.model.Customer;
import io.cockroachdb.bootcamp.model.Product;
import io.cockroachdb.bootcamp.model.PurchaseOrder;
import io.cockroachdb.bootcamp.model.ShipmentStatus;
import io.cockroachdb.bootcamp.model.Simulation;
import io.cockroachdb.bootcamp.repository.CustomerRepository;
import io.cockroachdb.bootcamp.repository.OrderRepository;
import io.cockroachdb.bootcamp.repository.ProductRepository;
import io.cockroachdb.bootcamp.util.AssertUtils;

@Service
public class DefaultOrderService implements OrderService {
    private final Logger logger = LoggerFactory.getLogger(getClass());

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

    @PersistenceContext
    private EntityManager entityManager;

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
    public PurchaseOrder placeOrderWithValidation(PurchaseOrder order) throws BusinessException {
        AssertUtils.assertNoTransaction();

        // Pre-validate order item products outside of DB txn scope
        order.getOrderItems().forEach(orderItem -> {
            UUID productId = Objects.requireNonNull(orderItem.getProduct().getId());
            inventoryService.verifyProductInventory(productId,
                    orderItem.getUnitPrice(),
                    orderItem.getQuantity());
        });

        OrderService selfProxy = selfInvocationProvider.getObject();

        return selfProxy.placeOrder(order);
    }

    @Override
    @TransactionExplicit
    @Retryable(predicate = TransientExceptionClassifier.class,
            maxRetries = 5,
            maxDelay = 15_0000,
            multiplier = 1.5)
    public void updateOrder(UUID id,
                            ShipmentStatus preCondition,
                            ShipmentStatus postCondition,
                            Simulation simulation) {
        AssertUtils.assertReadWriteTransaction();

        if (Objects.equals(simulation.getPattern(), Simulation.Pattern.READ_MODIFY_WRITE)) {
            TypedQuery<PurchaseOrder> query = entityManager.createQuery(
                    "select po from PurchaseOrder po where po.id=:id", PurchaseOrder.class);
            query.setParameter("id", id);
            query.setLockMode(LockModeType.PESSIMISTIC_WRITE);
            query.setLockMode(simulation.getLockModeType());

            // SELECT .. FOR UPDATE
            PurchaseOrder purchaseOrder = query.getSingleResult();
            if (purchaseOrder == null) {
                throw new ObjectRetrievalFailureException(PurchaseOrder.class, id);
            }

            if (purchaseOrder.getStatus().equals(preCondition)) {
                purchaseOrder.setStatus(postCondition);
                purchaseOrder.setDateUpdated(LocalDateTime.now());
            } else {
                logger.warn("Precondition failed (no-op): %s != %s".formatted(preCondition, postCondition));
            }
        } else {
            // UPDATE directly, which is also a SELECT .. FOR UPDATE in the read part
            Query update = entityManager.createQuery(
                    "update PurchaseOrder po set po.status=:postStatus where po.id=:id and po.status=:preStatus");
            update.setParameter("id", id);
            update.setParameter("preStatus", preCondition);
            update.setParameter("postStatus", postCondition);
            update.setLockMode(simulation.getLockModeType());

            int rowsAffected = update.executeUpdate();
            if (rowsAffected != 1) {
                logger.warn("Precondition failed (no-op): %s != %s".formatted(preCondition, postCondition));
            }
        }

        simulation.thinkTime();

        entityManager.flush();
    }
}
