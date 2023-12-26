package io.github.cardsandhuskers.parkourchallenge.listeners;

import io.github.cardsandhuskers.parkourchallenge.ParkourChallenge;
import io.github.cardsandhuskers.parkourchallenge.handlers.LevelHandler;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class PlayerMoveListener implements Listener {
    ParkourChallenge plugin;
    LevelHandler levelHandler;
    public PlayerMoveListener(ParkourChallenge plugin, LevelHandler levelHandler) {
        this.plugin = plugin;
        this.levelHandler = levelHandler;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();

        levelHandler.onLevelComplete(p);
        levelHandler.checkFail(p);

         //if(p.getLocation().getY() <= plugin.getConfig().getInt("minY")) {
        if(p.getGameMode() == GameMode.SPECTATOR && p.getLocation().getY() <= 0) {
            p.teleport(plugin.getConfig().getLocation("spawn"));
        } else if(ParkourChallenge.gameState == ParkourChallenge.State.GAME_STARTING && p.getLocation().getY() <= 0) {
            p.teleport(plugin.getConfig().getLocation("spawn"));
        } else if(levelHandler.checkFail(p)) {
            levelHandler.addFail(p);
            levelHandler.resetPlayer(p);
        }
    }

}
