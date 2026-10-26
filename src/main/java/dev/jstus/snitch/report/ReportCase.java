package dev.jstus.snitch.report;

import java.time.Instant;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

public record ReportCase(
        long id,
        UUID reported,
        double riskScore,
        @Nullable Resolution resolution,
        Instant createdAt,
        Instant lastReportAt,
        @Nullable UUID resolvedBy,
        @Nullable Instant resolvedAt
) {

    public boolean open() {
        return resolution == null;
    }
}
