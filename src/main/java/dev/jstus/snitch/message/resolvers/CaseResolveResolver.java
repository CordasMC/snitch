package dev.jstus.snitch.message.resolvers;

import dev.jstus.snitch.command.snitch.impl.SnitchResolveCaseCommand;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.Context;
import net.kyori.adventure.text.minimessage.ParsingException;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@RequiredArgsConstructor
public class CaseResolveResolver implements TagResolver {

    private final String staff;
    private final String target;
    private final SnitchResolveCaseCommand.Verdict verdict;

    @Override
    public @Nullable Tag resolve(@NotNull String name, @NotNull ArgumentQueue arguments, @NotNull Context ctx) throws ParsingException {
        return switch (name) {
            case "staff" -> Tag.inserting(Component.text(staff));
            case "target" -> Tag.inserting(Component.text(target));
            case "verdict" -> Tag.inserting(Component.text(verdict.toString()));
            default -> throw new IllegalStateException("CaseResolveResolver only supports placeholders <staff>, <target> and <verdict>. Received instead: " + name);
        };
    }

    @Override
    public boolean has(@NotNull String name) {
        return switch (name) {
            case "staff" -> true;
            case "target" -> true;
            case "verdict" -> true;
            default -> false;
        };
    }
}
