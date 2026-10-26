package dev.jstus.snitch.command.report;

import com.google.inject.Inject;
import dev.jstus.snitch.command.CommandClass;
import dev.jstus.snitch.command.SnitchCommand;
import dev.jstus.snitch.command.suggestion.OnlinePlayerSuggestions;
import dev.jstus.snitch.report.ReportResult;
import dev.jstus.snitch.report.ReportService;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.paper.PaperCommandManager;
import org.incendo.cloud.parser.standard.StringParser;

@CommandClass
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class ReportCommand implements SnitchCommand {

    private final ReportService reports;

    @Override
    public void register(PaperCommandManager<CommandSourceStack> manager) {
        manager.command(manager.commandBuilder("reportcheating")
                .permission("snitch.report")
                .required("player", StringParser.stringParser(), new OnlinePlayerSuggestions())
                .optional("reason", StringParser.greedyStringParser())
                .futureHandler(this::handle));
    }

    private CompletableFuture<Void> handle(CommandContext<CommandSourceStack> context) {
        if (!(context.sender().getExecutor() instanceof Player player)) {
            context.sender().getSender().sendMessage(
                    Component.text("Only players can report.", NamedTextColor.RED));
            return CompletableFuture.completedFuture(null);
        }

        String reason = context.<String>optional("reason").orElse(null);
        if (reason != null && reason.length() > 255) {
            context.sender().getSender().sendMessage(Component.text("The provided reason is too long ( chill out there mate )!", NamedTextColor.RED));
            return CompletableFuture.completedFuture(null);
        }

        String name = context.get("player");
        Player target = Bukkit.getPlayerExact(name);
        if (target == null) {
            player.sendMessage(Component.text(name + " is not online.", NamedTextColor.RED));
            return CompletableFuture.completedFuture(null);
        }

        return reports.report(player.getUniqueId(), target.getUniqueId(), reason)
                .thenAccept(result -> notifyResult(player, target, result));
    }

    private void notifyResult(Player player, Player target, ReportResult result) {
        switch (result.outcome()) {
            case REPORTED -> player.sendMessage(
                    Component.text("Reported " + target.getName() + ".", NamedTextColor.GREEN));
            case ALREADY_REPORTED -> player.sendMessage(
                    Component.text("You already reported " + target.getName() + ".", NamedTextColor.RED));
            case SELF -> player.sendMessage(
                    Component.text("You cannot report yourself.", NamedTextColor.RED));
            case ERROR -> player.sendMessage(
                    Component.text("An error occurred submitting the report.", NamedTextColor.RED));
        }
    }
}
