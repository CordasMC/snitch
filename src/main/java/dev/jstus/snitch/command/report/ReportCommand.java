package dev.jstus.snitch.command.report;

import com.google.inject.Inject;
import dev.jstus.snitch.command.CommandClass;
import dev.jstus.snitch.command.SnitchCommand;
import dev.jstus.snitch.command.suggestion.OnlinePlayerSuggestions;
import dev.jstus.snitch.config.impl.MessageConfig;
import dev.jstus.snitch.message.resolvers.ReportResolver;
import dev.jstus.snitch.report.ReportResult;
import dev.jstus.snitch.report.ReportService;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.paper.PaperCommandManager;
import org.incendo.cloud.parser.standard.StringParser;

@CommandClass
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class ReportCommand implements SnitchCommand {

    private final ReportService reports;
    private final MiniMessage mm;
    private final MessageConfig msg;
    private final Logger logger;

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
            context.sender().getSender().sendMessage(mm.deserialize(msg.getReportOnlyPlayers()));
            return CompletableFuture.completedFuture(null);
        }

        String reason = context.<String>optional("reason").orElse(null);
        if (reason != null && reason.length() > 255) {
            context.sender().getSender().sendMessage(mm.deserialize(msg.getReportReasonTooLong()));
            return CompletableFuture.completedFuture(null);
        }

        String name = context.get("player");
        Player target = Bukkit.getPlayerExact(name);
        if (target == null) {
            player.sendMessage(mm.deserialize(msg.getReportTargetOffline(), new ReportResolver(name)));
            return CompletableFuture.completedFuture(null);
        }

        return reports.report(player.getUniqueId(), target.getUniqueId(), reason)
                .thenAccept(result -> notifyResult(result, player, new ReportResolver(target.getName())));
    }

    private void notifyResult(ReportResult result, Player player, ReportResolver tagResolver) {
        switch (result.outcome()) {
            case REPORTED -> notifyPlayerSuccess(player, tagResolver);
            case SILENT_DENIAL_ALT_PREVENTION -> {
                notifyPlayerSuccess(player, tagResolver);
                logger.info(player.getName() + "'s report was silently ignored because they were detected as an alt-account. See the configuration file as for how to modify this behaviour.");
            }
            case SILENT_DENIAL_SHADOW_BANNED -> {
                notifyPlayerSuccess(player, tagResolver);
                logger.info(player.getName() + "'s report was silently ignored because they are shadow-banned.");
            }
            case ALREADY_REPORTED -> player.sendMessage(mm.deserialize(msg.getReportAlreadyReported(), tagResolver));
            case SELF -> player.sendMessage(mm.deserialize(msg.getReportSelf(), tagResolver));
            case ERROR -> player.sendMessage(mm.deserialize(msg.getReportError(), tagResolver));
        }
    }

    private void notifyPlayerSuccess(Player player, TagResolver resolver) {
        player.sendMessage(mm.deserialize(msg.getReportSuccess(), resolver));
    }
}
