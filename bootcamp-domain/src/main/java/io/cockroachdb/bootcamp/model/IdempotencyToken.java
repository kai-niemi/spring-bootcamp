package io.cockroachdb.bootcamp.model;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "idempotency_token")
public class IdempotencyToken extends AbstractEntity<UUID> {
    @Id
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected IdempotencyToken() {
    }

    public IdempotencyToken(UUID id) {
        this.id = id;
        this.createdAt = LocalDateTime.now();
    }

    @Override
    public UUID getId() {
        return id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    public String toString() {
        return "IdempotencyToken{" +
               "id=" + id +
               ", createdAt=" + createdAt +
               "} " + super.toString();
    }
}
