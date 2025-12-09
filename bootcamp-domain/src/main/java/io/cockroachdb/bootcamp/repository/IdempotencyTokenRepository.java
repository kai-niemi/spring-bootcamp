package io.cockroachdb.bootcamp.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import io.cockroachdb.bootcamp.model.IdempotencyToken;

@Repository
public interface IdempotencyTokenRepository extends JpaRepository<IdempotencyToken, UUID> {
    @Query("select it from IdempotencyToken it where it.id=:id")
    Optional<IdempotencyToken> findByIdempotencyKey(@Param("id") UUID idempotencyKey);
}
