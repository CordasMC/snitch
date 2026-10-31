package dev.jstus.snitch.config.impl;

import de.exlll.configlib.Comment;
import de.exlll.configlib.Configuration;
import dev.jstus.snitch.config.ConfigurationFile;
import lombok.Getter;

import java.time.Duration;

@Getter
@Configuration
@ConfigurationFile("altdetection.yml")
public class AltDetectionConfig {

    @Comment("How should we detect alt accounts? Options:" +
            "PLAYTIME: Accept/Deny reports based on the playtime of the reporter" +
            "LITEBANS: Use the litebans API to query for known alt accounts")
    private DetectionMethod detectionMethod = DetectionMethod.PLAYTIME;

    @Comment("Minimum playtime of the reporter for their report to be accepted")
    private long minimumPlaytime = 3600000;

    public Duration getMinimumPlaytime() {
        return Duration.ofMillis(minimumPlaytime);
    }

    public enum DetectionMethod {
        PLAYTIME,
        LITEBANS
    }
}
