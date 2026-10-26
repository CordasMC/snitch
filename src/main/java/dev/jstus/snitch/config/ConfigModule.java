package dev.jstus.snitch.config;

import com.google.inject.AbstractModule;
import java.nio.file.Path;

public final class ConfigModule extends AbstractModule {

    private final Class<?> anchor;
    private final Path dataDirectory;

    public ConfigModule(Class<?> anchor, Path dataDirectory) {
        this.anchor = anchor;
        this.dataDirectory = dataDirectory;
    }

    @Override
    protected void configure() {
        ConfigStore store = new ConfigStore(new ConfigClassFinder(anchor), dataDirectory);
        bind(ConfigStore.class).toInstance(store);
        store.types().forEach(type -> bindConfig(type, store));
    }

    private <T> void bindConfig(Class<T> type, ConfigStore store) {
        bind(type).toProvider(() -> store.get(type));
    }
}
