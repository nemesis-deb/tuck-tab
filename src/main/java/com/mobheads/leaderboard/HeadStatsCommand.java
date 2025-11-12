package com.mobheads.leaderboard;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class HeadStatsCommand implements CommandExecutor {
    
    private final HeadTracker headTracker;
    
    public HeadStatsCommand(HeadTracker headTracker) {
        this.headTracker = headTracker;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player target;
        
        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "Console must specify a player!");
                return true;
            }
            target = (Player) sender;
        } else {
            target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                sender.sendMessage(ChatColor.RED + "Player not found!");
                return true;
            }
        }
        
        int count = headTracker.getHeadCount(target);
        int rank = headTracker.getPlayerRank(target.getUniqueId());
        
        sender.sendMessage(ChatColor.GOLD + "=== Mob Head Stats ===");
        sender.sendMessage(ChatColor.YELLOW + "Player: " + ChatColor.WHITE + target.getName());
        sender.sendMessage(ChatColor.YELLOW + "Heads Collected: " + ChatColor.GREEN + count);
        if (rank > 0) {
            sender.sendMessage(ChatColor.YELLOW + "Rank: " + ChatColor.AQUA + "#" + rank);
        }
        
        return true;
    }
}
