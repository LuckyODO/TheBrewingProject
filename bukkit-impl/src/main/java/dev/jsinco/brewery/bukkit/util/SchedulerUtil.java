package dev.jsinco.brewery.bukkit.util;

import dev.jsinco.brewery.bukkit.TheBrewingProject;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;

import java.util.concurrent.CompletableFuture;

/**
 * Small ownership-aware scheduling helpers for Paper/Folia compatible code.
 */
public final class SchedulerUtil {

    private SchedulerUtil() {
    }

    public static CompletableFuture<Void> runForEntity(Entity entity, Runnable action) {
        if (Bukkit.isOwnedByCurrentRegion(entity)) {
            return runNow(action);
        }
        CompletableFuture<Void> result = new CompletableFuture<>();
        entity.getScheduler().run(
                TheBrewingProject.getInstance(),
                ignored -> complete(action, result),
                () -> result.complete(null)
        );
        return result;
    }

    public static CompletableFuture<Void> runForLocation(Location location, Runnable action) {
        if (Bukkit.isOwnedByCurrentRegion(location)) {
            return runNow(action);
        }
        CompletableFuture<Void> result = new CompletableFuture<>();
        Bukkit.getRegionScheduler().run(
                TheBrewingProject.getInstance(),
                location,
                ignored -> complete(action, result)
        );
        return result;
    }

    public static CompletableFuture<Void> runGlobal(Runnable action) {
        CompletableFuture<Void> result = new CompletableFuture<>();
        Bukkit.getGlobalRegionScheduler().run(
                TheBrewingProject.getInstance(),
                ignored -> complete(action, result)
        );
        return result;
    }

    public static CompletableFuture<Void> runForSender(CommandSender sender, Runnable action) {
        if (sender instanceof Entity entity) {
            return runForEntity(entity, action);
        }
        if (sender instanceof BlockCommandSender blockCommandSender) {
            return runForLocation(blockCommandSender.getBlock().getLocation(), action);
        }
        return runGlobal(action);
    }

    private static CompletableFuture<Void> runNow(Runnable action) {
        CompletableFuture<Void> result = new CompletableFuture<>();
        complete(action, result);
        return result;
    }

    private static void complete(Runnable action, CompletableFuture<Void> result) {
        try {
            action.run();
            result.complete(null);
        } catch (RuntimeException | Error throwable) {
            result.completeExceptionally(throwable);
            throw throwable;
        }
    }
}
