package io.github.cardsandhuskers.parkourchallenge.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class SetWallCommand implements CommandExecutor {
    SaveLevelEndCommand saveLevelEndCommand;

    public SetWallCommand(SaveLevelEndCommand saveLevelEndCommand) {
        this.saveLevelEndCommand = saveLevelEndCommand;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(sender instanceof  Player p && p.isOp()) {
            saveLevelEndCommand.type = SaveLevelEndCommand.Type.START_WALL;
            saveLevelEndCommand.initializeCommand(p);
        } else if(sender instanceof Player p) {
            p.sendMessage(ChatColor.RED + "ERROR: You do not have sufficient permission to do this");
        } else {
            System.out.println(ChatColor.RED + "ERROR: Cannot run from console");
        }

        return true;
    }
}
