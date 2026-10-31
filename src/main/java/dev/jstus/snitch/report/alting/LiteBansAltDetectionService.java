package dev.jstus.snitch.report.alting;

import com.google.inject.Inject;
import dev.jstus.snitch.db.DatabaseExecutor;
import dev.jstus.snitch.report.ReportCase;
import dev.jstus.snitch.report.ReportRepository;
import litebans.api.Database;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

public class LiteBansAltDetectionService implements AltDetectionService {

    private static final String SHARED_IP_QUERY = """
            SELECT 1 FROM {history} reporter
            JOIN {history} other ON other.ip = reporter.ip
            WHERE reporter.uuid = ? AND reporter.ip <> '#' AND other.uuid IN (%s)
            LIMIT 1
            """;

    private final ReportRepository reports;
    private final ExecutorService executor;
    private final Logger logger;

    @Inject
    public LiteBansAltDetectionService(ReportRepository reports, @DatabaseExecutor ExecutorService executor, Logger logger) {
        this.reports = reports;
        this.executor = executor;
        this.logger = logger;
    }

    @Override
    public CompletableFuture<ReportAltDetectionVerdict> shouldAllowReport(UUID reporter, @Nullable ReportCase reportCase) {
        if (reportCase == null) {
            return CompletableFuture.completedFuture(ReportAltDetectionVerdict.ALLOW);
        }

        return CompletableFuture.supplyAsync(() -> verdict(reporter, reportCase), executor);
    }

    private ReportAltDetectionVerdict verdict(UUID reporter, ReportCase reportCase) {
        List<UUID> others = reports.findReporters(reportCase.id()).stream()
                .filter(uuid -> !uuid.equals(reporter))
                .toList();

        if (others.isEmpty()) {
            return ReportAltDetectionVerdict.ALLOW;
        }

        String placeholders = String.join(", ", Collections.nCopies(others.size(), "?"));
        try (PreparedStatement statement = Database.get().prepareStatement(SHARED_IP_QUERY.formatted(placeholders))) {
            statement.setString(1, reporter.toString());
            for (int index = 0; index < others.size(); index++) {
                statement.setString(index + 2, others.get(index).toString());
            }

            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? ReportAltDetectionVerdict.DENY : ReportAltDetectionVerdict.ALLOW;
            }
        } catch (SQLException exception) {
            logger.error("Failed to query LiteBans for alts of {} on case {}", reporter, reportCase.id(), exception);
            return ReportAltDetectionVerdict.FAILURE;
        }
    }
}
