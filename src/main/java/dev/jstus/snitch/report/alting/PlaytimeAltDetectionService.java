package dev.jstus.snitch.report.alting;

import com.google.inject.Inject;
import dev.jstus.snitch.config.impl.AltDetectionConfig;
import dev.jstus.snitch.report.ReportCase;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Statistic;
import org.jspecify.annotations.Nullable;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor(onConstructor_ = @Inject)
public class PlaytimeAltDetectionService implements AltDetectionService {

    private final AltDetectionConfig conf;

    @Override
    public CompletableFuture<ReportAltDetectionVerdict> shouldAllowReport(UUID reporter, @Nullable ReportCase reportCase) {
        int ticks = Bukkit.getOfflinePlayer(reporter).getStatistic(Statistic.PLAY_ONE_MINUTE);
        Duration playTime = Duration.ofMillis(ticks * 50L);

        return CompletableFuture.completedFuture(playTime.compareTo(conf.getMinimumPlaytime()) < 0
                ? ReportAltDetectionVerdict.DENY
                : ReportAltDetectionVerdict.ALLOW);
    }
}
