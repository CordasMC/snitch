package dev.jstus.snitch.shadowban;

import com.google.inject.Inject;
import dev.jstus.snitch.db.JooqTransaction;
import dev.jstus.snitch.db.Transaction;
import dev.jstus.snitch.storage.jooq.tables.records.ShadowActionsRecord;
import dev.jstus.snitch.storage.jooq.tables.records.ShadowBansRecord;
import org.jooq.DSLContext;
import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static dev.jstus.snitch.storage.jooq.tables.ShadowActions.SHADOW_ACTIONS;
import static dev.jstus.snitch.storage.jooq.tables.ShadowBans.SHADOW_BANS;

public final class JooqShadowBanRepository implements ShadowBanRepository {

    private final DSLContext dsl;

    @Inject
    public JooqShadowBanRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public ShadowBanRepository with(Transaction tx) {
        if(!(tx instanceof JooqTransaction jooqTransaction)) {
            throw new IllegalArgumentException("Provided transaction must be of type JooqTransaction");
        }
        return new JooqShadowBanRepository(jooqTransaction.dsl());
    }

    @Override
    public boolean insertBan(UUID target, @Nullable UUID issuer, Instant createdAt) {
        return dsl.insertInto(SHADOW_BANS, SHADOW_BANS.TARGET, SHADOW_BANS.ISSUER, SHADOW_BANS.CREATED_AT)
                .values(target, issuer, createdAt.toEpochMilli())
                .onDuplicateKeyIgnore()
                .execute() > 0;
    }

    @Override
    public boolean deleteBan(UUID target) {
        return dsl.deleteFrom(SHADOW_BANS)
                .where(SHADOW_BANS.TARGET.eq(target))
                .execute() > 0;
    }

    @Override
    public void insertAction(UUID target, @Nullable UUID issuer, ShadowAction action, Instant createdAt) {
        dsl.insertInto(SHADOW_ACTIONS,
                        SHADOW_ACTIONS.TARGET, SHADOW_ACTIONS.ISSUER, SHADOW_ACTIONS.ACTION, SHADOW_ACTIONS.CREATED_AT)
                .values(target, issuer, action.toString(), createdAt.toEpochMilli())
                .execute();
    }

    @Override
    public Optional<ShadowBan> findBan(UUID target) {
        return dsl.selectFrom(SHADOW_BANS)
                .where(SHADOW_BANS.TARGET.eq(target))
                .fetchOptional(JooqShadowBanRepository::toBan);
    }

    @Override
    public List<ShadowActionEntry> findActionsAgainst(UUID target) {
        return dsl.selectFrom(SHADOW_ACTIONS)
                .where(SHADOW_ACTIONS.TARGET.eq(target))
                .orderBy(SHADOW_ACTIONS.CREATED_AT.asc(), SHADOW_ACTIONS.ID.asc())
                .fetch(JooqShadowBanRepository::toActionEntry);
    }

    @Override
    public List<ShadowActionEntry> findActionsBy(UUID issuer) {
        return dsl.selectFrom(SHADOW_ACTIONS)
                .where(SHADOW_ACTIONS.ISSUER.eq(issuer))
                .orderBy(SHADOW_ACTIONS.CREATED_AT.asc(), SHADOW_ACTIONS.ID.asc())
                .fetch(JooqShadowBanRepository::toActionEntry);
    }

    private static ShadowBan toBan(ShadowBansRecord record) {
        return new ShadowBan(
                record.getTarget(),
                record.getIssuer(),
                Instant.ofEpochMilli(record.getCreatedAt()));
    }

    private static ShadowActionEntry toActionEntry(ShadowActionsRecord record) {
        return new ShadowActionEntry(
                record.getId(),
                record.getTarget(),
                record.getIssuer(),
                ShadowAction.fromString(record.getAction()),
                Instant.ofEpochMilli(record.getCreatedAt()));
    }
}
