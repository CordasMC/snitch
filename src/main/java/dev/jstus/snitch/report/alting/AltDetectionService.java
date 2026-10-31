package dev.jstus.snitch.report.alting;

import dev.jstus.snitch.report.ReportCase;
import org.jspecify.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface AltDetectionService {

    CompletableFuture<ReportAltDetectionVerdict> shouldAllowReport(UUID reporter, @Nullable ReportCase reportCase);
}
