package dev.jstus.snitch.message.resolvers;

import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.Context;
import net.kyori.adventure.text.minimessage.ParsingException;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@RequiredArgsConstructor
public class ReportResolver implements TagResolver {

    private final String target;

    @Override
    public @Nullable Tag resolve(@NotNull String name, @NotNull ArgumentQueue arguments, @NotNull Context ctx) throws ParsingException {
        return switch (name) {
            case "target" -> Tag.inserting(Component.text(target));
            default -> throw new IllegalStateException("ReportResolver only supports placeholder <target>. Received instead: " + name);
        };
    }

    @Override
    public boolean has(@NotNull String name) {
        return switch (name) {
            case "target" -> true;
            default -> false;
        };
    }
}
