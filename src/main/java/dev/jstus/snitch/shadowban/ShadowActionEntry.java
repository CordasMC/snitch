package dev.jstus.snitch.shadowban;

import java.time.Instant;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

public record ShadowActionEntry(
        long id,
        UUID target,
        @Nullable UUID issuer,
        ShadowAction action,
        Instant createdAt
) {
}
