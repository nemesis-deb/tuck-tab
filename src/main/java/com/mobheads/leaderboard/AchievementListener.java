package com.mobheads.leaderboard;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;

public class AchievementListener implements Listener {
    
    private final HeadTracker headTracker;
    
    public AchievementListener(HeadTracker headTracker) {
        this.headTracker = headTracker;
    }
    
    @EventHandler
    public void onAdvancementDone(PlayerAdvancementDoneEvent event) {
        String key = event.getAdvancement().getKey().toString();
        
        // Check if it's a mob head advancement
        if (key.startsWith("mobheads:")) {
            // Update the player's count
            headTracker.getHeadCount(event.getPlayer());
        }
    }
}
