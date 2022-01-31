package me.mystc.simplehungergames.Commands;

import me.mystc.simplehungergames.Files.LocationsFile;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetLocationCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cYou need to be a player to run that command!"));
            return false;
        }
        if(!sender.hasPermission("hg.setloc")) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cInvalid Permission."));
            return false;
        }
        if(args.length != 1) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cIncorrect Format!&7 /sl <number [start from 1]>"));
            return false;
        }

        Player p = (Player) sender;
        Location loc = p.getLocation();
        double x = loc.getX();
        double y = loc.getY();
        double z = loc.getZ();
        int num = Integer.parseInt(args[0]);
        LocationsFile.get().set("spawn-" + num, new Double[]{x, y, z});
        LocationsFile.save();

        if(LocationsFile.get().isSet("spawns")) {
            if(num > LocationsFile.get().getInt("spawns")) {
                LocationsFile.get().set("spawns", num);
                LocationsFile.save();
            }
        } else {
            LocationsFile.get().set("spawns", num);
            LocationsFile.save();
        }

        p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7Set &b{&6" + x + ", " + y + ", " + z + "&b}&7 as spawn location&b " + num));

        return false;
    }
}
