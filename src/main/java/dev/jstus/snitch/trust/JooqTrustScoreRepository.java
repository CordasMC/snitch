package dev.jstus.snitch.trust;

import com.google.inject.Inject;
import dev.jstus.snitch.db.JooqTransaction;
import dev.jstus.snitch.db.Transaction;
import org.jooq.DSLContext;

import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.UUID;

import static dev.jstus.snitch.storage.jooq.tables.TrustScores.TRUST_SCORES;

public final class JooqTrustScoreRepository implements TrustScoreRepository {

    private final DSLContext dsl;

    @Inject
    public JooqTrustScoreRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public TrustScoreRepository with(Transaction tx) {
        if(!(tx instanceof JooqTransaction jooqTransaction)) {
            throw new IllegalArgumentException("Provided transaction must be of type JooqTransaction");
        }
        return new JooqTrustScoreRepository(jooqTransaction.dsl());
    }

    @Override
    public OptionalDouble find(UUID player) {
        Double score = dsl.select(TRUST_SCORES.SCORE)
                .from(TRUST_SCORES)
                .where(TRUST_SCORES.PLAYER.eq(player))
                .fetchOne(TRUST_SCORES.SCORE);
        return score == null ? OptionalDouble.empty() : OptionalDouble.of(score);
    }

    @Override
    public Map<UUID, Double> findAll(Collection<UUID> players) {
        return dsl.select(TRUST_SCORES.PLAYER, TRUST_SCORES.SCORE)
                .from(TRUST_SCORES)
                .where(TRUST_SCORES.PLAYER.in(players))
                .fetchMap(TRUST_SCORES.PLAYER, TRUST_SCORES.SCORE);
    }

    @Override
    public void upsert(UUID player, double score, Instant updatedAt) {
        dsl.insertInto(TRUST_SCORES, TRUST_SCORES.PLAYER, TRUST_SCORES.SCORE, TRUST_SCORES.UPDATED_AT)
                .values(player, score, updatedAt.toEpochMilli())
                .onDuplicateKeyUpdate()
                .set(TRUST_SCORES.SCORE, score)
                .set(TRUST_SCORES.UPDATED_AT, updatedAt.toEpochMilli())
                .execute();
    }

    @Override
    public void upsertAll(Map<UUID, Double> scores, Instant updatedAt) {
        scores.forEach((player, score) -> upsert(player, score, updatedAt));
    }
}
