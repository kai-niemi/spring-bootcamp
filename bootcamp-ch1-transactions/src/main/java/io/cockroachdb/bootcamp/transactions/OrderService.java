package io.cockroachdb.bootcamp.transactions;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import io.cockroachdb.bootcamp.model.Customer;
import io.cockroachdb.bootcamp.model.Product;
import io.cockroachdb.bootcamp.model.PurchaseOrder;
import io.cockroachdb.bootcamp.model.ShipmentStatus;
import io.cockroachdb.bootcamp.model.Simulation;

public interface OrderService {
    Page<Product> findProducts(Pageable pageable);

    Page<Customer> findCustomers(Pageable pageable);

    Page<PurchaseOrder> findOrders(Pageable pageable);

    Optional<PurchaseOrder> findOrderById(UUID id);

    Optional<PurchaseOrder> findOrderDetailById(UUID id);

    List<PurchaseOrder> findOrderDetails();

    PurchaseOrder placeOrder(PurchaseOrder order);

    PurchaseOrder placeOrderWithLongWait(PurchaseOrder order);

    void updateOrder(UUID id, ShipmentStatus preCondition, ShipmentStatus postCondition,
                     Simulation simulation);
}
