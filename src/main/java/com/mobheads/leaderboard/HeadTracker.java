package com.mobheads.leaderboard;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class HeadTracker {
    
    private final MobHeadLeaderboard plugin;
    private final Map<UUID, Integer> headCounts;
    
    public HeadTracker(MobHeadLeaderboard plugin) {
        this.plugin = plugin;
        this.headCounts = new HashMap<>();
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
        
        int count = 0;
        
        // Mob Heads datapack from Modrinth uses "mobheads" namespace
        // List of all mob head advancements from the datapack
        String[] mobHeadAdvancements = {
            // Passive Mobs
            "mobheads:allay", "mobheads:axolotl", "mobheads:bat", "mobheads:camel",
            "mobheads:cat", "mobheads:chicken", "mobheads:cod", "mobheads:cow",
            "mobheads:donkey", "mobheads:fox", "mobheads:frog", "mobheads:glow_squid",
            "mobheads:horse", "mobheads:mooshroom", "mobheads:mule", "mobheads:ocelot",
            "mobheads:parrot", "mobheads:pig", "mobheads:pufferfish", "mobheads:rabbit",
            "mobheads:salmon", "mobheads:sheep", "mobheads:sniffer", "mobheads:snow_golem",
            "mobheads:squid", "mobheads:strider", "mobheads:tadpole", "mobheads:tropical_fish",
            "mobheads:turtle", "mobheads:villager", "mobheads:wandering_trader",
            
            // Neutral Mobs
            "mobheads:bee", "mobheads:cave_spider", "mobheads:dolphin", "mobheads:drowned",
            "mobheads:enderman", "mobheads:goat", "mobheads:iron_golem", "mobheads:llama",
            "mobheads:panda", "mobheads:piglin", "mobheads:polar_bear", "mobheads:spider",
            "mobheads:trader_llama", "mobheads:wolf", "mobheads:zombified_piglin",
            
            // Hostile Mobs
            "mobheads:blaze", "mobheads:creeper", "mobheads:elder_guardian", "mobheads:endermite",
            "mobheads:evoker", "mobheads:ghast", "mobheads:guardian", "mobheads:hoglin",
            "mobheads:husk", "mobheads:magma_cube", "mobheads:phantom", "mobheads:piglin_brute",
            "mobheads:pillager", "mobheads:ravager", "mobheads:shulker", "mobheads:silverfish",
            "mobheads:skeleton", "mobheads:slime", "mobheads:stray", "mobheads:vex",
            "mobheads:vindicator", "mobheads:warden", "mobheads:witch", "mobheads:wither_skeleton",
            "mobheads:zoglin", "mobheads:zombie", "mobheads:zombie_villager",
            
            // Boss Mobs
            "mobheads:ender_dragon", "mobheads:wither"
        };
        
        for (String advancementKey : mobHeadAdvancements) {
            org.bukkit.advancement.Advancement advancement = Bukkit.getAdvancement(
                org.bukkit.NamespacedKey.fromString(advancementKey)
            );
            
            if (advancement != null) {
                org.bukkit.advancement.AdvancementProgress progress = 
                    onlinePlayer.getAdvancementProgress(advancement);
                if (progress.isDone()) {
                    count++;
                }
            }
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
