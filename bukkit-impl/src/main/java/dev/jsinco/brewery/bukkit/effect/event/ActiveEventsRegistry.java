package dev.jsinco.brewery.bukkit.effect.event;

import dev.jsinco.brewery.bukkit.TheBrewingProject;
import dev.jsinco.brewery.api.event.NamedDrunkEvent;
import dev.jsinco.brewery.api.util.BreweryKey;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ActiveEventsRegistry {

    private final Map<UUID, Map<BreweryKey, Long>> events = new ConcurrentHashMap<>();

    public boolean hasActiveEvent(UUID playerUuid, NamedDrunkEvent event) {
        Map<BreweryKey, Long> playerEvents = events.get(playerUuid);
        if (playerEvents == null) {
            return false;
        }
        BreweryKey key = event.key();
        Long expiresAt = playerEvents.get(key);
        if (expiresAt == null) {
            return false;
        }
        if (expiresAt > TheBrewingProject.getInstance().getTime()) {
            return true;
        }
        playerEvents.remove(key, expiresAt);
        if (playerEvents.isEmpty()) {
            events.remove(playerUuid, playerEvents);
        }
        return false;
    }

    public void registerActiveEvent(UUID playerUuid, NamedDrunkEvent event, int duration) {
        events.computeIfAbsent(playerUuid, ignored -> new ConcurrentHashMap<>())
                .put(event.key(), TheBrewingProject.getInstance().getTime() + duration);
    }
}
