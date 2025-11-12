package com.mobheads.leaderboard;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TabListManager {
    
    private final MobHeadLeaderboard plugin;
    private final HeadTracker headTracker;
    private final SkillsAPIBridge skillsAPI;
    private BukkitTask updateTask;
    private int animationFrame = 0;
    private final DecimalFormat decimalFormat = new DecimalFormat("#.##");
    
    public TabListManager(MobHeadLeaderboard plugin, HeadTracker headTracker, SkillsAPIBridge skillsAPI) {
        this.plugin = plugin;
        this.headTracker = headTracker;
        this.skillsAPI = skillsAPI;
    }
    
    public void startUpdating() {
        int interval = plugin.getConfig().getInt("update-interval", 40);
        
        updateTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            animationFrame++;
            for (Player player : Bukkit.getOnlinePlayers()) {
                updateTabList(player);
            }
        }, 20L, interval);
    }
    
    public void stopUpdating() {
        if (updateTask != null) {
            updateTask.cancel();
        }
    }
    
    private void updateTabList(Player player) {
        String header = buildHeader(player);
        String footer = buildFooter(player);
        
        player.setPlayerListHeaderFooter(header, footer);
    }
    
    private String buildHeader(Player viewer) {
        StringBuilder header = new StringBuilder();
        
        // Animated title
        if (plugin.getConfig().getBoolean("display.animated-title", true)) {
            header.append(getAnimatedTitle()).append("\n");
        } else {
            String title = plugin.getConfig().getString("display.title", "&6&l⚔ Mob Head Achievements ⚔");
            header.append(color(title)).append("\n");
        }
        
        header.append("\n");
        
        // Server info section
        if (plugin.getConfig().getBoolean("display.show-server-info", true)) {
            header.append(buildServerInfo()).append("\n");
        }
        
        // Player stats section
        if (plugin.getConfig().getBoolean("display.show-player-stats", true)) {
            header.append(buildPlayerStats(viewer)).append("\n");
        }
        
        return header.toString();
    }
    
    private String buildServerInfo() {
        StringBuilder info = new StringBuilder();
        
        String separator = color(plugin.getConfig().getString("display.separator", "&8&m━━━━━━━━━━━━━━━━━━━━━━━━━━━━"));
        info.append(separator).append("\n");
        
        // Online players
        int online = Bukkit.getOnlinePlayers().size();
        int max = Bukkit.getMaxPlayers();
        String onlineFormat = plugin.getConfig().getString("display.server-info.online-players", 
            "&7Players: &a{online}&7/&a{max}");
        info.append(color(onlineFormat
            .replace("{online}", String.valueOf(online))
            .replace("{max}", String.valueOf(max)))).append("\n");
        
        // Server TPS (if enabled)
        if (plugin.getConfig().getBoolean("display.server-info.show-tps", true)) {
            double tps = getServerTPS();
            String tpsColor = tps >= 19.0 ? "&a" : tps >= 17.0 ? "&e" : "&c";
            String tpsFormat = plugin.getConfig().getString("display.server-info.tps", 
                "&7TPS: {tps_color}{tps}");
            info.append(color(tpsFormat
                .replace("{tps_color}", tpsColor)
                .replace("{tps}", decimalFormat.format(tps)))).append("\n");
        }
        
        // Current time (if enabled)
        if (plugin.getConfig().getBoolean("display.server-info.show-time", false)) {
            String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
            String timeFormat = plugin.getConfig().getString("display.server-info.time", 
                "&7Time: &f{time}");
            info.append(color(timeFormat.replace("{time}", time))).append("\n");
        }
        
        info.append(separator);
        
        return info.toString();
    }
    
    private String buildPlayerStats(Player viewer) {
        StringBuilder stats = new StringBuilder();
        
        stats.append("\n");
        String statsTitle = plugin.getConfig().getString("display.player-stats.title", "&b&lYour Stats");
        stats.append(color(statsTitle)).append("\n");
        
        // Player's head count
        int headCount = headTracker.getHeadCount(viewer.getUniqueId());
        String headFormat = plugin.getConfig().getString("display.player-stats.heads", 
            "&7Mob Heads: &e{count}");
        stats.append(color(headFormat.replace("{count}", String.valueOf(headCount)))).append("\n");
        
        // Player's rank
        int rank = headTracker.getPlayerRank(viewer.getUniqueId());
        String rankFormat = plugin.getConfig().getString("display.player-stats.rank", 
            "&7Rank: &6#{rank}");
        stats.append(color(rankFormat.replace("{rank}", rank > 0 ? String.valueOf(rank) : "Unranked"))).append("\n");
        
        // Progress to next rank (if not #1)
        if (rank > 1) {
            int nextRankHeads = headTracker.getHeadsForRank(rank - 1);
            int needed = nextRankHeads - headCount;
            if (needed > 0) {
                String progressFormat = plugin.getConfig().getString("display.player-stats.progress", 
                    "&7Next Rank: &c{needed} more");
                stats.append(color(progressFormat.replace("{needed}", String.valueOf(needed)))).append("\n");
            }
        }
        
        // Skill progression (if Skills plugin is available)
        if (plugin.getConfig().getBoolean("display.player-stats.show-skill-progress", true) && skillsAPI.isAvailable()) {
            SkillsAPIBridge.SkillData skillData = skillsAPI.getLastSkillGained(viewer);
            if (skillData != null) {
                stats.append("\n");
                stats.append(buildSkillProgressBar(skillData));
            }
        }
        
        return stats.toString();
    }
    
    private String buildSkillProgressBar(SkillsAPIBridge.SkillData skillData) {
        StringBuilder bar = new StringBuilder();
        
        // Skill title
        String skillTitle = plugin.getConfig().getString("display.skill-progress.title", 
            "&d⚔ {skill} &7Lv.{level}");
        bar.append(color(skillTitle
            .replace("{skill}", skillData.getFormattedSkillName())
            .replace("{level}", String.valueOf(skillData.getLevel())))).append("\n");
        
        // Progress bar
        int barLength = plugin.getConfig().getInt("display.skill-progress.bar-length", 20);
        String filledChar = plugin.getConfig().getString("display.skill-progress.filled-char", "█");
        String emptyChar = plugin.getConfig().getString("display.skill-progress.empty-char", "░");
        String filledColor = plugin.getConfig().getString("display.skill-progress.filled-color", "&a");
        String emptyColor = plugin.getConfig().getString("display.skill-progress.empty-color", "&7");
        
        double percentage = skillData.getProgressPercentage();
        int filled = (int) Math.round((percentage / 100.0) * barLength);
        int empty = barLength - filled;
        
        bar.append(color(filledColor));
        for (int i = 0; i < filled; i++) {
            bar.append(filledChar);
        }
        bar.append(color(emptyColor));
        for (int i = 0; i < empty; i++) {
            bar.append(emptyChar);
        }
        
        // XP text
        String xpFormat = plugin.getConfig().getString("display.skill-progress.xp-format", 
            " &f{current}&7/&f{required} XP");
        bar.append(color(xpFormat
            .replace("{current}", String.format("%.0f", skillData.getCurrentXP()))
            .replace("{required}", String.format("%.0f", skillData.getRequiredXP()))
            .replace("{percentage}", String.format("%.1f", percentage))));
        
        return bar.toString();
    }
    
    private String buildFooter(Player viewer) {
        StringBuilder footer = new StringBuilder();
        
        footer.append("\n");
        
        // Leaderboard section
        if (plugin.getConfig().getBoolean("display.show-leaderboard", true)) {
            footer.append(buildLeaderboard(viewer));
        }
        
        // Custom footer message
        String customFooter = plugin.getConfig().getString("display.footer-message", "");
        if (!customFooter.isEmpty()) {
            footer.append("\n").append(color(customFooter));
        }
        
        return footer.toString();
    }
    
    private String buildLeaderboard(Player viewer) {
        StringBuilder leaderboard = new StringBuilder();
        
        String separator = color(plugin.getConfig().getString("display.separator", "&8&m━━━━━━━━━━━━━━━━━━━━━━━━━━━━"));
        leaderboard.append(separator).append("\n");
        
        // Leaderboard title
        String title = plugin.getConfig().getString("leaderboard-format.title", "&e&lTop Collectors");
        leaderboard.append(color(title)).append("\n\n");
        
        // Get top players
        int leaderboardSize = plugin.getConfig().getInt("leaderboard-size", 10);
        List<Map.Entry<UUID, Integer>> topPlayers = headTracker.getTopPlayers(leaderboardSize);
        
        // Display leaderboard
        int rank = 1;
        for (Map.Entry<UUID, Integer> entry : topPlayers) {
            UUID uuid = entry.getKey();
            int count = entry.getValue();
            OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
            String playerName = player.getName() != null ? player.getName() : "Unknown";
            
            String format;
            String medal = getMedalForRank(rank);
            
            if (uuid.equals(viewer.getUniqueId())) {
                format = plugin.getConfig().getString("leaderboard-format.current-player", 
                    "{medal} &b{player} &8- &a{count} &7heads &8(&7You&8)");
            } else {
                format = plugin.getConfig().getString("leaderboard-format.entry", 
                    "{medal} &f{player} &8- &a{count} &7heads");
            }
            
            String line = format
                .replace("{medal}", medal)
                .replace("{rank}", String.valueOf(rank))
                .replace("{player}", playerName)
                .replace("{count}", String.valueOf(count));
            
            leaderboard.append(color(line)).append("\n");
            rank++;
        }
        
        leaderboard.append("\n").append(separator);
        
        return leaderboard.toString();
    }
    
    private String getMedalForRank(int rank) {
        return switch (rank) {
            case 1 -> color(plugin.getConfig().getString("leaderboard-format.medals.first", "&6&l#1"));
            case 2 -> color(plugin.getConfig().getString("leaderboard-format.medals.second", "&7&l#2"));
            case 3 -> color(plugin.getConfig().getString("leaderboard-format.medals.third", "&c&l#3"));
            default -> color(plugin.getConfig().getString("leaderboard-format.medals.default", "&7#{rank}"))
                .replace("{rank}", String.valueOf(rank));
        };
    }
    
    private String getAnimatedTitle() {
        List<String> frames = plugin.getConfig().getStringList("display.animated-frames");
        if (frames.isEmpty()) {
            return color("&6&l⚔ Mob Head Achievements ⚔");
        }
        
        int frameIndex = (animationFrame / 2) % frames.size();
        return color(frames.get(frameIndex));
    }
    
    private double getServerTPS() {
        try {
            Object server = Bukkit.getServer().getClass().getMethod("getServer").invoke(Bukkit.getServer());
            double[] recentTps = (double[]) server.getClass().getField("recentTps").get(server);
            return Math.min(recentTps[0], 20.0);
        } catch (Exception e) {
            return 20.0;
        }
    }
    
    private String color(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}
