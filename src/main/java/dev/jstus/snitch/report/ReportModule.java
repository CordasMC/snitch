package dev.jstus.snitch.report;

import com.google.inject.AbstractModule;
import com.google.inject.Provider;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import dev.jstus.snitch.config.impl.AltDetectionConfig;
import dev.jstus.snitch.report.alting.AltDetectionService;
import dev.jstus.snitch.report.alting.LiteBansAltDetectionService;
import dev.jstus.snitch.report.alting.PlaytimeAltDetectionService;
import org.bukkit.Bukkit;
import dev.jstus.snitch.trust.JooqTrustScoreRepository;
import dev.jstus.snitch.trust.TrustScoreRepository;
import dev.jstus.snitch.trust.TrustScoreService;

import java.util.logging.Logger;

public final class ReportModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(ReportService.class);
        bind(TrustScoreService.class);

        bind(TrustScoreRepository.class).to(JooqTrustScoreRepository.class).in(Singleton.class);
        bind(ReportCaseRepository.class).to(JooqReportCaseRepository.class).in(Singleton.class);
        bind(ReportRepository.class).to(JooqReportRepository.class).in(Singleton.class);
    }

    @Provides
    @Singleton
    AltDetectionService altDetectionService(AltDetectionConfig config,
                                            Provider<PlaytimeAltDetectionService> playtime,
                                            Provider<LiteBansAltDetectionService> liteBans,
                                            Provider<Logger> logger) {
        return switch (config.getDetectionMethod()) {
            case PLAYTIME -> playtime.get();
            case LITEBANS -> {
                if (!Bukkit.getPluginManager().isPluginEnabled("LiteBans")) {
                    logger.get().warning("Alt Detection method was LiteBans, but LiteBans is not installed. Falling back to Playtime.");
                    yield playtime.get();
                }
                yield liteBans.get();
            }
        };
    }
}
