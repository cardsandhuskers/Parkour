package io.github.cardsandhuskers.parkourchallenge.listeners;

import io.github.cardsandhuskers.parkourchallenge.ParkourChallenge;
import io.github.cardsandhuskers.parkourchallenge.handlers.LevelHandler;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class PlayerClickListener implements Listener {
    LevelHandler levelHandler;
    public PlayerClickListener(LevelHandler levelHandler) {
        this.levelHandler = levelHandler;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {

        //if(e.getAction().equals(Action.LEFT_CLICK_AIR) || e.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
            if(e.getItem() != null && e.getItem().getItemMeta().getDisplayName().equalsIgnoreCase("level skip")) {
                //skip level
                levelHandler.onLevelSkip(e.getPlayer());

            }
        //}
    }
}
