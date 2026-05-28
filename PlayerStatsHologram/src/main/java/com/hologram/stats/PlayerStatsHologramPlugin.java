package com.hologram.stats;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

public class PlayerStatsHologramPlugin extends JavaPlugin {

    private final Map<UUID, HologramInstance> activeHolograms = new HashMap<>();
    private final Map<UUID, Long> crouchTimestamps = new HashMap<>();
    private static final double MAX_DISTANCE = 10.0;
    private static final long DEBOUNCE_DELAY_MS = 150;

    // Hologram lines as specified
    private static final String[] HOLOGRAM_LINES = {
            "&6<b>(!)</b> &eStats of %player_name%",
            "",
            " &f☆ &fRank: &c%changeoutput_equals_input:default_matcher:{vault_rank}_ifmatch:¡Get one!_else:{luckperms_prefix}%",
            " &#B6ADFD☀ &fGems: &#B6ADFD%playerpoints_points_formatted%",
            " &#feb801⛁ &fBalance: &#feb801%vault_eco_balance_formatted%",
            " &#FFEB50📋 &fRankUp: %vault_suffix%",
            " &#ecff00🧪 &fLevel: &#ecff00%clv_player_level% &7(%clv_player_exp_percent%)",
            " &#789FFF🦋 &fFlight: %tempfly_time-formatted%",
            " &#4789FF⛏ &fBlocks: &#4789FF%ajlb_value_statistic_mine_block_alltime%",
            " &#FF3535🗡 &fKills: &#FF3535%ajlb_value_statistic_player_kills_alltime%",
            " &#FFCF53☠ &fDeaths: &#FFCF53%ajlb_value_statistic_deaths_alltime%",
            " &#946FEC⌚ &fPlayTime: &#946FEC%statistic_time_played%"
    };

    @Override
    public void onEnable() {
        if (!Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            getLogger().severe("PlaceholderAPI is required! Disabling plugin.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        // Start the main task to check for crouching players
        new BukkitRunnable() {
            @Override
            public void run() {
                checkCrouchingPlayers();
            }
        }.runTaskTimer(this, 0L, 1L); // Run every tick (20 times per second)

        getLogger().info("PlayerStatsHologram enabled successfully!");
    }

    @Override
    public void onDisable() {
        // Clean up all holograms
        for (HologramInstance hologram : activeHolograms.values()) {
            hologram.hide();
        }
        activeHolograms.clear();
        crouchTimestamps.clear();
        getLogger().info("PlayerStatsHologram disabled. All holograms removed.");
    }

    private void checkCrouchingPlayers() {
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            if (!viewer.isSneaking()) {
                // Viewer is not crouching - remove their hologram if exists
                removeHologram(viewer.getUniqueId());
                crouchTimestamps.remove(viewer.getUniqueId());
                continue;
            }

            // Viewer is crouching - find target
            Player target = getTargetPlayer(viewer);

            if (target == null) {
                // No valid target - remove hologram
                removeHologram(viewer.getUniqueId());
                continue;
            }

            // Check debounce to prevent spam on rapid crouch toggle
            long currentTime = System.currentTimeMillis();
            Long lastCrouchTime = crouchTimestamps.get(viewer.getUniqueId());

            if (lastCrouchTime != null && (currentTime - lastCrouchTime) < DEBOUNCE_DELAY_MS) {
                // Still in debounce period - just update position if hologram exists
                HologramInstance hologram = activeHolograms.get(viewer.getUniqueId());
                if (hologram != null && hologram.getTarget() != null) {
                    hologram.updatePosition();
                }
                continue;
            }

            // Valid crouch with target - create or update hologram
            if (lastCrouchTime == null || (currentTime - lastCrouchTime) >= DEBOUNCE_DELAY_MS) {
                crouchTimestamps.put(viewer.getUniqueId(), currentTime);
            }

            HologramInstance hologram = activeHolograms.get(viewer.getUniqueId());

            if (hologram == null) {
                // Create new hologram
                hologram = new HologramInstance(viewer, target, HOLOGRAM_LINES);
                activeHolograms.put(viewer.getUniqueId(), hologram);
            } else if (!hologram.getTarget().equals(target)) {
                // Target changed - recreate hologram for new target
                hologram.hide();
                hologram = new HologramInstance(viewer, target, HOLOGRAM_LINES);
                activeHolograms.put(viewer.getUniqueId(), hologram);
            } else {
                // Same target - just update position
                hologram.updatePosition();
            }
        }
    }

    private Player getTargetPlayer(Player viewer) {
        Location eyeLocation = viewer.getEyeLocation();
        Vector direction = eyeLocation.getDirection();

        // Raycast to find player within range
        for (Player potentialTarget : Bukkit.getOnlinePlayers()) {
            if (potentialTarget.equals(viewer)) {
                continue; // Skip self
            }

            Location targetLocation = potentialTarget.getLocation();
            double distance = eyeLocation.distance(targetLocation);

            if (distance > MAX_DISTANCE) {
                continue; // Too far
            }

            // Check if viewer is looking at the target
            Vector toTarget = targetLocation.clone().subtract(eyeLocation).toVector();
            toTarget.normalize();

            double dotProduct = direction.dot(toTarget);

            // Higher dot product = more directly looking at target
            // 0.95 is approximately 18 degrees cone
            if (dotProduct > 0.95) {
                // Verify line of sight (simple check - could be enhanced with ray tracing)
                if (viewer.hasLineOfSight(potentialTarget)) {
                    return potentialTarget;
                }
            }
        }

        return null;
    }

    private void removeHologram(UUID viewerUuid) {
        HologramInstance hologram = activeHolograms.remove(viewerUuid);
        if (hologram != null) {
            hologram.hide();
        }
    }

    public static PlayerStatsHologramPlugin getInstance() {
        return PlayerStatsHologramPlugin.getPlugin(PlayerStatsHologramPlugin.class);
    }
}
