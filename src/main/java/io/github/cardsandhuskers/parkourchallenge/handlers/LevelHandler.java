package io.github.cardsandhuskers.parkourchallenge.handlers;

import io.github.cardsandhuskers.parkourchallenge.ParkourChallenge;
import io.github.cardsandhuskers.parkourchallenge.objects.GameMessages;
import io.github.cardsandhuskers.teams.Teams;
import io.github.cardsandhuskers.teams.objects.Team;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import java.util.Collections;
import java.util.HashMap;
import java.util.UUID;

import static io.github.cardsandhuskers.parkourchallenge.ParkourChallenge.handler;
import static io.github.cardsandhuskers.parkourchallenge.ParkourChallenge.multiplier;

public class LevelHandler {
    private ParkourChallenge plugin;
    private HashMap<Integer, Level> levels;
    public int numLevels;
    private HashMap<Integer, Integer> playersCompleted;
    private HashMap<UUID, Integer> currentLevels, currentFails, totalFails, levelTime, levelWins;
    private int numFails;

    public LevelHandler(ParkourChallenge plugin) {
        this.plugin = plugin;
    }

    public void registerLevels() {
        levels = new HashMap<>();
        //populate lists
        int counter = 1;
        while(plugin.getConfig().getLocation("levels." + counter) != null) {
            Location spawn = plugin.getConfig().getLocation("levels." + counter);
            //Location plate = plugin.getConfig().getLocation("plates." + counter);
            Location endA = plugin.getConfig().getLocation("end." + counter + ".1");
            Location endB = plugin.getConfig().getLocation("end." + counter + ".2");

            if(endA != null && endB != null) {
                levels.put(counter, new Level(counter, spawn, endA, endB));
            } else {
                plugin.getLogger().warning("Level " + counter + " has an invalid end area");
            }

            counter++;
        }
        numLevels = counter - 1;

        playersCompleted = new HashMap<>();
        for(int i = 1; i <= numLevels; i++) {
            playersCompleted.put(i, 0);
        }

        currentLevels = new HashMap<>();
        for(Player p: Bukkit.getOnlinePlayers()) {
            currentLevels.put(p.getUniqueId(), 1);
        }
        currentFails = new HashMap<>();
        totalFails = new HashMap<>();
        levelTime = new HashMap<>();
        levelWins = new HashMap<>();

        numFails = plugin.getConfig().getInt("numFails");
    }

    /**
     * runs and checks if the player has completed the level they're currently on. If so, handles
     * @param p
     */
    public void onLevelComplete(Player p) {
        UUID u = p.getUniqueId();
        int playerLevel = currentLevels.get(u);
        if(playerLevel > numLevels) return;

        if(levels.get(playerLevel).playerInEnd(p)) {
            currentLevels.put(u, playerLevel + 1);
            currentFails.put(u, 0);
            levelTime.put(u, 0);

            if(playersCompleted.get(playerLevel) == 0) {
                if (levelWins.containsKey(u)) levelWins.put(u, levelWins.get(u) + 1);
                else levelWins.put(u, 1);
            }
            p.getInventory().remove(Material.GOLD_BLOCK);

            givePoints(p, playerLevel);
            p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, ()->{p.playSound(p.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 1, 1);},5);
            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, ()->{
                p.playSound(p.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1, 1);
                Particle.DustOptions dustOptions = new Particle.DustOptions(handler.getPlayerTeam(p).translateColor(), 1);
                p.spawnParticle(Particle.REDSTONE, p.getLocation().add(0,2,0),800, 2, 2, 2, dustOptions);
            },10);

