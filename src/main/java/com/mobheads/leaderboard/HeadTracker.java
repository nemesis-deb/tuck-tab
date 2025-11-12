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
        // Passive Mobs
        advancements.add("mobheads:allay");
        advancements.add("mobheads:axolotl");
        advancements.add("mobheads:bat");
        advancements.add("mobheads:camel");
        advancements.add("mobheads:cat");
        advancements.add("mobheads:chicken");
        advancements.add("mobheads:cod");
        advancements.add("mobheads:cow");
        advancements.add("mobheads:donkey");
        advancements.add("mobheads:fox");
        advancements.add("mobheads:frog");
        advancements.add("mobheads:glow_squid");
        advancements.add("mobheads:horse");
        advancements.add("mobheads:mooshroom");
        advancements.add("mobheads:mule");
        advancements.add("mobheads:ocelot");
        advancements.add("mobheads:parrot");
        advancements.add("mobheads:pig");
        advancements.add("mobheads:pufferfish");
        advancements.add("mobheads:rabbit");
        advancements.add("mobheads:salmon");
        advancements.add("mobheads:sheep");
        advancements.add("mobheads:sniffer");
        advancements.add("mobheads:snow_golem");
        advancements.add("mobheads:squid");
        advancements.add("mobheads:strider");
        advancements.add("mobheads:tadpole");
        advancements.add("mobheads:tropical_fish");
        advancements.add("mobheads:turtle");
        advancements.add("mobheads:villager");
        advancements.add("mobheads:wandering_trader");
        
        // Neutral Mobs
        advancements.add("mobheads:bee");
        advancements.add("mobheads:cave_spider");
        advancements.add("mobheads:dolphin");
        advancements.add("mobheads:drowned");
        advancements.add("mobheads:enderman");
        advancements.add("mobheads:goat");
        advancements.add("mobheads:iron_golem");
        advancements.add("mobheads:llama");
        advancements.add("mobheads:panda");
        advancements.add("mobheads:piglin");
        advancements.add("mobheads:polar_bear");
        advancements.add("mobheads:spider");
        advancements.add("mobheads:trader_llama");
        advancements.add("mobheads:wolf");
        advancements.add("mobheads:zombified_piglin");
        
        // Hostile Mobs
        advancements.add("mobheads:blaze");
        advancements.add("mobheads:creeper");
        advancements.add("mobheads:elder_guardian");
        advancements.add("mobheads:endermite");
        advancements.add("mobheads:evoker");
        advancements.add("mobheads:ghast");
        advancements.add("mobheads:guardian");
        advancements.add("mobheads:hoglin");
        advancements.add("mobheads:husk");
        advancements.add("mobheads:magma_cube");
        advancements.add("mobheads:phantom");
        advancements.add("mobheads:piglin_brute");
        advancements.add("mobheads:pillager");
        advancements.add("mobheads:ravager");
        advancements.add("mobheads:shulker");
        advancements.add("mobheads:silverfish");
        advancements.add("mobheads:skeleton");
        advancements.add("mobheads:slime");
        advancements.add("mobheads:stray");
        advancements.add("mobheads:vex");
        advancements.add("mobheads:vindicator");
        advancements.add("mobheads:warden");
        advancements.add("mobheads:witch");
        advancements.add("mobheads:wither_skeleton");
        advancements.add("mobheads:zoglin");
        advancements.add("mobheads:zombie");
        advancements.add("mobheads:zombie_villager");
        
        // Boss Mobs
        advancements.add("mobheads:ender_dragon");
        advancements.add("mobheads:wither");
        
        return advancements;
    }
    
    public boolean isMobHeadAdvancement(String key) {
        return mobHeadAdvancements.contains(key);
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
