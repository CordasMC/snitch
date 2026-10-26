package dev.jstus.snitch.util;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;

public abstract class ClassFinder {

    private static final String CLASS_SUFFIX = ".class";

    private final Class<?> anchor;
    private final String basePackage;

    protected ClassFinder(Class<?> anchor) {
        this(anchor, anchor.getPackageName());
    }

    protected ClassFinder(Class<?> anchor, String basePackage) {
        this.anchor = anchor;
        this.basePackage = basePackage;
    }

    protected abstract boolean matches(Class<?> clazz);

    public List<Class<?>> find() {
        List<Class<?>> found = new ArrayList<>();

        for (String name : classNames()) {
            if (!name.startsWith(basePackage)) {
                continue;
            }

            Class<?> clazz = load(name);
            if (clazz != null && matches(clazz)) {
                found.add(clazz);
            }
        }

        return List.copyOf(found);
    }

    private List<String> classNames() {
        Path source = codeSource();

        try {
            return Files.isDirectory(source)
                    ? classNamesFromDirectory(source)
                    : classNamesFromJar(source);
        } catch (IOException exception) {
            throw new UncheckedIOException("Unable to scan " + source + " for classes", exception);
        }
    }

    private Path codeSource() {
        try {
            return Path.of(anchor.getProtectionDomain().getCodeSource().getLocation().toURI());
        } catch (URISyntaxException exception) {
            throw new IllegalStateException("Unable to locate code source of " + anchor, exception);
        }
    }

    private List<String> classNamesFromJar(Path jar) throws IOException {
        try (JarFile jarFile = new JarFile(jar.toFile())) {
            return jarFile.stream()
                    .map(JarEntry::getName)
                    .filter(name -> name.endsWith(CLASS_SUFFIX))
                    .map(name -> name.substring(0, name.length() - CLASS_SUFFIX.length()).replace('/', '.'))
                    .toList();
        }
    }

    private List<String> classNamesFromDirectory(Path directory) throws IOException {
        try (Stream<Path> files = Files.walk(directory)) {
            return files
                    .map(directory::relativize)
                    .map(Path::toString)
                    .filter(name -> name.endsWith(CLASS_SUFFIX))
                    .map(name -> name.substring(0, name.length() - CLASS_SUFFIX.length())
                            .replace(directory.getFileSystem().getSeparator(), "."))
                    .toList();
        }
    }

    /**
     * Classes are loaded without initialization; classes whose dependencies are absent from the
     * current classpath are skipped rather than failing the scan.
     */
    private Class<?> load(String name) {
        try {
            return Class.forName(name, false, anchor.getClassLoader());
        } catch (ClassNotFoundException | LinkageError ignored) {
            return null;
        }
    }
}
