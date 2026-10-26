package dev.jstus.snitch.db;

import java.util.function.Function;

public interface TransactionalOrchestrator {

    <T> T orchestrateTransactionally(Function<Transaction, T> work);

}
