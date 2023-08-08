package io.github.cardsandhuskers.parkourchallenge;

import io.github.cardsandhuskers.parkourchallenge.commands.*;
import io.github.cardsandhuskers.parkourchallenge.objects.Placeholder;
import io.github.cardsandhuskers.parkourchallenge.objects.StatCalculator;
import io.github.cardsandhuskers.teams.Teams;
import io.github.cardsandhuskers.teams.handlers.TeamHandler;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class ParkourChallenge extends JavaPlugin {
    public static double multiplier;
    public static TeamHandler handler;
    public static State gameState = State.GAME_STARTING;
    public static int timeVar = 0;
    public StatCalculator statCalculator;

    @Override
    public void onEnable() {
        // Plugin startup logic
        StartGameCommand startGameCommand = new StartGameCommand(this);
//Placeholder API validation
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            /*
             * We register the EventListener here, when PlaceholderAPI is installed.
             * Since all events are in the main class (this class), we simply use "this"
             */
            new Placeholder(this, startGameCommand).register();

        } else {
            /*
             * We inform about the fact that PlaceholderAPI isn't installed and then
             * disable this plugin to prevent issues.
             */
            System.out.println("Could not find PlaceholderAPI!");
            //Bukkit.getPluginManager().disablePlugin(this);
        }


        getCommand("startParkour").setExecutor(startGameCommand);
        getCommand("setParkourSpawn").setExecutor(new SetWorldSpawnCommand(this));
        getCommand("setParkourLobby").setExecutor(new SetLobbyCommand(this));
        getCommand("cancelParkour").setExecutor(new CancelGameCommand(this, startGameCommand));
        getCommand("reloadParkour").setExecutor(new ReloadConfigCommand(this));
        getCommand("setParkourLevelStart").setExecutor(new SaveLevelStartCommand(this));

        SaveLevelEndCommand saveLevelEndCommand = new SaveLevelEndCommand(this);
        getCommand("setParkourLevelEnd").setExecutor(saveLevelEndCommand);
        getCommand("setParkourStartWall").setExecutor(new SetWallCommand(saveLevelEndCommand));

        saveDefaultConfig();
        getConfig().options().copyDefaults(true);
        handler = Teams.handler;

        statCalculator = new StatCalculator(this, startGameCommand);
        try {
            statCalculator.calculateStats();
        } catch (Exception e) {
            StackTraceElement[] trace = e.getStackTrace();
            String str = "";
            for(StackTraceElement element:trace) str += element.toString() + "\n";
            this.getLogger().severe("ERROR Calculating Stats!\n" + str);
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public enum State {
        GAME_STARTING,
        GAME_IN_PROGRESS,
        GAME_OVER
    }
}
