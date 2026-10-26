package dev.jstus.snitch.trust;

import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.UUID;

import dev.jstus.snitch.db.Transaction;
import org.jooq.DSLContext;

public interface TrustScoreRepository {

    TrustScoreRepository with(Transaction tx);

    OptionalDouble find(UUID player);

    Map<UUID, Double> findAll(Collection<UUID> players);

    void upsert(UUID player, double score, Instant updatedAt);

    void upsertAll(Map<UUID, Double> scores, Instant updatedAt);
}
