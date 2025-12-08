package io.cockroachdb.bootcamp.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.QueryHint;

import io.cockroachdb.bootcamp.model.PurchaseOrder;
import io.cockroachdb.bootcamp.model.ShipmentStatus;

@Repository
public interface OrderRepository extends JpaRepository<PurchaseOrder, UUID> {
    @Query(value = "select sum(po.totalPrice) from PurchaseOrder po where po.status=:status")
    BigDecimal sumOrderTotal(@Param("status") ShipmentStatus status);

    @Query(value = "select sum(po.total_price) from purchase_order po " +
                   "as of system time follower_read_timestamp() " +
                   "where po.status=?1", nativeQuery = true)
    BigDecimal sumOrderTotalNativeQuery(String status);

    // Embeddable type and not an entity
    @Modifying
    @Query(value = "delete from purchase_order_item where 1=1", nativeQuery = true)
    void deleteAllOrderItems();

//    @Query(value = "from PurchaseOrder o "
//                   + "join fetch o.customer c where o.id=:id")
//    Optional<PurchaseOrder> findOrderById(@Param("id") UUID id);
//
    @Query(value = "from PurchaseOrder o "
                   + "join fetch o.customer c "
                   + "join fetch o.orderItems oi "
                   + "join fetch oi.product "
                   + "where o.id=:id")
    Optional<PurchaseOrder> findOrderDetailsById(@Param("id") UUID id);

    @Query(value = "from PurchaseOrder o "
                   + "join fetch o.customer "
                   + "join fetch o.orderItems oi "
                   + "join fetch oi.product")
    @QueryHints({
            @QueryHint(name = "org.hibernate.comment", value = "replaceJoinWithInnerJoin,appendForShare")
    })
    List<PurchaseOrder> findAllOrderDetails();
}
