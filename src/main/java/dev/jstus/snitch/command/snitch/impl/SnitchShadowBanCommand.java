package dev.jstus.snitch.command.snitch.impl;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import dev.jstus.snitch.command.suggestion.OnlinePlayerSuggestions;
import dev.jstus.snitch.config.impl.MessageConfig;
import dev.jstus.snitch.message.resolvers.ShadowBanResolver;
import dev.jstus.snitch.profile.ProfileCache;
import dev.jstus.snitch.shadowban.ShadowAction;
import dev.jstus.snitch.shadowban.ShadowBanService;
import dev.jstus.snitch.shadowban.ShadowRequest;
import dev.jstus.snitch.shadowban.ShadowResponse;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.cloud.Command;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.parser.standard.EnumParser;
import org.incendo.cloud.parser.standard.StringParser;

@Singleton
public class SnitchShadowBanCommand {

    private final ShadowBanService shadowBans;
    private final ProfileCache profiles;
    private final MiniMessage mm;
    private final MessageConfig msg;

    @Inject
    public SnitchShadowBanCommand(ShadowBanService shadowBans, ProfileCache profiles, MiniMessage mm, MessageConfig msg) {
        this.shadowBans = shadowBans;
        this.profiles = profiles;
        this.mm = mm;
        this.msg = msg;
    }

    public Command.Builder<CommandSourceStack> build(Command.Builder<CommandSourceStack> root) {
        return root.literal("shadowban")
                .permission("snitch.shadowban")
                .required("action", EnumParser.enumParser(Action.class))
                .required("player", StringParser.stringParser(), new OnlinePlayerSuggestions())
                .futureHandler(this::handle);
    }

    private CompletableFuture<Void> handle(CommandContext<CommandSourceStack> context) {
        CommandSender sender = context.sender().getSender();
        Action action = context.get("action");
        String targetName = context.get("player");
        ShadowBanResolver tagResolver = new ShadowBanResolver(targetName);

        return profiles.uuid(targetName).thenCompose(target -> {
            if (target == null) {
                sender.sendMessage(mm.deserialize(msg.getShadowBanTargetNotFound(), tagResolver));
                return CompletableFuture.completedFuture(null);
            }

            Optional<UUID> issuer = sender instanceof Player player ? Optional.of(player.getUniqueId()) : Optional.empty();
            ShadowRequest request = new ShadowRequest(action.shadowAction(), target, issuer, System.currentTimeMillis());

            return shadowBans.handleRequest(request)
                    .thenAccept(response -> notifyResult(response, sender, tagResolver));
        });
    }

    private void notifyResult(ShadowResponse response, CommandSender sender, ShadowBanResolver tagResolver) {
        String message = switch (response.status()) {
            case SUCCESS -> response.action() == ShadowAction.BAN ? msg.getShadowBanAdded() : msg.getShadowBanRemoved();
            case ALREADY_BANNED -> msg.getShadowBanAlreadyBanned();
            case NOT_BANNED -> msg.getShadowBanNotBanned();
            case FAILURE -> msg.getShadowBanError();
        };

        sender.sendMessage(mm.deserialize(message, tagResolver));
    }

    public enum Action {
        ADD,
        REMOVE;

        public ShadowAction shadowAction() {
            return this == ADD ? ShadowAction.BAN : ShadowAction.UNBAN;
        }
    }
}
