package com.mobheads.leaderboard;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Tracks mob head collection progress for players using the MobHeads datapack.
 * Uses the "mobheads:collection/root" advancement which contains all mob heads as criteria.
 */
public class HeadTracker {
    
    private static final String COLLECTION_ADVANCEMENT = "mobheads:collection/root";
    
    private final MobHeadLeaderboard plugin;
    private final Map<UUID, Integer> headCounts;
    
    public HeadTracker(MobHeadLeaderboard plugin) {
        this.plugin = plugin;
        this.headCounts = new HashMap<>();
    }
    
    /**
     * Checks if an advancement key is related to mob heads collection.
     * 
     * @param key The advancement key to check
     * @return true if it's a mobheads advancement
     */
    public boolean isMobHeadAdvancement(String key) {
        return key.startsWith("mobheads:collection/");
    }
    
    /**
     * Forces an update of a player's head count by clearing cache.
     * 
     * @param uuid The player's UUID
     */
    public void updateHeadCount(UUID uuid) {
        headCounts.remove(uuid);
        OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
        if (player.isOnline()) {
            getHeadCount(uuid);
        }
    }
    
    /**
     * Gets the number of mob heads a player has collected.
     * 
     * @param player The player
     * @return The number of heads collected
     */
    public int getHeadCount(Player player) {
        return getHeadCount(player.getUniqueId());
    }
    
    /**
     * Gets the number of mob heads a player has collected.
     * 
     * @param uuid The player's UUID
     * @return The number of heads collected
     */
    public int getHeadCount(UUID uuid) {
        // Check cache first
        if (headCounts.containsKey(uuid)) {
            return headCounts.get(uuid);
        }
        
        OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
        if (!player.isOnline()) {
            return 0;
        }
        
        return countPlayerHeads(player.getPlayer());
    }
    
    /**
     * Counts the number of mob heads a player has collected by checking
     * the awarded criteria in the collection advancement.
     * 
     * @param player The online player
     * @return The number of heads collected
     */
    private int countPlayerHeads(Player player) {
        if (player == null) {
            return 0;
        }
        
        // Get the main collection advancement
        NamespacedKey key = NamespacedKey.fromString(COLLECTION_ADVANCEMENT);
        if (key == null) {
            plugin.getLogger().warning("Invalid advancement key: " + COLLECTION_ADVANCEMENT);
            return 0;
        }
        
        Advancement collectionAdvancement = Bukkit.getAdvancement(key);
        if (collectionAdvancement == null) {
            plugin.getLogger().warning("MobHeads collection advancement not found. Is the datapack installed?");
            return 0;
        }
        
        AdvancementProgress progress = player.getAdvancementProgress(collectionAdvancement);
        int count = progress.getAwardedCriteria().size();
        
        // Cache the result
        headCounts.put(player.getUniqueId(), count);
        
        return count;
    }
    
    /**
     * Gets the top players by mob head count.
     * 
     * @param limit Maximum number of players to return
     * @return List of player UUIDs and their head counts, sorted by count descending
     */
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
    
    /**
     * Gets a player's rank based on their mob head count.
     * 
     * @param uuid The player's UUID
     * @return The player's rank (1-based), or -1 if not ranked
     */
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
    
    /**
     * Gets the number of heads needed to reach a specific rank.
     * 
     * @param rank The rank to check (1-based)
     * @return The number of heads the player at that rank has, or 0 if rank doesn't exist
     */
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
