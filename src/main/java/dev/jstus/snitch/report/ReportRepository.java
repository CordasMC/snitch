package dev.jstus.snitch.report;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import dev.jstus.snitch.db.Transaction;
import org.jspecify.annotations.Nullable;

public interface ReportRepository {

    ReportRepository with(Transaction tx);

    boolean insertIfAbsent(long caseId, UUID reporter, double trustWeight, double multiplier,
                           @Nullable String reason, Instant createdAt);

    List<Report> findByCase(long caseId);

    List<UUID> findReporters(long caseId);

    Map<Long, Integer> countByCase(Collection<Long> caseIds);

    Map<Long, List<String>> findRecentReasons(Collection<Long> caseIds, int limit);
}
