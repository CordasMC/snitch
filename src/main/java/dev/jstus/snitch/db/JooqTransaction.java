package dev.jstus.snitch.db;

import org.jooq.DSLContext;

public final class JooqTransaction implements Transaction {

    private final DSLContext dsl;

    JooqTransaction(DSLContext dsl) {
        this.dsl = dsl;
    }

    public DSLContext dsl() {
        return dsl;
    }
}
