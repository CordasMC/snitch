package dev.jstus.snitch.command;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import dev.jstus.snitch.Snitch;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.minecraft.extras.MinecraftExceptionHandler;
import org.incendo.cloud.paper.PaperCommandManager;

public class CommandModule extends AbstractModule {

    @Provides
    @Singleton
    PaperCommandManager<CommandSourceStack> commandManager(Snitch plugin, Injector injector) {
        PaperCommandManager<CommandSourceStack> manager = PaperCommandManager.builder()
                .executionCoordinator(ExecutionCoordinator.simpleCoordinator())
                .buildOnEnable(plugin);

        MinecraftExceptionHandler.create(CommandSourceStack::getSender)
                .defaultHandlers()
                .registerTo(manager);

        for (Class<?> clazz : new CommandFinder(Snitch.class).find()) {
            SnitchCommand command = (SnitchCommand) injector.getInstance(clazz);
            command.register(manager);
        }

        return manager;
    }
}
