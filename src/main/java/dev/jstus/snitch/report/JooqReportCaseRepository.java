package dev.jstus.snitch.report;

import com.google.inject.Inject;
import dev.jstus.snitch.db.JooqTransaction;
import dev.jstus.snitch.db.Transaction;
import dev.jstus.snitch.storage.jooq.tables.records.ReportCasesRecord;
import org.jooq.DSLContext;
import org.jooq.ResultQuery;
import org.jooq.SQLDialect;
import org.jooq.SelectConditionStep;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static dev.jstus.snitch.storage.jooq.tables.ReportCases.REPORT_CASES;

public final class JooqReportCaseRepository implements ReportCaseRepository {

    private final DSLContext dsl;

    @Inject
    public JooqReportCaseRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public ReportCaseRepository with(Transaction tx) {
        if(!(tx instanceof JooqTransaction jooqTransaction)) {
            throw new IllegalArgumentException("Provided transaction must be of type JooqTransaction");
        }
        return new JooqReportCaseRepository(jooqTransaction.dsl());
    }

    @Override
    public Optional<ReportCase> findOpen(UUID reported) {
        return openQuery(reported, false).fetchOptional(JooqReportCaseRepository::toCase);
    }

    @Override
    public Optional<ReportCase> findOpenForUpdate(UUID reported) {
        return openQuery(reported, true).fetchOptional(JooqReportCaseRepository::toCase);
    }

    @Override
    public List<ReportCase> findAllOpen() {
        return dsl.selectFrom(REPORT_CASES)
                .where(REPORT_CASES.OPEN.isTrue())
                .orderBy(REPORT_CASES.RISK_SCORE.desc())
                .fetch(JooqReportCaseRepository::toCase);
    }

    private ResultQuery<ReportCasesRecord> openQuery(UUID reported, boolean lock) {
        SelectConditionStep<ReportCasesRecord> query = dsl.selectFrom(REPORT_CASES)
                .where(REPORT_CASES.REPORTED.eq(reported))
                .and(REPORT_CASES.OPEN.isTrue());

        // For some ungodly reason, SQLITE had to be special and not have a FOR UPDATE
        // We pray that the user configures a connection pool size of 1 for SQLITE
        // If they don't, blame the shitty database
        return lock && dsl.configuration().family() != SQLDialect.SQLITE ? query.forUpdate() : query;
    }

    @Override
    public long create(UUID reported, double riskScore, Instant createdAt) {
        long timestamp = createdAt.toEpochMilli();

        return dsl.insertInto(REPORT_CASES,
                        REPORT_CASES.REPORTED, REPORT_CASES.RISK_SCORE, REPORT_CASES.OPEN,
                        REPORT_CASES.CREATED_AT, REPORT_CASES.LAST_REPORT_AT)
                .values(reported, riskScore, true, timestamp, timestamp)
                .returningResult(REPORT_CASES.ID)
                .fetchSingle()
                .value1();
    }

    @Override
    public void updateRisk(long caseId, double riskScore, Instant lastReportAt) {
        dsl.update(REPORT_CASES)
                .set(REPORT_CASES.RISK_SCORE, riskScore)
                .set(REPORT_CASES.LAST_REPORT_AT, lastReportAt.toEpochMilli())
                .where(REPORT_CASES.ID.eq(caseId))
                .execute();
    }

    @Override
    public boolean resolve(long caseId, Resolution resolution, UUID resolvedBy, Instant resolvedAt) {
        return dsl.update(REPORT_CASES)
                .setNull(REPORT_CASES.OPEN)
                .set(REPORT_CASES.RESOLUTION, resolution.name())
                .set(REPORT_CASES.RESOLVED_BY, resolvedBy)
                .set(REPORT_CASES.RESOLVED_AT, resolvedAt.toEpochMilli())
                .where(REPORT_CASES.ID.eq(caseId))
                .and(REPORT_CASES.OPEN.isTrue())
                .execute() > 0;
    }

    private static ReportCase toCase(ReportCasesRecord record) {
        return new ReportCase(
                record.getId(),
                record.getReported(),
                record.getRiskScore(),
                record.getResolution() == null ? null : Resolution.valueOf(record.getResolution()),
                Instant.ofEpochMilli(record.getCreatedAt()),
                Instant.ofEpochMilli(record.getLastReportAt()),
                record.getResolvedBy(),
                record.getResolvedAt() == null ? null : Instant.ofEpochMilli(record.getResolvedAt()));
    }
}
