package com.mobheads.leaderboard;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;
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
            
            // Get the last skill that gained XP
            Method getLastSkillXP = profile.getClass().getMethod("getLastSkillXP");
            Object lastSkillType = getLastSkillXP.invoke(profile);
            
            if (lastSkillType == null) return null;
            
            // Get all skills to retrieve the specific skill data
            Method getSkills = profile.getClass().getMethod("getSkills");
            Map<?, ?> skills = (Map<?, ?>) getSkills.invoke(profile);
            
            if (skills == null || skills.isEmpty()) return null;
            
            // Find the last skill that gained XP
            Object skill = skills.get(lastSkillType);
            if (skill == null) return null;
            
            // Extract skill data
            Method getLevel = skill.getClass().getMethod("getLevel");
            Method getExperience = skill.getClass().getMethod("getExperience");
            Method getRequiredExperience = skill.getClass().getMethod("getRequiredExperience");
            
            int level = (int) getLevel.invoke(skill);
            double experience = (double) getExperience.invoke(skill);
            double required = (double) getRequiredExperience.invoke(skill);
            
            String skillName = lastSkillType.toString();
            return new SkillData(skillName, level, experience, required);
            
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
