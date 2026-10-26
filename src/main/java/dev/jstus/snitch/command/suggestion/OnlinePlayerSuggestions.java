package dev.jstus.snitch.command.suggestion;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.Bukkit;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.suggestion.BlockingSuggestionProvider;
import org.incendo.cloud.suggestion.Suggestion;

public final class OnlinePlayerSuggestions implements BlockingSuggestionProvider<CommandSourceStack> {

    @Override
    public Iterable<? extends Suggestion> suggestions(CommandContext<CommandSourceStack> context, CommandInput input) {
        return Bukkit.getOnlinePlayers().stream()
                .map(player -> Suggestion.suggestion(player.getName()))
                .toList();
    }
}
