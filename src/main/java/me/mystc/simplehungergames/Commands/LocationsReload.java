package me.mystc.simplehungergames.Commands;

import me.mystc.simplehungergames.Files.ChestsFile;
import me.mystc.simplehungergames.Files.LocationsFile;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class LocationsReload implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!sender.hasPermission("hg.reload")) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cInvalid Permission"));
            return false;
        }
        LocationsFile.save();
        LocationsFile.reload();

        ChestsFile.save();
        ChestsFile.reload();

        Bukkit.getServer().getPluginManager().getPlugin("SimpleHungerGames").saveConfig();
        Bukkit.getServer().getPluginManager().getPlugin("SimpleHungerGames").reloadConfig();

        return false;
    }
}
