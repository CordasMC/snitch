package dev.jstus.snitch.report;

import com.google.inject.Inject;
import dev.jstus.snitch.db.JooqTransaction;
import dev.jstus.snitch.db.Transaction;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static dev.jstus.snitch.storage.jooq.tables.Reports.REPORTS;

public final class JooqReportRepository implements ReportRepository {

    private final DSLContext dsl;

    @Inject
    public JooqReportRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public ReportRepository with(Transaction tx) {
        if(!(tx instanceof JooqTransaction jooqTransaction)) {
            throw new IllegalArgumentException("Provided transaction must be of type JooqTransaction");
        }
        return new JooqReportRepository(jooqTransaction.dsl());
    }

    @Override
    public boolean insertIfAbsent(long caseId, UUID reporter, double trustWeight, double multiplier,
                                  @Nullable String reason, Instant createdAt) {
        return dsl.insertInto(REPORTS,
                        REPORTS.CASE_ID, REPORTS.REPORTER, REPORTS.TRUST_WEIGHT,
                        REPORTS.MULTIPLIER, REPORTS.REASON, REPORTS.CREATED_AT)
                .values(caseId, reporter, trustWeight, multiplier, reason, createdAt.toEpochMilli())
                .onDuplicateKeyIgnore()
                .execute() > 0;
    }

    @Override
    public List<Report> findByCase(long caseId) {
        return dsl.select(REPORTS.ID, REPORTS.CASE_ID, REPORTS.REPORTER, REPORTS.TRUST_WEIGHT,
                        REPORTS.MULTIPLIER, REPORTS.REASON, REPORTS.CREATED_AT)
                .from(REPORTS)
                .where(REPORTS.CASE_ID.eq(caseId))
                .orderBy(REPORTS.CREATED_AT.asc())
                .fetch(record -> new Report(
                        record.value1(),
                        record.value2(),
                        record.value3(),
                        record.value4(),
                        record.value5(),
                        record.value6(),
                        Instant.ofEpochMilli(record.value7())));
    }

    @Override
    public List<UUID> findReporters(long caseId) {
        return dsl.select(REPORTS.REPORTER)
                .from(REPORTS)
                .where(REPORTS.CASE_ID.eq(caseId))
                .fetch(REPORTS.REPORTER);
    }

    @Override
    public Map<Long, Integer> countByCase(Collection<Long> caseIds) {
        Field<Integer> count = DSL.count();
        return dsl.select(REPORTS.CASE_ID, count)
                .from(REPORTS)
                .where(REPORTS.CASE_ID.in(caseIds))
                .groupBy(REPORTS.CASE_ID)
                .fetchMap(REPORTS.CASE_ID, count);
    }

    @Override
    public Map<Long, List<String>> findRecentReasons(Collection<Long> caseIds, int limit) {
        if (caseIds.isEmpty()) {
            return Map.of();
        }

        Field<Integer> rank = DSL.rowNumber()
                .over(DSL.partitionBy(REPORTS.CASE_ID).orderBy(REPORTS.CREATED_AT.desc(), REPORTS.ID.desc()))
                .as("rank");

        Table<?> ranked = dsl.select(REPORTS.CASE_ID, REPORTS.REASON, rank)
                .from(REPORTS)
                .where(REPORTS.CASE_ID.in(caseIds))
                .and(REPORTS.REASON.isNotNull())
                .asTable("ranked");

        Field<Long> rankedCaseId = ranked.field(REPORTS.CASE_ID);
        Field<String> rankedReason = ranked.field(REPORTS.REASON);
        Field<Integer> rankedRank = ranked.field(rank);

        return dsl.select(rankedCaseId, rankedReason)
                .from(ranked)
                .where(rankedRank.le(limit))
                .orderBy(rankedCaseId, rankedRank)
                .fetchGroups(rankedCaseId, rankedReason);
    }
}
