package io.github.cardsandhuskers.parkourchallenge.handlers;

import io.github.cardsandhuskers.parkourchallenge.ParkourChallenge;
import io.github.cardsandhuskers.parkourchallenge.objects.GameMessages;
import io.github.cardsandhuskers.parkourchallenge.objects.Level;
import io.github.cardsandhuskers.teams.handlers.TeamHandler;
import io.github.cardsandhuskers.teams.objects.Team;
import io.github.cardsandhuskers.parkourchallenge.objects.Stats;
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
    private Stats stats;

    public LevelHandler(ParkourChallenge plugin, Stats stats) {
        this.plugin = plugin;
        this.stats = stats;
    }

    /**
     * Registers all of the levels into the level hashmap
     * Accesses the config file to obtain the locations of each level's start and end points
     */
    public void registerLevels() {
        levels = new HashMap<>();
        //populate lists
        int counter = 1;
        while(plugin.getConfig().getLocation("levels." + counter) != null) {
            try {
                Location spawn = plugin.getConfig().getLocation("levels." + counter);
                //Location plate = plugin.getConfig().getLocation("plates." + counter);
                Location endA = plugin.getConfig().getLocation("end." + counter + ".1");
                Location endB = plugin.getConfig().getLocation("end." + counter + ".2");

                if (endA != null && endB != null) {
                    levels.put(counter, new Level(counter, spawn, endA, endB));
                } else {
                    plugin.getLogger().warning("Level " + counter + " has an invalid end area");
                }
            } catch (Exception e) {
                System.out.println("ERROR BUILDING LEVEL: " + counter);
                e.printStackTrace();
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
     * runs and checks if the player has completed the level they're currently on. If so, handles the win behavior:
     * updates level as completed and plays fireworks/sounds
     * @param p - player
     */
    public void checkLevelComplete(Player p) {
        UUID u = p.getUniqueId();
        int playerLevel = currentLevels.get(u);
        
        if(playerLevel > numLevels) return;

        if(levels.get(playerLevel).playerInEnd(p)) {
            //stats -> Player,Team,Level,Finish,Time
            int numCompleted = playersCompleted.get(playerLevel);

            String lineEntry = p.getName() + "," + handler.getPlayerTeam(p).getTeamName() + "," + GameMessages.convertLevel(playerLevel) + "," + 
                (numCompleted+1) + "," + ParkourChallenge.timeVar + "," + getLevelFails(p);
            stats.addEntry(lineEntry);

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
                p.sendMessage("You finished all the levels!");
            }
        }
        int numPlayers = 0;
        for(Team t:handler.getTeams()) for(Player pl:t.getOnlinePlayers()) numPlayers++;
        if(playersCompleted.get(numLevels) == numPlayers) {
            //TODO: Game Over!
        }
    }

    public boolean checkFail(Player p) {
        UUID u = p.getUniqueId();
        int playerLevel = currentLevels.get(u);
        if(playerLevel > numLevels) return false;

        Level level = levels.get(playerLevel);

        if(p.getLocation().getY() <= level.lowerY - 20) {
            return true;
        }
        return false;
    }

    /**
     * Increments the amount of time a player has been on a level every second
     * In order to use a skip, there is a minimum amount of time that a player must be on a level for.
     */
    public void incrementTime() {
        for(Team team: TeamHandler.getInstance().getTeams()) {
            for(Player p:team.getOnlinePlayers()) {
                UUID u = p.getUniqueId();
                if(levelTime.containsKey(u)) levelTime.put(u, levelTime.get(u) + 1);
                else levelTime.put(u, 1);
            }
        }
    }

    /**
     * Runs when a player uses their level skip. Updates their level and removes the item from their inventory
     * @param p
     */
    public void onLevelSkip(Player p) {
        UUID u = p.getUniqueId();
        int playerLevel = currentLevels.get(u);
        if(playerLevel >= numLevels) return;

        //Player,Team,Level,Finish,Time,Fails
        String lineEntry = p.getName() + "," + handler.getPlayerTeam(p).getTeamName() + "," + GameMessages.convertLevel(playerLevel) 
            + ",skipped," + ParkourChallenge.timeVar + "," + getLevelFails(p);
        stats.addEntry(lineEntry);

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
            p.sendMessage("You finished all the levels!");
        }
    }

    /**
     * Handles the logic for giving points to a player after completing a level
     * Also handles sending the messages
     * @param p - player
     * @param level - level player just completed
     */
    public void givePoints(Player p, int level) {
        p.setHealth(20);
        p.setFoodLevel(20);
        p.setSaturation(20);
        int numCompleted = playersCompleted.get(level);

        if(handler.getPlayerTeam(p) != null) {
            Team t = handler.getPlayerTeam(p);
            double maxPoints = plugin.getConfig().getDouble("maxPoints") * multiplier;
            double dropOff = plugin.getConfig().getDouble("dropOff") * multiplier;

            double points = maxPoints - (dropOff * numCompleted);

            for (Player player : Bukkit.getOnlinePlayers()) {
                String message;
                if(player.equals(p)) {
                    message = ChatColor.AQUA + "You";
                    message += ChatColor.AQUA + " finished level " + ChatColor.GREEN + GameMessages.convertLevel(level) + ChatColor.AQUA + " in " + ChatColor.GREEN + ChatColor.BOLD;
                } else {
                    message = handler.getPlayerTeam(p).color + p.getName();
                    message += ChatColor.GRAY + " finished level " + GameMessages.convertLevel(level) + " in " + ChatColor.BOLD;
                }

                if(numCompleted % 10 == 0) {
                    message += (numCompleted+1) + "st";
                } else if(numCompleted % 10 == 1) {
                    message += (numCompleted+1) + "nd";
                } else if(numCompleted % 10 == 2) {
                    message += (numCompleted+1) + "rd";
                } else {
                    message += (numCompleted+1) + "th";
                }

                if(player.equals(p)) {
                    message += ChatColor.RESET + "" + ChatColor.AQUA + " place";
                    message += " [" + ChatColor.YELLOW + "" + ChatColor.BOLD + "+" + points + ChatColor.RESET + ChatColor.AQUA + "] points";
                } else {
                    message += ChatColor.RESET + "" + ChatColor.GRAY + " place";
                }
                player.sendMessage(message);
                
            }
            t.addTempPoints(p, points);
        }
    }

    /**
     * Resets a player to the beginning of a level after they fail it
     * @param p - player
     */
    public void resetPlayer(Player p) {
        UUID u = p.getUniqueId();
        int level = currentLevels.get(u);
        if(!levels.containsKey(level)) {
            System.out.println("Level Out of Bounds");
            p.teleport(plugin.getConfig().getLocation("spawn"));
            return;
        }

        p.teleport(levels.get(level).start);

        if(currentFails.containsKey(u) && currentFails.get(u) >= numFails &&
                levelTime.containsKey(u) && levelTime.get(u) >= plugin.getConfig().getInt("levelTime")) {
            giveSkipItem(p);
        }
    }

    /**
     * Adds a fall to a player's count
     * @param p - player
     */
    public void addFail(Player p) {
        UUID u = p.getUniqueId();
        if(totalFails.containsKey(u)) totalFails.put(u, totalFails.get(u) + 1);
        else totalFails.put(u, 1);

        if(currentFails.containsKey(u)) currentFails.put(u, currentFails.get(u) + 1);
        else currentFails.put(u, 1);
    }

    /**
     * Gives a player a skip item, called when they've earned one
     * @param p - player
     */
    public void giveSkipItem(Player p) {
        ItemStack skipItem = new ItemStack(Material.GOLD_BLOCK);
        ItemMeta skipItemMeta = skipItem.getItemMeta();
        skipItemMeta.setDisplayName("Level Skip");
        skipItemMeta.setLore(Collections.singletonList("Skip the level you're currently on. You will not receive points for the level."));
        skipItem.setItemMeta(skipItemMeta);
        p.getInventory().setItem(4, skipItem);
    }

    /**
     * Preps a player, gives them boots and a blaze rod, though blaze rod should be phased out in favor of fake invisibility
     * @param p - player
     */
    public void prepPlayer(Player p) {
        Team team = TeamHandler.getInstance().getPlayerTeam(p);
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

        for(Player target:Bukkit.getOnlinePlayers()) {
            if(TeamHandler.getInstance().getPlayerTeam(target) != team) {
                p.hidePlayer(plugin, target);
                target.hidePlayer(plugin, p);
            }
        }

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
}
