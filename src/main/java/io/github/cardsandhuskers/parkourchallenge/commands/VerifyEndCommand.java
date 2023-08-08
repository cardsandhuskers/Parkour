package io.github.cardsandhuskers.parkourchallenge.commands;

import io.github.cardsandhuskers.parkourchallenge.ParkourChallenge;
import io.github.cardsandhuskers.parkourchallenge.listeners.FinishLineRodClickListener;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class VerifyEndCommand implements CommandExecutor {
    ParkourChallenge plugin;
    FinishLineRodClickListener finishLineRodClickListener;
    SaveLevelEndCommand saveLevelEndCommand;

    public VerifyEndCommand(ParkourChallenge plugin, FinishLineRodClickListener finishLineRodClickListener, SaveLevelEndCommand saveLevelEndCommand) {
        this. plugin = plugin;
        this.finishLineRodClickListener = finishLineRodClickListener;
        this.saveLevelEndCommand = saveLevelEndCommand;
    }
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(sender instanceof Player p && p.isOp()) {
            boolean result = saveLevelEndCommand.attemptSave(p);

            if(result) {
                HandlerList.unregisterAll(finishLineRodClickListener);
            }
            return true;
        } else {
            return false;
        }
    }
}
