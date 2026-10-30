package dev.jstus.snitch.config.impl;

import de.exlll.configlib.Comment;
import de.exlll.configlib.Configuration;
import dev.jstus.snitch.config.ConfigurationFile;
import lombok.Getter;

@Getter
@Configuration
@ConfigurationFile("messages.yml")
public class MessageConfig {

    @Comment("Global prefix. All instances of <prefix> in this config will see this injected. Empty -> no prefix")
    private String globalPrefix = "";

    private String reportOnlyPlayers = "<prefix><red>Only players can report.</red>";
    private String reportReasonTooLong = "<prefix><red>The provided reason is too long ( chill out there mate )!</red>";
    private String reportTargetOffline = "<prefix><red><target> is not online.</red>";
    private String reportSuccess = "<prefix><green>Reported <target>.</green>";
    private String reportAlreadyReported = "<prefix><red>You already reported <target>.</red>";
    private String reportSelf = "<prefix><red>You cannot report yourself.</red>";
    private String reportError = "<prefix><red>An error occurred submitting the report.</red>";

    private String resolveCaseOnlyPlayers = "<prefix><red>Only players can resolve cases.</red>";
    private String resolveCaseUnknownPlayer = "<prefix><red>Unknown player <target>.</red>";
    private String resolveCaseResolved = "<prefix><green>Resolved the case against <target> as <verdict>.</green>";
    private String resolveCaseNoOpenCase = "<prefix><red><target> has no open case.</red>";
    private String resolveCaseError = "<prefix><red>An error occurred resolving the case.</red>";
    private String menuOnlyPlayers = "<prefix><red>Only players can open the menu.</red>";
}
