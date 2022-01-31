package me.mystc.simplehungergames.Commands;

import me.mystc.simplehungergames.Files.LocationsFile;
import me.mystc.simplehungergames.SimpleHungerGames;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PlayCommand implements CommandExecutor {
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
        if(args.length != 2) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cIncorrect Input!&7 /play [Admin?<true/false>] [camName]"));
            return false;
        }
        if(Bukkit.getOnlinePlayers().size() > LocationsFile.get().getInt("spawns")) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cNot enough spawn locations to start the game."));
            return false;
        }
        if(SimpleHungerGames.isGame) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cGame already running!"));
            return false;
        }

        if(args[0].equalsIgnoreCase("true")) {
            int len = Bukkit.getOnlinePlayers().size()-1;
            if(len <= 1) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cNot enough people to start game."));
                return false;
            }

            ArrayList<Player> players = new ArrayList<>();
            for(Player p : Bukkit.getOnlinePlayers()) {
                if(!p.getName().equalsIgnoreCase(sender.getName())) {
                    players.add(p);
                    SimpleHungerGames.inGame.add(p.getUniqueId());
                    SimpleHungerGames.setGamer(p);
                }
            }
            int spawnLen = LocationsFile.get().getInt("spawns");
            for(int i=0;i<spawnLen;i++) {
                if(i == players.size()) break;
                Player p = players.get(i);
                //players.remove(p);
                if(p.getName().equalsIgnoreCase(sender.getName())) continue;
                List<Double> cord = LocationsFile.get().getDoubleList("spawn-"+(i+1));
                p.teleport(new Location(Bukkit.getWorld("world"), cord.get(0), cord.get(1), cord.get(2)));
                //p.setWalkSpeed(0f);
                p.getInventory().clear();
                Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), "pc startother " + p.getName() + " " + args[1]);
            }
            Player p = (Player)sender;
            SimpleHungerGames.makeAdmin(p);
            p.teleport(new Location(Bukkit.getWorld("world"), Bukkit.getPluginManager().getPlugin("SimpleHungerGames").getConfig().getIntegerList("lobby-location").get(0), Bukkit.getPluginManager().getPlugin("SimpleHungerGames").getConfig().getIntegerList("lobby-location").get(1), Bukkit.getPluginManager().getPlugin("SimpleHungerGames").getConfig().getIntegerList("lobby-location").get(2)));
            Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), "pc startother " + p.getName() + " admin");

            SimpleHungerGames.isAdmin = true;
            SimpleHungerGames.admin = p;
        } else if(args[0].equalsIgnoreCase("false")) {
            int len = Bukkit.getOnlinePlayers().size();
            if(len <= 1) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cNot enough people to start game."));
                return false;
            }

            ArrayList<Player> players = new ArrayList<>();
            for(Player p : Bukkit.getOnlinePlayers()) {
                players.add(p);
                SimpleHungerGames.inGame.add(p.getUniqueId());
                SimpleHungerGames.setGamer(p);
            }

            System.out.println(players);

            int spawnLen = LocationsFile.get().getInt("spawns");
            for(int i=1;i<=spawnLen;i++) {
                if(i == players.size()) break;
                Player p = players.get(i-1);
                //players.remove(p);
                if(p.getName().equalsIgnoreCase(sender.getName())) continue;
                List<Double> cord = LocationsFile.get().getDoubleList("spawn-"+(i));
                p.teleport(new Location(Bukkit.getWorld("world"), cord.get(0), cord.get(1), cord.get(2)));
                //p.setWalkSpeed(0f);
                p.getInventory().clear();
                Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), "pc startother " + p.getName() + " " + args[1]);
            }
            Player p = (Player)sender;
            List<Double> cord = LocationsFile.get().getDoubleList("spawn-1");
            p.teleport(new Location(Bukkit.getWorld("world"), cord.get(0), cord.get(1), cord.get(2)));
            p.getInventory().clear();
            Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), "pc startother " + p.getName() + " admin");
            SimpleHungerGames.isAdmin = false;
            SimpleHungerGames.admin = null;
        } else {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cIncorrect Input!!&7 /play [Admin?<true/false>] [camName]"));
            return false;
        }
        return false;
    }
}
