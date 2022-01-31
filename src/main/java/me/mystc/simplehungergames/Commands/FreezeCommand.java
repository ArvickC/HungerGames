package me.mystc.simplehungergames.Commands;

import me.mystc.simplehungergames.SimpleHungerGames;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class FreezeCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(sender.hasPermission("hg.admin") || sender.equals(Bukkit.getServer().getConsoleSender())) {

            if (args.length >= 1 && args[0].equalsIgnoreCase("freeze")) {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7People are now&b Frozen&7!"));
                }
                SimpleHungerGames.preGame = true;
                SimpleHungerGames.isGame = true;
                SimpleHungerGames.resetChests();
                return false;
            }

            if (SimpleHungerGames.preGame) {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7People are now&c Un-Frozen&7!"));
                }
                SimpleHungerGames.preGame = false;
            } else {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7People are now &bFrozen&7!"));
                }
                SimpleHungerGames.preGame = true;
            }
        } else {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cInvalid Permission!"));
            return false;
        }
        return false;
    }
}
