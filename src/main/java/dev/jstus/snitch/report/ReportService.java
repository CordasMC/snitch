package dev.jstus.snitch.report;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import dev.jstus.snitch.config.impl.ReportConfig;
import dev.jstus.snitch.db.DatabaseExecutor;
import dev.jstus.snitch.db.TransactionalOrchestrator;
import dev.jstus.snitch.report.alting.AltDetectionService;
import dev.jstus.snitch.report.alting.ReportAltDetectionVerdict;
import dev.jstus.snitch.shadowban.ShadowBanService;
import dev.jstus.snitch.trust.TrustScoreRepository;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;
import org.jooq.exception.DataAccessException;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

@Singleton
public class ReportService {

    private static final int MENU_REASON_LIMIT = 5;

    private final ReportCaseRepository cases;
    private final ReportRepository reports;
    private final TrustScoreRepository trust;
    private final Provider<ReportConfig> config;
    private final ExecutorService executor;
    private final TransactionalOrchestrator orchestrator;
    private final Logger logger;
    private final AltDetectionService altPrevention;
    private final ShadowBanService shadowBanService;

    @Inject
    public ReportService(ReportCaseRepository cases, ReportRepository reports, TrustScoreRepository trust,
                         Provider<ReportConfig> config, @DatabaseExecutor ExecutorService executor,
                         TransactionalOrchestrator orchestrator, Logger logger, AltDetectionService altPrevention,
                         ShadowBanService shadowBanService) {
        this.cases = cases;
        this.reports = reports;
        this.trust = trust;
        this.config = config;
        this.executor = executor;
        this.orchestrator = orchestrator;
        this.logger = logger;
        this.altPrevention = altPrevention;
        this.shadowBanService = shadowBanService;
    }

    public CompletableFuture<ReportResult> report(UUID reporter, UUID reported, @Nullable String reason) {
        if (reporter.equals(reported)) {
            return CompletableFuture.completedFuture(ReportResult.self());
        }

        return shadowBanService.isShadowBanned(reporter)
                .thenCompose(shadowBanned -> shadowBanned
                        ? CompletableFuture.completedFuture(ReportResult.shadowBanned())
                        : fileReport(reporter, reported, reason));
    }

    private CompletableFuture<ReportResult> fileReport(UUID reporter, UUID reported, @Nullable String reason) {
        return async(() -> cases.findOpen(reported).orElse(null))
                .thenCompose(open -> altPrevention.shouldAllowReport(reporter, open))
                .thenCompose(verdict -> verdict == ReportAltDetectionVerdict.DENY
                        ? CompletableFuture.completedFuture(ReportResult.altPrevented())
                        : async(() -> fileReportTransactional(reporter, reported, reason)));
    }

    private ReportResult fileReportTransactional(UUID reporter, UUID reported, @Nullable String reason) {
        ReportConfig config = this.config.get();

        try {
            return orchestrator.orchestrateTransactionally(tx -> {
                ReportCaseRepository cases = this.cases.with(tx);
                ReportRepository reports = this.reports.with(tx);
                Instant now = Instant.now();

                double weight = this.trust.with(tx).find(reporter).orElse(config.getBaseTrustScore());

                // We get the case again, this time locking it, to prevent a lost update occurring
                ReportCase open = cases.findOpenForUpdate(reported).orElse(null);
                if (open == null) {
                    long caseId = cases.create(reported, weight, now);
                    reports.insertIfAbsent(caseId, reporter, weight, 1.0, reason, now);
                    return ReportResult.reported(weight);
                }

                double multiplier = withinSuccessionWindow(config, open.lastReportAt(), now)
                        ? config.getSuccessionMultiplier()
                        : 1.0;

                if (!reports.insertIfAbsent(open.id(), reporter, weight, multiplier, reason, now)) {
                    return ReportResult.alreadyReported();
                }

                double risk = (open.riskScore() + weight) * multiplier;
                cases.updateRisk(open.id(), risk, now);
                return ReportResult.reported(risk);
            });
        } catch (DataAccessException exception) {
            logger.error("Failed to file report by {} against {}", reporter, reported, exception);
            return ReportResult.error();
        }
    }

    public CompletableFuture<ResolveResult> resolve(UUID reported, Resolution resolution, UUID staff) {
        return async(() -> resolveTransactional(reported, resolution, staff));
    }

    private ResolveResult resolveTransactional(UUID reported, Resolution resolution, UUID staff) {
        ReportConfig config = this.config.get();

        try {
            return orchestrator.orchestrateTransactionally(tx -> {
                ReportCaseRepository cases = this.cases.with(tx);
                TrustScoreRepository trust = this.trust.with(tx);
                Instant now = Instant.now();

                ReportCase open = cases.findOpenForUpdate(reported).orElse(null);
                if (open == null || !cases.resolve(open.id(), resolution, staff, now)) {
                    return ResolveResult.noOpenCase();
                }

                List<UUID> reporters = this.reports.with(tx).findReporters(open.id());
                Map<UUID, Double> trustScores = trust.findAll(reporters);

                Map<UUID, Double> adjustedTrustScores = new HashMap<>();
                for (UUID reporter : reporters) {
                    double current = trustScores.getOrDefault(reporter, config.getBaseTrustScore());
                    adjustedTrustScores.put(reporter, adjust(config, current, resolution));
                }
                trust.upsertAll(adjustedTrustScores, now);

                return ResolveResult.resolved(reporters.size());
            });
        } catch (DataAccessException exception) {
            logger.error("Failed to resolve case against {} as {}", reported, resolution, exception);
            return ResolveResult.error();
        }
    }

    public CompletableFuture<List<ReportCase>> openCases() {
        return async(cases::findAllOpen);
    }

    public CompletableFuture<List<OpenCaseSummary>> openCaseSummaries() {
        return async(() -> {
            List<ReportCase> open = cases.findAllOpen();
            List<Long> caseIds = open.stream().map(ReportCase::id).toList();

            Map<Long, Integer> counts = reports.countByCase(caseIds);
            Map<Long, List<String>> reasons = reports.findRecentReasons(caseIds, MENU_REASON_LIMIT);

            return open.stream()
                    .map(reportCase -> new OpenCaseSummary(
                            reportCase,
                            counts.getOrDefault(reportCase.id(), 0),
                            reasons.getOrDefault(reportCase.id(), List.of())))
                    .toList();
        });
    }

    private double adjust(ReportConfig config, double score, Resolution resolution) {
        return switch (resolution) {
            case POSITIVE -> score + config.getPositiveTrustReward();
            case NEGATIVE -> score * config.getNegativeTrustFactor();
        };
    }

    private boolean withinSuccessionWindow(ReportConfig config, Instant lastReportAt, Instant now) {
        return !now.isAfter(lastReportAt.plus(config.successionWindow()));
    }

    private <T> CompletableFuture<T> async(Supplier<T> query) {
        return CompletableFuture.supplyAsync(query, executor);
    }
}
