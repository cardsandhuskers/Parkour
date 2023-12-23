package io.github.cardsandhuskers.parkourchallenge.handlers;

import io.github.cardsandhuskers.parkourchallenge.ParkourChallenge;
import io.github.cardsandhuskers.parkourchallenge.listeners.PlayerDamageListener;
import io.github.cardsandhuskers.parkourchallenge.listeners.PlayerJoinListener;
import io.github.cardsandhuskers.parkourchallenge.listeners.PlayerMoveListener;
import io.github.cardsandhuskers.parkourchallenge.listeners.PlayerClickListener;
import io.github.cardsandhuskers.parkourchallenge.objects.Countdown;
import io.github.cardsandhuskers.parkourchallenge.objects.GameMessages;
import io.github.cardsandhuskers.teams.objects.Team;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.io.IOException;

import static io.github.cardsandhuskers.teams.Teams.handler;

public class GameStageHandler {
    ParkourChallenge plugin;
    private Countdown pregameTimer, gameTimer, gameEndTimer;
    public LevelHandler levelHandler;
    public int numLevels;


    public GameStageHandler(ParkourChallenge plugin) {
        this.plugin = plugin;
    }

    /**
     * Initial start of the game
     * Registers the listeners and calls the pregame countdown
     */
    public void start() {

        levelHandler = new LevelHandler(plugin);
        levelHandler.registerLevels();
        numLevels = levelHandler.numLevels;

        plugin.getServer().getPluginManager().registerEvents(new PlayerClickListener(levelHandler), plugin);
        plugin.getServer().getPluginManager().registerEvents(new PlayerDamageListener(), plugin);
        plugin.getServer().getPluginManager().registerEvents(new PlayerMoveListener(plugin, levelHandler), plugin);
        plugin.getServer().getPluginManager().registerEvents(new PlayerJoinListener(plugin, levelHandler), plugin);

        buildWall(Material.BARRIER);
        pregameCountdown();
    }

    /**
     * Countdown that runs before the game starts (while players are waiting in game world)
     * teleports players, sets their gamemode, and at the end of the timer deletes the wall blocking them
     */
    public void pregameCountdown() {
        pregameTimer = new Countdown(plugin,
                //should be 60
                plugin.getConfig().getInt("PregameTime"),
                //Timer Start
                () -> {
                    ParkourChallenge.gameState = ParkourChallenge.State.GAME_STARTING;
                    Location spawn = plugin.getConfig().getLocation("spawn");
                    for(Player p:Bukkit.getOnlinePlayers()) {
                        p.teleport(spawn);
                        if(handler.getPlayerTeam(p) != null) {
                            p.setGameMode(GameMode.ADVENTURE);
                        } else {
                            p.setGameMode(GameMode.SPECTATOR);
                        }
                    }
                },

                //Timer End
                () -> {
                    Bukkit.broadcastMessage(ChatColor.RED + "Start!");
                    for(Player p:Bukkit.getOnlinePlayers()) {
                        p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1F);
                        p.sendTitle(ChatColor.GREEN + "GO!", "", 5, 20, 5);
                        p.getInventory().clear();

                        if(handler.getPlayerTeam(p) != null) {
                            p.setGameMode(GameMode.ADVENTURE);
                            levelHandler.prepPlayer(p);
                        } else {
                            p.setGameMode(GameMode.SPECTATOR);
                        }
                        p.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 18000, 1));
                    }
                    ParkourChallenge.timeVar = 0;
                    buildWall(Material.AIR);

