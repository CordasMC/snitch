package dev.jstus.snitch.profile;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.inject.Singleton;
import java.time.Duration;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jspecify.annotations.Nullable;

@Singleton
public class ProfileCache {

    private static final Duration EXPIRY = Duration.ofMinutes(10);
    private static final long MAXIMUM_SIZE = 1_000;
    private static final int LOOKUP_THREADS = 4;

    private final ExecutorService executor;
    private final AsyncLoadingCache<UUID, Optional<String>> usernames;
    private final AsyncLoadingCache<String, Optional<UUID>> uuids;

    public ProfileCache() {
        this.executor = Executors.newFixedThreadPool(
                LOOKUP_THREADS, Thread.ofPlatform().name("snitch-profile-", 0).daemon(true).factory());

        CacheLoader<UUID, Optional<String>> usernameLoader = this::lookupUsername;
        CacheLoader<String, Optional<UUID>> uuidLoader = this::lookupUuid;

        this.usernames = Caffeine.newBuilder()
                .executor(executor)
                .expireAfterWrite(EXPIRY)
                .maximumSize(MAXIMUM_SIZE)
                .buildAsync(usernameLoader);

        this.uuids = Caffeine.newBuilder()
                .executor(executor)
                .expireAfterWrite(EXPIRY)
                .maximumSize(MAXIMUM_SIZE)
                .buildAsync(uuidLoader);
    }

    public CompletableFuture<Map<UUID, String>> usernames(Collection<UUID> players) {
        return usernames.getAll(players).thenApply(resolved -> {
            Map<UUID, String> names = new HashMap<>(resolved.size());
            resolved.forEach((player, username) -> username.ifPresent(value -> names.put(player, value)));
            return names;
        });
    }

    public CompletableFuture<@Nullable String> username(UUID player) {
        return usernames.get(player).thenApply(username -> username.orElse(null));
    }

    public CompletableFuture<@Nullable UUID> uuid(String username) {
        return uuids.get(key(username)).thenApply(player -> player.orElse(null));
    }

    public void shutdown() {
        executor.shutdownNow();
    }

    private Optional<String> lookupUsername(UUID player) {
        String username = Bukkit.getOfflinePlayer(player).getName();
        if (username != null) {
            uuids.put(key(username), CompletableFuture.completedFuture(Optional.of(player)));
        }
        return Optional.ofNullable(username);
    }

    private Optional<UUID> lookupUuid(String username) {
        OfflinePlayer cached = Bukkit.getOfflinePlayerIfCached(username);
        if (cached != null) {
            return Optional.of(cached.getUniqueId());
        }

        PlayerProfile profile = Bukkit.createProfile(username).update().join();
        return Optional.ofNullable(profile.getId());
    }

    private String key(String username) {
        return username.toLowerCase(Locale.ROOT);
    }
}
