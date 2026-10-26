package dev.jstus.snitch.command.snitch.impl;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import dev.jstus.snitch.report.ReportService;
import dev.jstus.snitch.report.menu.OverviewMenu;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.incendo.cloud.Command;
import org.incendo.cloud.context.CommandContext;

@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class SnitchMenuCommand {

    private final ReportService reports;
    private final OverviewMenu menu;

    public Command.Builder<CommandSourceStack> build(Command.Builder<CommandSourceStack> root) {
        return root.literal("menu")
                .permission("snitch.menu")
                .futureHandler(this::handle);
    }

    private CompletableFuture<Void> handle(CommandContext<CommandSourceStack> context) {
        if (!(context.sender().getExecutor() instanceof Player player)) {
            context.sender().getSender().sendMessage(
                    Component.text("Only players can open the menu.", NamedTextColor.RED));
            return CompletableFuture.completedFuture(null);
        }

        return reports.openCaseSummaries().thenCompose(cases -> menu.open(player, cases));
    }
}