                    gameTimer();

                },

                //Each Second
                (t) -> {
                    if(t.getSecondsLeft() == t.getTotalSeconds() - 2) Bukkit.broadcastMessage(GameMessages.gameDescription(numLevels, plugin));
                    if(t.getSecondsLeft() == t.getTotalSeconds() - 12) Bukkit.broadcastMessage(GameMessages.pointsDescription(plugin));

                    ParkourChallenge.timeVar = t.getSecondsLeft();
                    if(t.getSecondsLeft() <= 4) {
                        for(Player p:Bukkit.getOnlinePlayers()) {
                            p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 2F);
                            p.sendTitle(ChatColor.GREEN + ">" + t.getSecondsLeft() + "<", "", 2, 16, 2);
                        }
                    }
                }
        );

        // Start scheduling, don't use the "run" method unless you want to skip a second
        pregameTimer.scheduleTimer();
    }

    /**
     * Timer that runs while game is active
     * Calls the gameEndTimer when done
     */
    public void gameTimer() {
        gameTimer = new Countdown((JavaPlugin)plugin,
                plugin.getConfig().getInt("GameTime"),
                //Timer Start
                () -> {
                    ParkourChallenge.gameState = ParkourChallenge.State.GAME_IN_PROGRESS;
                    for(Team t:handler.getTeams()) {
                        for(Player p: t.getOnlinePlayers()) {
                            if(p.getGameMode() != GameMode.ADVENTURE) levelHandler.resetPlayer(p);
                        }
                    }
                },

                //Timer End
                this::gameEndTimer,

                //Each Second
                (t) -> {
                    ParkourChallenge.timeVar = t.getSecondsLeft();
                    if(t.getSecondsLeft() <= 4) {
                        for(Player p:Bukkit.getOnlinePlayers()) {
                            p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 2F);
                        }
                        Bukkit.broadcastMessage(ChatColor.GREEN + "Game ends in " + ChatColor.AQUA + t.getSecondsLeft() + ChatColor.GREEN + " Seconds!");
                    }
                    levelHandler.incrementTime();

                }
        );

        // Start scheduling, don't use the "run" method unless you want to skip a second
        gameTimer.scheduleTimer();
    }

    /**
     * Timer that runs during the end of the game
     * Triggers the messages in the console for performance
     */
    public void gameEndTimer() {
        for(Player p:Bukkit.getOnlinePlayers()) {
            p.setInvisible(false);
            p.setGameMode(GameMode.SPECTATOR);
        }
        HandlerList.unregisterAll(plugin);

        gameEndTimer = new Countdown((JavaPlugin)plugin,
                //should be 60
                plugin.getConfig().getInt("PostgameTime"),
                //Timer Start
                () -> {
                    Bukkit.broadcastMessage(ChatColor.GREEN + "GAME OVER!");
                    for(Player p:Bukkit.getOnlinePlayers()) {
                        p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1F);
                        p.sendTitle(ChatColor.GREEN + "GAME OVER", "", 5, 20, 5);
                    }
                    ParkourChallenge.timeVar = 0;

                    ParkourChallenge.gameState = ParkourChallenge.State.GAME_OVER;

                },

                //Timer End
                () -> {
                    try {
                        plugin.statCalculator.saveRecords();
                    } catch (IOException e) {
                        StackTraceElement[] trace = e.getStackTrace();
                        String str = "";
                        for(StackTraceElement element:trace) str += element.toString() + "\n";
                        plugin.getLogger().severe("ERROR Calculating Stats!\n" + str);
                    }

                    for(Player p:Bukkit.getOnlinePlayers()) {
                        if(plugin.getConfig().getLocation("lobby") != null) {
                            p.teleport(plugin.getConfig().getLocation("lobby"));
                        } else {
                            Bukkit.broadcastMessage(ChatColor.RED + "NO LOBBY LOCATION FOUND");
                        }
                    }
                    for(Player p:Bukkit.getOnlinePlayers()) {
                        if(p.isOp()) {
                            p.performCommand("startRound");
                            break;
                        }
                    }

                },

                //Each Second
                (t) -> {
                    ParkourChallenge.timeVar = t.getSecondsLeft();

                    if(t.getSecondsLeft() == t.getTotalSeconds() - 1) GameMessages.announceTopPlayers();
                    if(t.getSecondsLeft() == t.getTotalSeconds() - 6) GameMessages.announceTeamPlayers();
                    if(t.getSecondsLeft() == t.getTotalSeconds() - 11) GameMessages.announceTeamLeaderboard();
                }
        );

        // Start scheduling, don't use the "run" method unless you want to skip a second
        gameEndTimer.scheduleTimer();
    }

    /**
     * Cancels any active timers, used as part of the cancelGame command
     * @return
     */
    public boolean cancelTimers() {
        if(pregameTimer != null) pregameTimer.cancelTimer();
        if(gameTimer != null) gameTimer.cancelTimer();
        if(gameEndTimer != null) gameEndTimer.cancelTimer();

        return true;
    }

    /**
     * Builds the wall ahead of players before the game starts
     * Use air as the material to destroy it
     * @param mat - material you want the wall made of
     */
    public void buildWall(Material mat) {
        Location pos1 = plugin.getConfig().getLocation("startWall.1");
        Location pos2 = plugin.getConfig().getLocation("startWall.2");

        int lowerx, lowery, lowerz, higherx, highery, higherz;
        lowerx = Math.min(pos1.getBlockX(), pos2.getBlockX());
        lowery = Math.min(pos1.getBlockY(), pos2.getBlockY());
        lowerz = Math.min(pos1.getBlockZ(), pos2.getBlockZ());

        higherx = Math.max(pos1.getBlockX(), pos2.getBlockX());
        highery = Math.max(pos1.getBlockY(), pos2.getBlockY());
        higherz = Math.max(pos1.getBlockZ(), pos2.getBlockZ());

        for(int x = lowerx; x <= higherx; x++) {
            for(int y = lowery; y <= highery; y++) {
                for(int z = lowerz; z <= higherz; z++) {
                    Location l = new Location(pos1.getWorld(), x, y, z);
                    l.getBlock().setType(mat);
                }
            }
        }
    }
}
