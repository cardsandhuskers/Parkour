package io.github.cardsandhuskers.parkourchallenge.listeners;

import io.github.cardsandhuskers.parkourchallenge.commands.SaveLevelEndCommand;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class FinishLineRodClickListener implements Listener {
    SaveLevelEndCommand saveLevelEndCommand;
    public FinishLineRodClickListener(SaveLevelEndCommand saveLevelEndCommand) {
        this.saveLevelEndCommand = saveLevelEndCommand;
    }
    @EventHandler
    public void onRodClick(PlayerInteractEvent e) {
        if(e.getItem() != null && e.getItem().getItemMeta().getDisplayName().equalsIgnoreCase("Parkour Area Selector Rod")) {
            if(e.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
                Location locA = e.getClickedBlock().getLocation();
                saveLevelEndCommand.setLocA(locA);
                e.getPlayer().sendMessage("Point 1: (" + (int)locA.getX() + "," + (int)locA.getY() + "," + (int)locA.getZ() + ")");
            } else if(e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
                Location locB = e.getClickedBlock().getLocation();
                saveLevelEndCommand.setLocB(locB);
                e.getPlayer().sendMessage("Point 2: (" + (int)locB.getX() + "," + (int)locB.getY() + "," + (int)locB.getZ() + ")");
            }
            e.setCancelled(true);
        }

    }
}
