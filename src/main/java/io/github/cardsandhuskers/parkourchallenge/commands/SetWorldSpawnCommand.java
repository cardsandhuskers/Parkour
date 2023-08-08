package io.github.cardsandhuskers.parkourchallenge.commands;

import io.github.cardsandhuskers.parkourchallenge.ParkourChallenge;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class SetWorldSpawnCommand implements CommandExecutor {

    private ParkourChallenge plugin;
    public SetWorldSpawnCommand(ParkourChallenge plugin) {
        this.plugin = plugin;
    }
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(sender instanceof  Player p && p.isOp()) {
            Location l = p.getLocation();
            plugin.getConfig().set("spawn", l);
            plugin.saveConfig();
            p.sendMessage("Location set to " + l.toString());


        } else if(sender instanceof Player p) {
            p.sendMessage(ChatColor.RED + "ERROR: You do not have sufficient permission to do this");
        } else {
            System.out.println(ChatColor.RED + "ERROR: Cannot run from console");
        }
        return true;
    }
}
