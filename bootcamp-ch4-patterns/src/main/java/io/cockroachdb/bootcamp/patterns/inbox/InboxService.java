package io.cockroachdb.bootcamp.patterns.inbox;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import io.cockroachdb.bootcamp.annotation.ServiceFacade;
import io.cockroachdb.bootcamp.model.PurchaseOrder;

@ServiceFacade
public class InboxService {
    @Autowired
    private InboxRepository inboxRepository;

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public PurchaseOrder submitPurchaseOrder(PurchaseOrder order) {
        inboxRepository.writeEvent(order, "purchase_order");
        return order;
    }
}
