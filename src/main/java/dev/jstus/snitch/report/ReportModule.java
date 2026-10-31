package dev.jstus.snitch.report;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import dev.jstus.snitch.report.alting.AltDetectionService;
import dev.jstus.snitch.report.alting.PlaytimeAltDetectionService;
import dev.jstus.snitch.trust.JooqTrustScoreRepository;
import dev.jstus.snitch.trust.TrustScoreRepository;
import dev.jstus.snitch.trust.TrustScoreService;

public final class ReportModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(ReportService.class);
        bind(TrustScoreService.class);

        bind(TrustScoreRepository.class).to(JooqTrustScoreRepository.class).in(Singleton.class);
        bind(ReportCaseRepository.class).to(JooqReportCaseRepository.class).in(Singleton.class);
        bind(ReportRepository.class).to(JooqReportRepository.class).in(Singleton.class);
        bind(AltDetectionService.class).to(PlaytimeAltDetectionService.class).asEagerSingleton();
    }
}
