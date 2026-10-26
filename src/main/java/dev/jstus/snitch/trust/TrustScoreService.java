package dev.jstus.snitch.trust;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import dev.jstus.snitch.config.impl.ReportConfig;
import dev.jstus.snitch.db.DatabaseExecutor;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

@Singleton
public class TrustScoreService {

    private final TrustScoreRepository trust;
    private final Provider<ReportConfig> config;
    private final ExecutorService executor;

    @Inject
    public TrustScoreService(TrustScoreRepository trust, Provider<ReportConfig> config,
                             @DatabaseExecutor ExecutorService executor) {
        this.trust = trust;
        this.config = config;
        this.executor = executor;
    }

    public CompletableFuture<Double> trustScore(UUID player) {
        ReportConfig config = this.config.get();

        return CompletableFuture.supplyAsync(
                () -> trust.find(player).orElse(config.getBaseTrustScore()), executor);
    }
}
