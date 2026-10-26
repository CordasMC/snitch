package dev.jstus.snitch.config;

import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

public final class ConfigStore {

    private final ConfigClassFinder finder;
    private final Path dataDirectory;
    private volatile Map<Class<?>, Object> configs;

    ConfigStore(ConfigClassFinder finder, Path dataDirectory) {
        this.finder = finder;
        this.dataDirectory = dataDirectory;
        reload();
    }

    public <T> T get(Class<T> type) {
        Object config = configs.get(type);
        if (config == null) {
            throw new IllegalArgumentException(type.getName() + " is not a registered configuration");
        }
        return type.cast(config);
    }

    public Set<Class<?>> types() {
        return configs.keySet();
    }

    public void reload() {
        configs = Map.copyOf(finder.load(dataDirectory));
    }
}
