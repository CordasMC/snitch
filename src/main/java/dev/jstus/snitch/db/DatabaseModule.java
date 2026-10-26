package dev.jstus.snitch.db;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import dev.jstus.snitch.config.impl.DatabaseConfig;
import dev.jstus.snitch.config.impl.DatabaseConfig.SqlDialect;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import org.flywaydb.core.Flyway;
import org.jooq.DSLContext;
import org.jooq.conf.Settings;
import org.jooq.impl.DSL;

@RequiredArgsConstructor
public class DatabaseModule extends AbstractModule {

    private final Path dataDirectory;

    @Provides
    @Singleton
    public DataSource dataSource(DatabaseConfig config) {
        SqlDialect dialect = config.getDialect();

        HikariConfig hikari = new HikariConfig();
        hikari.setPoolName("snitch-db");
        hikari.setJdbcUrl(dialect.jdbcUrl(config, dataDirectory));
        hikari.setDriverClassName(dialect.getDriverClassName());
        hikari.setMaximumPoolSize(dialect.poolSize(config));
        if (dialect.isServerBased()) {
            hikari.setUsername(config.getUsername());
            hikari.setPassword(config.getPassword());
        }

        HikariDataSource dataSource = new HikariDataSource(hikari);

        Flyway.configure(getClass().getClassLoader())
                .dataSource(dataSource)
                .locations(dialect.migrationLocations())
                .load()
                .migrate();

        return dataSource;
    }

    @Provides
    @Singleton
    public DSLContext dslContext(DataSource dataSource, DatabaseConfig config) {
        return DSL.using(dataSource, config.getDialect().getJooq(), new Settings().withRenderSchema(false));
    }

    @Provides
    @Singleton
    @DatabaseExecutor
    public ExecutorService databaseExecutor(DatabaseConfig config) {
        return Executors.newFixedThreadPool(
                config.getDialect().poolSize(config),
                Thread.ofPlatform().name("snitch-db-", 0).daemon(true).factory());
    }

    @Provides
    @Singleton
    public TransactionalOrchestrator transactionalOrchestrator(DSLContext dsl) {
        return new JooqTransactionalOrchestrator(dsl);
    }
}
