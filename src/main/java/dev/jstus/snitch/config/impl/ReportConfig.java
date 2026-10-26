package dev.jstus.snitch.config.impl;

import de.exlll.configlib.Comment;
import de.exlll.configlib.Configuration;
import dev.jstus.snitch.config.ConfigurationFile;
import java.time.Duration;
import lombok.Getter;

@Getter
@Configuration
@ConfigurationFile("report.yml")
@SuppressWarnings("FieldMayBeFinal")
public class ReportConfig {

    @Comment("The trust score every player starts with.")
    private double baseTrustScore = 10.0;

    @Comment({
            "",
            "When a report against a player comes in within this many seconds of the previous one,",
            "the case's risk score is multiplied by the succession multiplier."
    })
    private long successionWindowSeconds = 30;
    private double successionMultiplier = 1.2;

    @Comment({"", "Amount added to each reporter's trust score when their report is resolved as positive."})
    private double positiveTrustReward = 1.0;

    @Comment({"", "Factor each reporter's trust score is multiplied by when their report is resolved as negative."})
    private double negativeTrustFactor = 0.8;

    public Duration successionWindow() {
        return Duration.ofSeconds(successionWindowSeconds);
    }
}