            playersCompleted.put(playerLevel, playersCompleted.get(playerLevel) + 1);
            if(playerLevel == numLevels) {
                p.setGameMode(GameMode.SPECTATOR);
                p.setInvisible(false);
                p.sendMessage("You finished all the levels!");
            }
        }
        int numPlayers = 0;
        for(Team t:handler.getTeams()) for(Player pl:t.getOnlinePlayers()) numPlayers++;
        if(playersCompleted.get(numLevels) == numPlayers) {
            //TODO: Game Over!
        }
    }
    public void incrementTime() {
        for(Team team: Teams.handler.getTeams()) {
            for(Player p:team.getOnlinePlayers()) {
                UUID u = p.getUniqueId();
                if(levelTime.containsKey(u)) levelTime.put(u, levelTime.get(u) + 1);
                else levelTime.put(u, 1);
            }
        }
    }

    public void onLevelSkip(Player p) {
        UUID u = p.getUniqueId();
        int playerLevel = currentLevels.get(u);
        currentLevels.put(u, playerLevel + 1);
        currentFails.put(u, 0);
        levelTime.put(u, 0);

        p.sendMessage(ChatColor.AQUA + "You skipped level " + ChatColor.RED + GameMessages.convertLevel(playerLevel));
        p.playSound(p.getLocation(), Sound.ENTITY_BLAZE_DEATH, 1, 1);

        resetPlayer(p);
        ItemStack item = p.getInventory().getItemInMainHand();
        if(item != null && item.getType() == Material.GOLD_BLOCK) {
            p.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
        }
        item = p.getInventory().getItemInOffHand();
        if(item != null && item.getType() == Material.GOLD_BLOCK) {
            p.getInventory().setItemInOffHand(new ItemStack(Material.AIR));
        }

        if(playerLevel == numLevels) {
            p.setGameMode(GameMode.SPECTATOR);
            p.setInvisible(false);
            p.sendMessage("You finished all the levels!");
        }
    }

    public void givePoints(Player p, int level) {
        p.setHealth(20);
        p.setFoodLevel(20);
        p.setSaturation(20);
        int numCompleted = playersCompleted.get(level);

        if(handler.getPlayerTeam(p) != null) {
            Team t = handler.getPlayerTeam(p);
            double maxPoints = plugin.getConfig().getInt("maxPoints") * multiplier;
            double dropOff = plugin.getConfig().getDouble("dropOff") * multiplier;

            double points = maxPoints - (dropOff * numCompleted);

            if(numCompleted == 0) {
                //TODO: add a win to the player
            }


            for (Player player : Bukkit.getOnlinePlayers()) {
                String message;
                if(player.equals(p)) {
                    message = ChatColor.AQUA + "You";
                } else {
                    message = handler.getPlayerTeam(p).color + p.getName();
                }
                message += ChatColor.AQUA + " finished level " + ChatColor.RED + GameMessages.convertLevel(level) + ChatColor.AQUA + " in " + ChatColor.RED + ChatColor.BOLD;

                if(numCompleted % 10 == 0) {
                    message += (numCompleted+1) + "st";
                } else if(numCompleted % 10 == 1) {
                    message += (numCompleted+1) + "nd";
                } else if(numCompleted % 10 == 2) {
                    message += (numCompleted+1) + "rd";
                } else {
                    message += (numCompleted+1) + "th";
                }
                message += ChatColor.RESET + "" + ChatColor.AQUA + " place";
                if(player.equals(p)) {
                    message += " [" + ChatColor.RED + "" + ChatColor.BOLD + "+" + points + ChatColor.RESET + ChatColor.AQUA + "] points";
                }
                player.sendMessage(message);
            }
            t.addTempPoints(p, points);
        }


    }

    public void resetPlayer(Player p) {
        UUID u = p.getUniqueId();
        int level = currentLevels.get(u);
        p.teleport(levels.get(level).start);

        if(currentFails.containsKey(u) && currentFails.get(u) >= numFails &&
                levelTime.containsKey(u) && levelTime.get(u) >= 60) {
            giveSkipItem(p);
        }
    }

    public void addFail(Player p) {
        UUID u = p.getUniqueId();
        if(totalFails.containsKey(u)) totalFails.put(u, totalFails.get(u) + 1);
        else totalFails.put(u, 1);

        if(currentFails.containsKey(u)) currentFails.put(u, currentFails.get(u) + 1);
        else currentFails.put(u, 1);
    }

    public void giveSkipItem(Player p) {
        ItemStack skipItem = new ItemStack(Material.GOLD_BLOCK);
        ItemMeta skipItemMeta = skipItem.getItemMeta();
        skipItemMeta.setDisplayName("Level Skip");
        skipItemMeta.setLore(Collections.singletonList("Skip the level you're currently on. You will not receive points for the level."));
        skipItem.setItemMeta(skipItemMeta);
        p.getInventory().setItem(4, skipItem);
    }

    public void prepPlayer(Player p) {
        Team team = handler.getPlayerTeam(p);
        ItemStack boots = new ItemStack(Material.LEATHER_BOOTS, 1);
        LeatherArmorMeta bootsMeta = (LeatherArmorMeta) boots.getItemMeta();
        if(handler.getPlayerTeam(p) != null) {
            bootsMeta.setColor(team.translateColor());
        }
        bootsMeta.setUnbreakable(true);
        boots.setItemMeta(bootsMeta);
        p.getEquipment().setBoots(boots);

        ItemStack placeholderRod = new ItemStack(Material.BLAZE_ROD);
        ItemMeta placeholderRodMeta = placeholderRod.getItemMeta();
        placeholderRodMeta.setDisplayName("Placeholder Rod");
        placeholderRodMeta.setLore(Collections.singletonList("Since you have no hand, hold this if it helps you"));

        p.setInvisible(true);


    }
    public int getCurrentLevel(Player p) {
        return currentLevels.getOrDefault(p.getUniqueId(), 0);
    }
    public int getLevelFails(Player p) {
        return currentFails.getOrDefault(p.getUniqueId(), 0);
    }

    public int getTotalFails(Player p) {
        return totalFails.getOrDefault(p.getUniqueId(), 0);
    }

    public int getPlayerWins(Player p) {
        return levelWins.getOrDefault(p.getUniqueId(), 0);
    }

    class Level {
        public int levelNum;
        public Location start;
        public int lowerX, lowerY, lowerZ, higherX, higherY, higherZ;
        public Level(int levelNum, Location start, Location endA, Location endB) {
            this.levelNum = levelNum;
            this.start = start;
            setCoordinates(endA, endB);

        }

        public void setCoordinates(Location locA, Location locB) {
            lowerX = Math.min(locA.getBlockX(), locB.getBlockX());
            higherX = Math.max(locA.getBlockX(), locB.getBlockX());

            lowerY = Math.min(locA.getBlockY(), locB.getBlockY());
            higherY = lowerY + 5;

            lowerZ = Math.min(locA.getBlockZ(), locB.getBlockZ());
            higherZ = Math.max(locA.getBlockZ(), locB.getBlockZ());

            higherX++;
            higherZ++;
        }

        public boolean playerInEnd(Player p) {
            Location playerLoc = p.getLocation();
            if( playerLoc.getX() >= lowerX && playerLoc.getX() <= higherX &&
                playerLoc.getY() >= lowerY && playerLoc.getY() <= higherY &&
                playerLoc.getZ() >= lowerZ && playerLoc.getZ() <= higherZ) {
                return true;
            }
            return false;
        }
    }
}