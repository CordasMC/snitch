package dev.jstus.snitch.db;

import com.google.inject.Inject;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import org.jooq.DSLContext;
import org.jooq.exception.DataAccessException;
import org.jooq.exception.IntegrityConstraintViolationException;

public final class JooqTransactionalOrchestrator implements TransactionalOrchestrator {

    private static final int MAX_ATTEMPTS = 3;

    private static final Set<String> RETRYABLE_SQL_STATES = Set.of("40001", "40P01", "HYT00");

    private final DSLContext dsl;

    @Inject
    public JooqTransactionalOrchestrator(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public <T> T orchestrateTransactionally(Function<Transaction, T> work) {
        DataAccessException failure = null;

        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                return dsl.transactionResult(configuration ->
                        work.apply(new JooqTransaction(configuration.dsl())));
            } catch (DataAccessException exception) {
                if (!isRetryable(exception)) {
                    throw exception;
                }
                failure = exception;
                backoff(attempt);
            }
        }

        throw failure;
    }

    private boolean isRetryable(DataAccessException exception) {
        if (exception instanceof IntegrityConstraintViolationException) {
            return true;
        }

        String state = exception.sqlState();
        return state != null && RETRYABLE_SQL_STATES.contains(state);
    }

    private void backoff(int attempt) {
        try {
            Thread.sleep(ThreadLocalRandom.current().nextLong(5L * attempt, 20L * attempt));
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while retrying a transaction", interrupted);
        }
    }
}
