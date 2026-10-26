package dev.jstus.snitch.config;

import de.exlll.configlib.YamlConfigurations;
import dev.jstus.snitch.util.ClassFinder;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public final class ConfigClassFinder extends ClassFinder {

    public ConfigClassFinder(Class<?> anchor) {
        super(anchor);
    }

    @Override
    protected boolean matches(Class<?> clazz) {
        return clazz.isAnnotationPresent(ConfigurationFile.class);
    }

    public Map<Class<?>, Object> load(Path dataDirectory) {
        try {
            Files.createDirectories(dataDirectory);
        } catch (IOException exception) {
            throw new UncheckedIOException("Unable to create " + dataDirectory, exception);
        }

        Map<Class<?>, Object> configs = new HashMap<>();

        for (Class<?> type : find()) {
            String fileName = type.getAnnotation(ConfigurationFile.class).value();
            configs.put(type, YamlConfigurations.update(dataDirectory.resolve(fileName), type));
        }

        return configs;
    }
}
