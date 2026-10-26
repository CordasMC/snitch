package dev.jstus.snitch.report;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import dev.jstus.snitch.db.Transaction;

public interface ReportCaseRepository {

    ReportCaseRepository with(Transaction tx);

    Optional<ReportCase> findOpen(UUID reported);

    Optional<ReportCase> findOpenForUpdate(UUID reported);

    List<ReportCase> findAllOpen();

    long create(UUID reported, double riskScore, Instant createdAt);

    void updateRisk(long caseId, double riskScore, Instant lastReportAt);

    boolean resolve(long caseId, Resolution resolution, UUID resolvedBy, Instant resolvedAt);
}
