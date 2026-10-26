package dev.jstus.snitch.config.impl;

import de.exlll.configlib.Comment;
import de.exlll.configlib.Configuration;
import dev.jstus.snitch.config.ConfigurationFile;
import java.nio.file.Path;
import java.util.Locale;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jooq.SQLDialect;

@Getter
@Configuration
@ConfigurationFile("database.yml")
@SuppressWarnings("FieldMayBeFinal")
public class DatabaseConfig {

    @Comment({
            "The SQL dialect to store data with.",
            "Supported values: POSTGRES, MYSQL, MARIADB, SQLITE, H2",
            "We strongly recommend you setup a postgres database, it's the more modern solution."
    })
    private SqlDialect dialect = SqlDialect.SQLITE;

    @Comment({"", "Connection details for server-based databases (POSTGRES, MYSQL, MARIADB)."})
    private String host = "localhost";
    private int port = 5432;
    private String database = "snitch";
    private String username = "root";
    private String password = "";

    @Comment({"", "Storage file for file-based databases (SQLITE, H2), relative to the plugin folder."})
    private String file = "storage.db";

    @Comment({"", "Maximum connections in the pool (SQLITE always uses a single connection)."})
    private int poolSize = 8;

    @Getter
    @RequiredArgsConstructor
    public enum SqlDialect {
        POSTGRES(SQLDialect.POSTGRES, "org.postgresql.Driver", true),
        MYSQL(SQLDialect.MYSQL, "org.mariadb.jdbc.Driver", true),
        MARIADB(SQLDialect.MARIADB, "org.mariadb.jdbc.Driver", true),
        SQLITE(SQLDialect.SQLITE, "org.sqlite.JDBC", false),
        H2(SQLDialect.H2, "org.h2.Driver", false);

        private final SQLDialect jooq;
        private final String driverClassName;
        private final boolean serverBased;

        public String jdbcUrl(DatabaseConfig config, Path dataDirectory) {
            return switch (this) {
                case POSTGRES -> "jdbc:postgresql://%s:%d/%s".formatted(config.host, config.port, config.database);
                case MYSQL, MARIADB -> "jdbc:mariadb://%s:%d/%s".formatted(config.host, config.port, config.database);
                case SQLITE -> "jdbc:sqlite:" + dataDirectory.resolve(config.file);
                case H2 -> "jdbc:h2:file:" + dataDirectory.resolve(config.file).toAbsolutePath()
                        + ";DATABASE_TO_LOWER=TRUE";
            };
        }

        public int poolSize(DatabaseConfig config) {
            return this == SQLITE ? 1 : config.poolSize;
        }

        public String[] migrationLocations() {
            return new String[] {
                    "classpath:db/migration/common",
                    "classpath:db/migration/" + name().toLowerCase(Locale.ROOT)
            };
        }
    }
}
