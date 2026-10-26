package dev.jstus.snitch.command;

import dev.jstus.snitch.util.ClassFinder;

public class CommandFinder extends ClassFinder {

    protected CommandFinder(Class<?> anchor) {
        super(anchor);
    }

    @Override
    protected boolean matches(Class<?> clazz) {
        return clazz.isAnnotationPresent(CommandClass.class);
    }
}
