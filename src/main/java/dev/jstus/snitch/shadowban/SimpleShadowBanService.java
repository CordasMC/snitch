package dev.jstus.snitch.shadowban;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import dev.jstus.snitch.db.DatabaseExecutor;
import dev.jstus.snitch.db.TransactionalOrchestrator;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import org.jooq.exception.DataAccessException;
import org.slf4j.Logger;

@Singleton
public class SimpleShadowBanService implements ShadowBanService {

    private final ShadowBanRepository bans;
    private final ExecutorService executor;
    private final TransactionalOrchestrator orchestrator;
    private final Logger logger;

    @Inject
    public SimpleShadowBanService(ShadowBanRepository bans, @DatabaseExecutor ExecutorService executor,
                                  TransactionalOrchestrator orchestrator, Logger logger) {
        this.bans = bans;
        this.executor = executor;
        this.orchestrator = orchestrator;
        this.logger = logger;
    }

    @Override
    public CompletableFuture<ShadowResponse> handleRequest(ShadowRequest request) {
        return CompletableFuture.supplyAsync(() -> handleRequestTransactional(request), executor);
    }

    @Override
    public CompletableFuture<Boolean> isShadowBanned(UUID target) {
        return CompletableFuture.supplyAsync(() -> bans.findBan(target).isPresent(), executor);
    }

    private ShadowResponse handleRequestTransactional(ShadowRequest request) {
        ShadowAction action = request.action();
        UUID target = request.target();
        UUID issuer = request.issuer().orElse(null);
        Instant createdAt = Instant.ofEpochMilli(request.createdAt());

        try {
            return orchestrator.orchestrateTransactionally(tx -> {
                ShadowBanRepository bans = this.bans.with(tx);

                boolean applied = switch (action) {
                    case BAN -> bans.insertBan(target, issuer, createdAt);
                    case UNBAN -> bans.deleteBan(target);
                };

                if (!applied) {
                    return new ShadowResponse(action, switch (action) {
                        case BAN -> ShadowResponse.Status.ALREADY_BANNED;
                        case UNBAN -> ShadowResponse.Status.NOT_BANNED;
                    });
                }

                bans.insertAction(target, issuer, action, createdAt);
                return new ShadowResponse(action, ShadowResponse.Status.SUCCESS);
            });
        } catch (DataAccessException exception) {
            logger.error("Failed to apply shadow {} against {}", action, target, exception);
            return new ShadowResponse(action, ShadowResponse.Status.FAILURE);
        }
    }
}
