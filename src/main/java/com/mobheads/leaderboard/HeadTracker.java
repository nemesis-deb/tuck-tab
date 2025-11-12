package com.mobheads.leaderboard;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class HeadTracker {
    
    private final MobHeadLeaderboard plugin;
    private final Map<UUID, Integer> headCounts;
    private final Set<String> mobHeadAdvancements;
    
    public HeadTracker(MobHeadLeaderboard plugin) {
        this.plugin = plugin;
        this.headCounts = new HashMap<>();
        this.mobHeadAdvancements = initializeMobHeadAdvancements();
    }
    
    private Set<String> initializeMobHeadAdvancements() {
        Set<String> advancements = new HashSet<>();
        // We only need to track the collection advancement now
        advancements.add("mobheads:collection");
        return advancements;
    }
    
    public boolean isMobHeadAdvancement(String key) {
        // Check if it's any mobheads advancement (they all update the collection)
        return key.startsWith("mobheads:");
    }
    
    public void updateHeadCount(UUID uuid) {
        // Force recalculation
        headCounts.remove(uuid);
        OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
        if (player.isOnline()) {
            getHeadCount(uuid);
        }
    }
    
    public int getHeadCount(Player player) {
        return getHeadCount(player.getUniqueId());
    }
    
    public int getHeadCount(UUID uuid) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
        if (player.hasPlayedBefore() || player.isOnline()) {
            return countPlayerAdvancements(player);
        }
        return 0;
    }
    
    private int countPlayerAdvancements(OfflinePlayer player) {
        if (!player.isOnline()) {
            return headCounts.getOrDefault(player.getUniqueId(), 0);
        }
        
        Player onlinePlayer = player.getPlayer();
        if (onlinePlayer == null) return 0;
        
        // Use the "MobHeads Collection" advancement which tracks total heads collected
        org.bukkit.advancement.Advancement collectionAdvancement = Bukkit.getAdvancement(
            org.bukkit.NamespacedKey.fromString("mobheads:collection")
        );
        
        int count = 0;
        
        if (collectionAdvancement != null) {
            org.bukkit.advancement.AdvancementProgress progress = 
                onlinePlayer.getAdvancementProgress(collectionAdvancement);
            
            // Get the number of completed criteria (each head is a criterion)
            count = progress.getAwardedCriteria().size();
        }
        
        headCounts.put(player.getUniqueId(), count);
        return count;
    }
    
    public List<Map.Entry<UUID, Integer>> getTopPlayers(int limit) {
        // Update counts for all online players
        for (Player player : Bukkit.getOnlinePlayers()) {
            getHeadCount(player);
        }
        
        return headCounts.entrySet().stream()
            .sorted(Map.Entry.<UUID, Integer>comparingByValue().reversed())
            .limit(limit)
            .collect(Collectors.toList());
    }
    
    public int getPlayerRank(UUID uuid) {
        List<Map.Entry<UUID, Integer>> sorted = headCounts.entrySet().stream()
            .sorted(Map.Entry.<UUID, Integer>comparingByValue().reversed())
            .collect(Collectors.toList());
        
        for (int i = 0; i < sorted.size(); i++) {
            if (sorted.get(i).getKey().equals(uuid)) {
                return i + 1;
            }
        }
        return -1;
    }
    
    public int getHeadsForRank(int rank) {
        List<Map.Entry<UUID, Integer>> sorted = headCounts.entrySet().stream()
            .sorted(Map.Entry.<UUID, Integer>comparingByValue().reversed())
            .collect(Collectors.toList());
        
        if (rank > 0 && rank <= sorted.size()) {
            return sorted.get(rank - 1).getValue();
        }
        return 0;
    }
}
