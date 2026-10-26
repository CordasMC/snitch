package dev.jstus.snitch.report;

import java.time.Instant;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

public record Report(
        long id,
        long caseId,
        UUID reporter,
        double trustWeight,
        double multiplier,
        @Nullable String reason,
        Instant createdAt
) {
}
