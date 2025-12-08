package io.cockroachdb.bootcamp.contention;

import java.util.Optional;
import java.util.UUID;

import io.cockroachdb.bootcamp.model.PurchaseOrder;
import io.cockroachdb.bootcamp.model.ShipmentStatus;
import io.cockroachdb.bootcamp.model.Simulation;

public interface OrderService {
    Optional<PurchaseOrder> findOrderById(UUID id);

    PurchaseOrder placeOrder(PurchaseOrder order);

    void updateOrder(UUID id, ShipmentStatus preCondition, ShipmentStatus postCondition,
                     Simulation simulation);
}
