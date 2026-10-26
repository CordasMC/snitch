package dev.jstus.snitch.command.snitch.impl;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import dev.jstus.snitch.command.suggestion.OnlinePlayerSuggestions;
import dev.jstus.snitch.profile.ProfileCache;
import dev.jstus.snitch.report.ReportService;
import dev.jstus.snitch.report.Resolution;
import dev.jstus.snitch.report.ResolveResult;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.incendo.cloud.Command;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.parser.standard.EnumParser;
import org.incendo.cloud.parser.standard.StringParser;

@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class SnitchResolveCaseCommand {

    private final ReportService reports;
    private final ProfileCache profiles;

    public Command.Builder<CommandSourceStack> build(Command.Builder<CommandSourceStack> root) {
        return root.literal("resolvecase")
                .permission("snitch.resolvecase")
                .required("player", StringParser.stringParser(), new OnlinePlayerSuggestions())
                .required("verdict", EnumParser.enumParser(Verdict.class))
                .futureHandler(this::handle);
    }

    private CompletableFuture<Void> handle(CommandContext<CommandSourceStack> context) {
        if (!(context.sender().getExecutor() instanceof Player staff)) {
            context.sender().getSender().sendMessage(
                    Component.text("Only players can resolve cases.", NamedTextColor.RED));
            return CompletableFuture.completedFuture(null);
        }

        String name = context.get("player");
        Verdict verdict = context.get("verdict");

        return profiles.uuid(name).thenCompose(target -> {
            if (target == null) {
                staff.sendMessage(Component.text("Unknown player " + name + ".", NamedTextColor.RED));
                return CompletableFuture.completedFuture(null);
            }

            return reports.resolve(target, verdict.resolution(), staff.getUniqueId())
                    .thenAccept(result -> notifyResult(staff, name, verdict, result));
        });
    }

    private void notifyResult(Player staff, String targetName, Verdict verdict, ResolveResult result) {
        switch (result.outcome()) {
            case RESOLVED -> staff.sendMessage(Component.text(
                    "Resolved the case against " + targetName + " as " + verdict.name().toLowerCase()
                            + ", adjusted the trust of " + result.reporters()
                            + (result.reporters() == 1 ? " reporter." : " reporters."),
                    NamedTextColor.GREEN));
            case NO_OPEN_CASE -> staff.sendMessage(Component.text(
                    targetName + " has no open case.", NamedTextColor.RED));
            case ERROR -> staff.sendMessage(Component.text(
                    "An error occurred resolving the case.", NamedTextColor.RED));
        }
    }

    public enum Verdict {
        CHEATING,
        LEGIT;

        public Resolution resolution() {
            return this == CHEATING ? Resolution.POSITIVE : Resolution.NEGATIVE;
        }
    }
}
