package com.mobheads.leaderboard;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Bridge class to interact with the TuckSkills plugin API.
 * Uses reflection to avoid hard dependency on the skills plugin.
 */
public class SkillsAPIBridge {
    
    private Plugin skillsPlugin;
    private Object playerDataManager;
    private boolean isAvailable = false;
    private final Map<UUID, SkillData> lastSkillCache = new HashMap<>();
    
    public SkillsAPIBridge() {
        try {
            skillsPlugin = Bukkit.getPluginManager().getPlugin("SkillsPlugin");
            if (skillsPlugin != null && skillsPlugin.isEnabled()) {
                Method getPlayerDataManager = skillsPlugin.getClass().getMethod("getPlayerDataManager");
                playerDataManager = getPlayerDataManager.invoke(skillsPlugin);
                isAvailable = true;
            }
        } catch (Exception e) {
            isAvailable = false;
        }
    }
    
    public boolean isAvailable() {
        return isAvailable && skillsPlugin != null && skillsPlugin.isEnabled();
    }
    
    public SkillData getLastSkillGained(Player player) {
        if (!isAvailable()) return null;
        
        try {
            // Get the player's skill profile
            Method getProfile = playerDataManager.getClass().getMethod("getProfile", UUID.class);
            Object profile = getProfile.invoke(playerDataManager, player.getUniqueId());
            
            if (profile == null) return null;
            
            // Get all skills and find the one with highest XP progress or most recent change
            Method getSkills = profile.getClass().getMethod("getSkills");
            Map<?, ?> skills = (Map<?, ?>) getSkills.invoke(profile);
            
            if (skills == null || skills.isEmpty()) return null;
            
            // Check cache first
            SkillData cached = lastSkillCache.get(player.getUniqueId());
            
            // Find skill with most progress or use cached
            SkillData bestSkill = null;
            double highestProgress = 0;
            
            for (Map.Entry<?, ?> entry : skills.entrySet()) {
                Object skillType = entry.getKey();
                Object skill = entry.getValue();
                
                if (skill == null) continue;
                
                // Extract skill data
                Method getLevel = skill.getClass().getMethod("getLevel");
                Method getExperience = skill.getClass().getMethod("getExperience");
                Method getRequiredExperience = skill.getClass().getMethod("getRequiredExperience");
                
                int level = (int) getLevel.invoke(skill);
                double experience = (double) getExperience.invoke(skill);
                double required = (double) getRequiredExperience.invoke(skill);
                
                String skillName = skillType.toString();
                SkillData currentSkill = new SkillData(skillName, level, experience, required);
                
                // Check if this skill has changed (gained XP)
                if (cached != null && cached.getSkillName().equals(skillName)) {
                    if (experience > cached.getCurrentXP() || level > cached.getLevel()) {
                        // This skill gained XP, update cache and return it
                        lastSkillCache.put(player.getUniqueId(), currentSkill);
                        return currentSkill;
                    }
                }
                
                // Track skill with highest progress
                double progress = experience / required;
                if (progress > highestProgress) {
                    highestProgress = progress;
                    bestSkill = currentSkill;
                }
            }
            
            // If we have a cached skill and it's still valid, use it
            if (cached != null) {
                // Verify cached skill still exists and update its data
                for (Map.Entry<?, ?> entry : skills.entrySet()) {
                    Object skillType = entry.getKey();
                    if (skillType.toString().equals(cached.getSkillName())) {
                        Object skill = entry.getValue();
                        Method getLevel = skill.getClass().getMethod("getLevel");
                        Method getExperience = skill.getClass().getMethod("getExperience");
                        Method getRequiredExperience = skill.getClass().getMethod("getRequiredExperience");
                        
                        int level = (int) getLevel.invoke(skill);
                        double experience = (double) getExperience.invoke(skill);
                        double required = (double) getRequiredExperience.invoke(skill);
                        
                        SkillData updated = new SkillData(cached.getSkillName(), level, experience, required);
                        lastSkillCache.put(player.getUniqueId(), updated);
                        return updated;
                    }
                }
            }
            
            // Use skill with highest progress
            if (bestSkill != null) {
                lastSkillCache.put(player.getUniqueId(), bestSkill);
                return bestSkill;
            }
            
            return null;
            
        } catch (Exception e) {
            return null;
        }
    }
    
    public static class SkillData {
        private final String skillName;
        private final int level;
        private final double currentXP;
        private final double requiredXP;
        
        public SkillData(String skillName, int level, double currentXP, double requiredXP) {
            this.skillName = skillName;
            this.level = level;
            this.currentXP = currentXP;
            this.requiredXP = requiredXP;
        }
        
        public String getSkillName() {
            return skillName;
        }
        
        public int getLevel() {
            return level;
        }
        
        public double getCurrentXP() {
            return currentXP;
        }
        
        public double getRequiredXP() {
            return requiredXP;
        }
        
        public double getProgressPercentage() {
            return (currentXP / requiredXP) * 100.0;
        }
        
        public String getFormattedSkillName() {
            // Convert MINING -> Mining, WOODCUTTING -> Woodcutting
            String name = skillName.toLowerCase();
            return name.substring(0, 1).toUpperCase() + name.substring(1);
        }
    }
}
