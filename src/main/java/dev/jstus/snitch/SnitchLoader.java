package dev.jstus.snitch;

import io.papermc.paper.ServerBuildInfo;
import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
import io.papermc.paper.plugin.loader.PluginLoader;
import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;

public final class SnitchLoader implements PluginLoader {

    private static final String INVUI_LATEST = "xyz.xenondevs.invui:invui:2.3.0";
    private static final String INVUI_LEGACY = "xyz.xenondevs.invui:invui:2.1.1";
    private static final int[] INVUI_LATEST_MINIMUM_MINECRAFT = {26, 2};

    private static final String[] LIBRARIES = {
            // Cloud v2 command framework
            "org.incendo:cloud-core:2.1.0",
            "org.incendo:cloud-paper:2.0.0",
            "org.incendo:cloud-minecraft-extras:2.0.0",

            // Persistence
            "org.jooq:jooq:3.21.6",
            "org.flywaydb:flyway-core:13.1.0",
            "org.flywaydb:flyway-database-postgresql:13.1.0",
            "org.flywaydb:flyway-mysql:13.1.0",
            "com.zaxxer:HikariCP:7.1.0",

            // JDBC drivers
            "org.postgresql:postgresql:42.7.13",
            "org.mariadb.jdbc:mariadb-java-client:3.5.10",
            "org.xerial:sqlite-jdbc:3.53.2.1",
            "com.h2database:h2:2.4.240",

            // Config & dependency injection
            "de.exlll:configlib-paper:4.8.1",
            "com.google.inject:guice:7.0.0",
            "com.github.ben-manes.caffeine:caffeine:3.2.4",
    };

    @Override
    public void classloader(final PluginClasspathBuilder classpathBuilder) {
        MavenLibraryResolver resolver = new MavenLibraryResolver();

        String invui = invuiCoordinates();
        classpathBuilder.getContext().getLogger().info("Using InvUI {}", invui);
        resolver.addDependency(new Dependency(new DefaultArtifact(invui), null));

        for (String library : LIBRARIES) {
            resolver.addDependency(new Dependency(new DefaultArtifact(library), null));
        }

        resolver.addRepository(new RemoteRepository.Builder(
                "central", "default", MavenLibraryResolver.MAVEN_CENTRAL_DEFAULT_MIRROR).build());
        resolver.addRepository(new RemoteRepository.Builder(
                "xenondevs", "default", "https://repo.xenondevs.xyz/releases").build());

        classpathBuilder.addLibrary(resolver);
    }

    private static String invuiCoordinates() {
        String minecraft = ServerBuildInfo.buildInfo().minecraftVersionId();
        return isAtLeast(minecraft, INVUI_LATEST_MINIMUM_MINECRAFT) ? INVUI_LATEST : INVUI_LEGACY;
    }

    private static boolean isAtLeast(String version, int[] minimum) {
        String[] parts = version.split("\\.");

        for (int index = 0; index < minimum.length; index++) {
            int part = index < parts.length ? parseOrZero(parts[index]) : 0;
            if (part != minimum[index]) {
                return part > minimum[index];
            }
        }

        return true;
    }

    private static int parseOrZero(String part) {
        try {
            return Integer.parseInt(part.split("[^0-9]", 2)[0]);
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }
}
