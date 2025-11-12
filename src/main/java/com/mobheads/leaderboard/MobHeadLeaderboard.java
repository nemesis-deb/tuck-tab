package com.mobheads.leaderboard;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class MobHeadLeaderboard extends JavaPlugin {
    
    private HeadTracker headTracker;
    private TabListManager tabListManager;
    private SkillsAPIBridge skillsAPI;
    
    @Override
    public void onEnable() {
        saveDefaultConfig();
        
        headTracker = new HeadTracker(this);
        skillsAPI = new SkillsAPIBridge();
        tabListManager = new TabListManager(this, headTracker, skillsAPI);
        
        getCommand("headstats").setExecutor(new HeadStatsCommand(headTracker));
        getCommand("headreload").setExecutor(new ReloadCommand(this));
        
        getServer().getPluginManager().registerEvents(new AchievementListener(headTracker), this);
        
        tabListManager.startUpdating();
        
        if (skillsAPI.isAvailable()) {
            getLogger().info("Mob Head Leaderboard enabled with Skills integration!");
        } else {
            getLogger().info("Mob Head Leaderboard enabled!");
        }
    }
    
    @Override
    public void onDisable() {
        if (tabListManager != null) {
            tabListManager.stopUpdating();
        }
        getLogger().info("Mob Head Leaderboard disabled!");
    }
    
    public void reload() {
        reloadConfig();
        if (tabListManager != null) {
            tabListManager.stopUpdating();
            tabListManager.startUpdating();
        }
    }
}
