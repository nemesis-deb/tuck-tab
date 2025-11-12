package com.mobheads.leaderboard;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;

/**
 * Listens for advancement completion events to update mob head counts.
 * When a player completes any mob head collection advancement, their count is refreshed.
 */
public class AchievementListener implements Listener {
    
    private final HeadTracker headTracker;
    
    public AchievementListener(HeadTracker headTracker) {
        this.headTracker = headTracker;
    }
    
    /**
     * Handles advancement completion events.
     * Updates the player's mob head count when they complete a mob head advancement.
     * 
     * @param event The advancement completion event
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onAdvancementDone(PlayerAdvancementDoneEvent event) {
        String key = event.getAdvancement().getKey().toString();
        
        // Check if it's a mob head collection advancement
        if (headTracker.isMobHeadAdvancement(key)) {
            // Update the player's head count (clears cache and recalculates)
            headTracker.updateHeadCount(event.getPlayer().getUniqueId());
        }
    }
}
