package io.cockroachdb.bootcamp.patterns;

import io.cockroachdb.bootcamp.model.PurchaseOrder;
import io.cockroachdb.bootcamp.patterns.outbox.OutboxEvent;

public class PurchaseOrderEvent extends OutboxEvent<PurchaseOrder> {
}
