package com.hologram.stats;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;

import java.util.ArrayList;
import java.util.List;

public class HologramInstance {

    private final Player viewer;
    private Player target;
    private final List<TextDisplay> displayEntities = new ArrayList<>();
    private final String[] rawLines;
    private static final float LINE_HEIGHT = 0.25f;
    private static final double HOLOGRAM_OFFSET_Y = 0.5;

    public HologramInstance(Player viewer, Player target, String[] lines) {
        this.viewer = viewer;
        this.target = target;
        this.rawLines = lines;
        spawn();
    }

    private void spawn() {
        Location baseLocation = getHologramLocation();
        if (baseLocation == null) {
            return;
        }

        // Calculate starting height (center the hologram)
        float totalHeight = (rawLines.length - 1) * LINE_HEIGHT;
        float startY = (float) (baseLocation.getY() + HOLOGRAM_OFFSET_Y + (totalHeight / 2));

        for (int i = 0; i < rawLines.length; i++) {
            Location loc = baseLocation.clone().add(0, startY - (i * LINE_HEIGHT), 0);
            String processedText = processPlaceholders(rawLines[i]);
            TextDisplay display = spawnTextDisplay(loc, processedText);
            if (display != null) {
                displayEntities.add(display);
            }
        }
    }

    private String processPlaceholders(String text) {
        if (target == null) {
            return text;
        }

        // Replace %player_name% with actual player name first
        text = text.replace("%player_name%", target.getName());

        // Process with PlaceholderAPI
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            text = PlaceholderAPI.setPlaceholders(target, text);
        }

        // Convert Minecraft color codes (&) to section symbol (§)
        text = ChatColorConverter.convert(text);

        return text;
    }

    private TextDisplay spawnTextDisplay(Location location, String text) {
        try {
            TextDisplay display = location.getWorld().spawn(location, TextDisplay.class, entity -> {
                // Set text
                entity.setText(text);

                // Make it billboard (always face player)
                entity.setBillboard(TextDisplay.Billboard.CENTER);

                // Make it invisible to all except the viewer
                entity.setDefaultBackground(false);
                entity.setShadowed(false);
                entity.setSeeThrough(true);
                entity.setLineWidth(200);

                // Set rotation to face the viewer initially
                Location viewerLoc = viewer.getLocation();
                entity.setRotation(
                        (float) Math.toDegrees(Math.atan2(viewerLoc.getX() - location.getX(), viewerLoc.getZ() - location.getZ())),
                        (float) Math.toDegrees(Math.atan2(viewerLoc.getY() - location.getY(), Math.sqrt(Math.pow(viewerLoc.getX() - location.getX(), 2) + Math.pow(viewerLoc.getZ() - location.getZ(), 2))))
                );
            });

            // Note: Per-player visibility via removeViewer() requires Paper 1.21+ with Java 21
            // For Spigot 1.20.6 compatibility, the hologram will be visible to all nearby players
            // The seeThrough and defaultBackground settings still provide good visibility

            return display;
        } catch (Exception e) {
            PlayerStatsHologramPlugin.getInstance().getLogger().warning("Failed to spawn TextDisplay: " + e.getMessage());
            return null;
        }
    }

    private Location getHologramLocation() {
        if (target == null || !target.isOnline()) {
            return null;
        }
        Location targetLoc = target.getLocation();
        return targetLoc.clone().add(0, 1.8, 0); // Position above player head
    }

    public void updatePosition() {
        if (target == null || !target.isOnline()) {
            hide();
            return;
        }

        Location baseLocation = getHologramLocation();
        if (baseLocation == null) {
            return;
        }

        // Calculate starting height (center the hologram)
        float totalHeight = (rawLines.length - 1) * LINE_HEIGHT;
        float startY = (float) (baseLocation.getY() + HOLOGRAM_OFFSET_Y + (totalHeight / 2));

        for (int i = 0; i < displayEntities.size() && i < rawLines.length; i++) {
            TextDisplay display = displayEntities.get(i);
            if (display != null && !display.isDead()) {
                Location newLoc = baseLocation.clone().add(0, startY - (i * LINE_HEIGHT), 0);
                display.teleport(newLoc);

                // Update rotation to face viewer
                Location viewerLoc = viewer.getLocation();
                display.setRotation(
                        (float) Math.toDegrees(Math.atan2(viewerLoc.getX() - newLoc.getX(), viewerLoc.getZ() - newLoc.getZ())),
                        (float) Math.toDegrees(Math.atan2(viewerLoc.getY() - newLoc.getY(), Math.sqrt(Math.pow(viewerLoc.getX() - newLoc.getX(), 2) + Math.pow(viewerLoc.getZ() - newLoc.getZ(), 2))))
                );

                // Update text with fresh placeholders
                String processedText = processPlaceholders(rawLines[i]);
                display.setText(processedText);
            }
        }
    }

    public void hide() {
        for (TextDisplay display : displayEntities) {
            if (display != null && !display.isDead()) {
                display.remove();
            }
        }
        displayEntities.clear();
    }

    public Player getTarget() {
        return target;
    }

    public void setTarget(Player target) {
        this.target = target;
    }

    public Player getViewer() {
        return viewer;
    }
}
