package dev.jstus.snitch.shadowban;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public record ShadowBanProfile(UUID player, Optional<ShadowBan> activeBan, Optional<List<ShadowAction>> actions) {}