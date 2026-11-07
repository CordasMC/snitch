package dev.jstus.snitch.shadowban;

import java.util.Optional;
import java.util.UUID;

/**
 * This one request serves as both a ban and an unban request to the service, because the parameters for both
 * are identical. Is this bad practice? mehhh
 * @param action Decides whether this request is for a ban or an unban
 * @param target The target of the action
 * @param issuer The person who issued it
 * @param createdAt The time the request was issued at
 */
public record ShadowRequest(ShadowAction action, UUID target, Optional<UUID> issuer, long createdAt) {}