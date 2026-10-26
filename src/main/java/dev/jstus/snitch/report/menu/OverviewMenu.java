package dev.jstus.snitch.report.menu;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import dev.jstus.snitch.Snitch;
import dev.jstus.snitch.profile.ProfileCache;
import dev.jstus.snitch.report.OpenCaseSummary;
import dev.jstus.snitch.report.ReportCase;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ResolvableProfile;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.item.Item;
import xyz.xenondevs.invui.item.ItemBuilder;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.window.Window;

@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class OverviewMenu {

    private static final int WIDTH = 9;
    private static final int HEIGHT = 5;
    private static final String UNKNOWN_USERNAME = "Unknown";

    /** Reasons are stored up to VARCHAR(255); anything near that is unreadable as lore. */
    private static final int MAX_REASON_LENGTH = 48;

    private final ProfileCache profiles;
    private final Snitch plugin;

    public CompletableFuture<Void> open(Player viewer, List<OpenCaseSummary> cases) {
        List<OpenCaseSummary> visible = cases.stream().limit(WIDTH * HEIGHT).toList();
        List<UUID> reported = visible.stream().map(summary -> summary.reportCase().reported()).toList();

        return profiles.usernames(reported).thenAccept(usernames ->
                viewer.getScheduler().run(plugin,
                        task -> createWindow(viewer, visible, usernames).open(),
                        null));
    }

    private Window createWindow(Player viewer, List<OpenCaseSummary> cases, Map<UUID, String> usernames) {
        return Window.builder()
                .setTitle(Component.text("Open Cases"))
                .setUpperGui(createGui(cases, usernames))
                .build(viewer);
    }

    private Gui createGui(List<OpenCaseSummary> cases, Map<UUID, String> usernames) {
        Gui gui = Gui.empty(WIDTH, HEIGHT);

        for (int slot = 0; slot < cases.size(); slot++) {
            gui.setItem(slot, Item.simple(caseItem(cases.get(slot), usernames)));
        }

        return gui;
    }

    private ItemProvider caseItem(OpenCaseSummary summary, Map<UUID, String> usernames) {
        ReportCase reportCase = summary.reportCase();
        String name = usernames.getOrDefault(reportCase.reported(), UNKNOWN_USERNAME);

        ItemStack skull = ItemStack.of(Material.PLAYER_HEAD);
        skull.setData(DataComponentTypes.PROFILE,
                ResolvableProfile.resolvableProfile().uuid(reportCase.reported()));

        List<Component> lore = new ArrayList<>(List.of(
                labelValueLoreLine("Username: ", name),
                labelValueLoreLine("UUID: ", reportCase.reported().toString()),
                labelValueLoreLine("Risk score: ", String.format(Locale.ROOT, "%.1f", reportCase.riskScore())),
                labelValueLoreLine("Reported by: ", summary.reporters() + " player" + (summary.reporters() == 1 ? "" : "s"))));

        if (!summary.reasons().isEmpty()) {
            lore.add(Component.empty());
            lore.add(noItalics(Component.text("Recent reasons:", NamedTextColor.GRAY)));
            summary.reasons().forEach(reason -> lore.add(reasonLine(reason)));
        }

        return new ItemBuilder(skull)
                .setName(noItalics(Component.text(name, NamedTextColor.RED)))
                .setLore(lore);
    }

    private Component noItalics(Component component) {
        return component.decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE);
    }

    private Component labelValueLoreLine(String label, String value) {
        return noItalics(Component.text(label, NamedTextColor.GRAY)
                .append(Component.text(value, NamedTextColor.WHITE)));
    }

    private Component reasonLine(String reason) {
        String text = reason.length() > MAX_REASON_LENGTH
                ? reason.substring(0, MAX_REASON_LENGTH - 1).stripTrailing() + "…"
                : reason;

        return noItalics(Component.text(" • ", NamedTextColor.DARK_GRAY)
                .append(Component.text(text, NamedTextColor.WHITE)));
    }
}
