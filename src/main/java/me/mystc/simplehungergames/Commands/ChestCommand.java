package me.mystc.simplehungergames.Commands;

import me.mystc.simplehungergames.Files.LocationsFile;
import me.mystc.simplehungergames.SimpleHungerGames;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ChestCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cYou need to be a player to run that command."));
            return false;
        }
        if(!sender.hasPermission("hg.chests")) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cInvalid Permission!"));
            return false;
        }

        Player p = (Player)sender;
        p.openInventory(SimpleHungerGames.chestOptions);

        return false;
    }
}
