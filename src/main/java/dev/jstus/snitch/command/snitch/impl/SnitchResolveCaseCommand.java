package dev.jstus.snitch.command.snitch.impl;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import dev.jstus.snitch.command.suggestion.OnlinePlayerSuggestions;
import dev.jstus.snitch.config.impl.MessageConfig;
import dev.jstus.snitch.message.resolvers.CaseResolveResolver;
import dev.jstus.snitch.profile.ProfileCache;
import dev.jstus.snitch.report.ReportService;
import dev.jstus.snitch.report.Resolution;
import dev.jstus.snitch.report.ResolveResult;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.minimessage.MiniMessage;
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
    private final MiniMessage mm;
    private final MessageConfig msg;

    public Command.Builder<CommandSourceStack> build(Command.Builder<CommandSourceStack> root) {
        return root.literal("resolvecase")
                .permission("snitch.resolvecase")
                .required("player", StringParser.stringParser(), new OnlinePlayerSuggestions())
                .required("verdict", EnumParser.enumParser(Verdict.class))
                .futureHandler(this::handle);
    }

    private CompletableFuture<Void> handle(CommandContext<CommandSourceStack> context) {
        if (!(context.sender().getExecutor() instanceof Player staff)) {
            context.sender().getSender().sendMessage(mm.deserialize(msg.getResolveCaseOnlyPlayers()));
            return CompletableFuture.completedFuture(null);
        }

        String targetName = context.get("player");
        Verdict verdict = context.get("verdict");

        return profiles.uuid(targetName).thenCompose(target -> {
            if (target == null) {
                staff.sendMessage(mm.deserialize(msg.getResolveCaseUnknownPlayer(), new CaseResolveResolver(staff.getName(), targetName, verdict)));
                return CompletableFuture.completedFuture(null);
            }

            return reports.resolve(target, verdict.resolution(), staff.getUniqueId())
                    .thenAccept(result -> notifyResult(result, staff, new CaseResolveResolver(staff.getName(), targetName, verdict)));
        });
    }

    private void notifyResult(ResolveResult result, Player staff, CaseResolveResolver tagResolver) {
        switch (result.outcome()) {
            case RESOLVED -> staff.sendMessage(mm.deserialize(msg.getResolveCaseResolved(), tagResolver));
            case NO_OPEN_CASE -> staff.sendMessage(mm.deserialize(msg.getResolveCaseNoOpenCase(), tagResolver));
            case ERROR -> staff.sendMessage(mm.deserialize(msg.getResolveCaseError(), tagResolver));
        }
    }

    public enum Verdict {
        CHEATING,
        LEGIT;

        public Resolution resolution() {
            return this == CHEATING ? Resolution.POSITIVE : Resolution.NEGATIVE;
        }

        public String toString() {
            return switch (this) {
                case CHEATING -> "Cheating";
                case LEGIT -> "Legit";
            };
        }
    }
}
