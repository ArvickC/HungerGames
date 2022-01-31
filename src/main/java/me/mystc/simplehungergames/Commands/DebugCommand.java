package me.mystc.simplehungergames.Commands;

import me.mystc.simplehungergames.SimpleHungerGames;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class DebugCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        System.out.println("isAdmin: " + SimpleHungerGames.isAdmin);
        System.out.println("Admin: " + SimpleHungerGames.admin);
        System.out.println("isGame: " + SimpleHungerGames.isGame);
        System.out.println("inGame: " + SimpleHungerGames.inGame);
        System.out.println("deadGame: " + SimpleHungerGames.deadGame);
        return false;
    }
}
