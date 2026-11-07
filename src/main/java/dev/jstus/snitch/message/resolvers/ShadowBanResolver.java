package dev.jstus.snitch.message.resolvers;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.Context;
import net.kyori.adventure.text.minimessage.ParsingException;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ShadowBanResolver implements TagResolver {

    private final String target;

    public ShadowBanResolver(String target) {
        this.target = target;
    }

    @Override
    public @Nullable Tag resolve(@NotNull String name, @NotNull ArgumentQueue arguments, @NotNull Context ctx) throws ParsingException {
        return switch (name) {
            case "target" -> Tag.inserting(Component.text(target));
            default -> throw new IllegalStateException("ShadowBanResolver only supports placeholder <target>. Received instead: " + name);
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
