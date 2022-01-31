package me.mystc.simplehungergames.Commands;

import me.mystc.simplehungergames.SimpleHungerGames;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class EndGame implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!SimpleHungerGames.isGame) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cNo game running!"));
            return false;
        }
        if(sender.hasPermission("hg.admin") || sender.equals(Bukkit.getServer().getConsoleSender())) {
            for(Player p : Bukkit.getOnlinePlayers()) {
                SimpleHungerGames.setLobby(p);
                p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7GG, game&c ended."));
            }

            SimpleHungerGames.inGame.clear();
            SimpleHungerGames.deadGame.clear();
            SimpleHungerGames.admin = null;
            SimpleHungerGames.isAdmin = false;
            SimpleHungerGames.preGame = false;
            SimpleHungerGames.isGame = false;
            Bukkit.getWorld("world").getWorldBorder().setSize(Bukkit.getPluginManager().getPlugin("SimpleHungerGames").getConfig().getInt("world-border"));
        } else {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cInvalid Permission!"));
            return false;
        }

        return false;
    }
}
