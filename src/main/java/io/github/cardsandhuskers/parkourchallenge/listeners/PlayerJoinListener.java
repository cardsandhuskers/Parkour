package io.github.cardsandhuskers.parkourchallenge.listeners;

import io.github.cardsandhuskers.parkourchallenge.ParkourChallenge;
import io.github.cardsandhuskers.parkourchallenge.handlers.LevelHandler;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.Objects;

import static io.github.cardsandhuskers.parkourchallenge.ParkourChallenge.gameState;
import static io.github.cardsandhuskers.parkourchallenge.ParkourChallenge.handler;

public class PlayerJoinListener implements Listener {
    private LevelHandler levelHandler;
    ParkourChallenge plugin;

    public PlayerJoinListener(ParkourChallenge plugin, LevelHandler levelHandler) {
        this.levelHandler = levelHandler;
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        if(handler.getPlayerTeam(p) != null) {
            levelHandler.resetPlayer(e.getPlayer());
            levelHandler.prepPlayer(e.getPlayer());
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> p.setGameMode(GameMode.ADVENTURE),2L);
        }
        else {
            try{p.teleport(Objects.requireNonNull(plugin.getConfig().getLocation("spawn")));}catch (NullPointerException ex) {
                plugin.getLogger().severe("Parkour World Spawn is Missing!");
            };
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> p.setGameMode(GameMode.SPECTATOR),2L);
        }
    }
}
