package dev.jsinco.brewery.bukkit.effect.named;

import dev.jsinco.brewery.api.event.EventPropertyExecutable;
import dev.jsinco.brewery.api.event.EventStepProperty;
import dev.jsinco.brewery.api.event.NamedDrunkEvent;
import dev.jsinco.brewery.bukkit.TheBrewingProject;
import dev.jsinco.brewery.bukkit.util.SchedulerUtil;
import dev.jsinco.brewery.configuration.EventSection;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class DrunkMessageNamedExecutable implements EventPropertyExecutable {

    @Override
    public @NonNull ExecutionResult execute(UUID contextPlayer, List<EventStepProperty> eventStepProperties) {
        Player player = Bukkit.getPlayer(contextPlayer);
        if (player == null) {
            return ExecutionResult.CONTINUE;
        }

        List<String> drunkMessages = EventSection.events().drunkMessages();
        if (drunkMessages.isEmpty()) {
            return ExecutionResult.CONTINUE;
        }
        SchedulerUtil.runGlobal(() -> {
            List<CompletableFuture<String>> nameFutures = Bukkit.getOnlinePlayers().stream()
                    .filter(other -> !player.equals(other))
                    .map(DrunkMessageNamedExecutable::collectPlayerName)
                    .toList();
            CompletableFuture.allOf(nameFutures.toArray(CompletableFuture[]::new)).thenRun(() -> {
                List<String> onlinePlayerNames = nameFutures.stream()
                        .map(future -> future.getNow(null))
                        .filter(java.util.Objects::nonNull)
                        .toList();
                if (onlinePlayerNames.isEmpty()) {
                    return;
                }
                String randomPlayerName = onlinePlayerNames.get(RANDOM.nextInt(onlinePlayerNames.size()));
                String message = drunkMessages.get(RANDOM.nextInt(drunkMessages.size()))
                        .replace("<random_player_name>", randomPlayerName);
                SchedulerUtil.runForEntity(player, () -> player.chat(message));
            });
        });
        return ExecutionResult.CONTINUE;
    }

    private static CompletableFuture<String> collectPlayerName(Player player) {
        CompletableFuture<String> result = new CompletableFuture<>();
        player.getScheduler().run(
                TheBrewingProject.getInstance(),
                ignored -> result.complete(player.isVisibleByDefault() ? player.getName() : null),
                () -> result.complete(null)
        );
        return result;
    }

    @Override
    public ExecutionContext context() {
        return ExecutionContext.PLAYER;
    }

    @Override
    public EventStepProperty toProperty() {
        return NamedDrunkEvent.fromKey("drunk_message");
    }

    @Override
    public int priority() {
        return -1;
    }

    @Override
    public EventPropertyExecutable withSkipPoint(@Nullable EventPropertyExecutable point) {
        return this; // NO-OP
    }
}
