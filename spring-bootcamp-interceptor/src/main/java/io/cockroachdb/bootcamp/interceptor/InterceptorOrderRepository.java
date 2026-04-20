package io.cockroachdb.bootcamp.interceptor;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;

import jakarta.persistence.QueryHint;

import io.cockroachdb.bootcamp.model.PurchaseOrder;

@Repository
public interface InterceptorOrderRepository extends JpaRepository<PurchaseOrder, UUID> {
    @Query(value = "from PurchaseOrder o "
                   + "join fetch o.customer "
                   + "join fetch o.orderItems oi "
                   + "join fetch oi.product")
    @QueryHints({
            @QueryHint(name = "org.hibernate.comment", value = "replaceJoinWithInnerJoin,appendForShare")
    })
    List<PurchaseOrder> findAllOrderDetails();
}
