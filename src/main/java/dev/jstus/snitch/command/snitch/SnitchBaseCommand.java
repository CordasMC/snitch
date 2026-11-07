package dev.jstus.snitch.command.snitch;

import com.google.inject.Inject;
import dev.jstus.snitch.command.CommandClass;
import dev.jstus.snitch.command.SnitchCommand;
import dev.jstus.snitch.command.snitch.impl.SnitchMenuCommand;
import dev.jstus.snitch.command.snitch.impl.SnitchResolveCaseCommand;
import dev.jstus.snitch.command.snitch.impl.SnitchShadowBanCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import lombok.RequiredArgsConstructor;
import org.incendo.cloud.Command;
import org.incendo.cloud.paper.PaperCommandManager;

@CommandClass
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class SnitchBaseCommand implements SnitchCommand {

    private final SnitchMenuCommand menu;
    private final SnitchResolveCaseCommand resolveCase;
    private final SnitchShadowBanCommand shadowBan;

    @Override
    public void register(PaperCommandManager<CommandSourceStack> manager) {
        Command.Builder<CommandSourceStack> root = manager.commandBuilder("snitch").permission("snitch.command");

        manager.command(menu.build(root));
        manager.command(resolveCase.build(root));
        manager.command(shadowBan.build(root));
    }
}
