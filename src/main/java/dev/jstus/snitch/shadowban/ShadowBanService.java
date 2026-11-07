package dev.jstus.snitch.shadowban;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface ShadowBanService {

    CompletableFuture<ShadowResponse> handleRequest(ShadowRequest request);

    CompletableFuture<Boolean> isShadowBanned(UUID target);
}
