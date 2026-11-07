package dev.jstus.snitch.shadowban;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import dev.jstus.snitch.db.Transaction;
import org.jspecify.annotations.Nullable;

public interface ShadowBanRepository {

    ShadowBanRepository with(Transaction tx);

    boolean insertBan(UUID target, @Nullable UUID issuer, Instant createdAt);

    boolean deleteBan(UUID target);

    void insertAction(UUID target, @Nullable UUID issuer, ShadowAction action, Instant createdAt);

    Optional<ShadowBan> findBan(UUID target);

    List<ShadowActionEntry> findActionsAgainst(UUID target);

    List<ShadowActionEntry> findActionsBy(UUID issuer);
}
