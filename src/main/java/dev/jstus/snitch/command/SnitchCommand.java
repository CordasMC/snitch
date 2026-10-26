package dev.jstus.snitch.command;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.incendo.cloud.paper.PaperCommandManager;

public interface SnitchCommand {

    void register(PaperCommandManager<CommandSourceStack> manager);
}
