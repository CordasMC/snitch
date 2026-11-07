package dev.jstus.snitch.shadowban;

import java.time.Instant;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

public record ShadowBan(
        UUID target,
        @Nullable UUID issuer,
        Instant createdAt
) {
}
