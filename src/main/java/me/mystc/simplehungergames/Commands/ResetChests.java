package me.mystc.simplehungergames.Commands;

import me.mystc.simplehungergames.SimpleHungerGames;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ResetChests implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cYou need to be a player to run that command."));
            return false;
        }
        if(!sender.hasPermission("hg.admin")) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cInvalid Permission!"));
            return false;
        }
        if(!SimpleHungerGames.isGame) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cNo game running!"));
            return false;
        }

        SimpleHungerGames.resetChests();
        for(Player p : Bukkit.getOnlinePlayers()) {
            p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7Chests have been&b restocked&7!"));
        }

        return false;
    }
}
