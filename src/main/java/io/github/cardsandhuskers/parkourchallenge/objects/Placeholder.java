package io.github.cardsandhuskers.parkourchallenge.objects;

import io.github.cardsandhuskers.parkourchallenge.ParkourChallenge;
import io.github.cardsandhuskers.parkourchallenge.commands.StartGameCommand;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;

import static io.github.cardsandhuskers.parkourchallenge.ParkourChallenge.*;

public class Placeholder extends PlaceholderExpansion {
    private final ParkourChallenge plugin;
    StartGameCommand startGameCommand;

    public Placeholder(ParkourChallenge plugin, StartGameCommand startGameCommand) {
        this.plugin = plugin;
        this.startGameCommand = startGameCommand;
    }


    @Override
    public String getIdentifier() {
        return "Parkour";
    }
    @Override
    public String getAuthor() {
        return "cardsandhuskers";
    }
    @Override
    public String getVersion() {
        return "1.0.0";
    }
    @Override
    public boolean persist() {
        return true;
    }


    @Override
    public String onRequest(OfflinePlayer p, String s) {

        if(s.equalsIgnoreCase("timer")) {
            int time = timeVar;
            int mins = time / 60;
            String seconds = String.format("%02d", time - (mins * 60));
            return mins + ":" + seconds;
        }

        if(s.equalsIgnoreCase("timerstage")) {
            switch(gameState) {
                case GAME_STARTING:
                    return "Game Starts";
                case GAME_IN_PROGRESS:
                    return "Game Ends";
                case GAME_OVER:
                    return "Return To Lobby";
                default:
                    return "Game";
            }
        }

        if(s.equalsIgnoreCase("levelnum")) {
            int level = startGameCommand.gameStageHandler.levelHandler.getCurrentLevel((Player) p);
            return GameMessages.convertLevel(level);
        }
        if(s.equalsIgnoreCase("levelfails")) {
            return startGameCommand.gameStageHandler.levelHandler.getLevelFails((Player) p) + "";
        }
        if(s.equalsIgnoreCase("totalfails")) {
            return startGameCommand.gameStageHandler.levelHandler.getTotalFails((Player) p) + "";
        }
        String[] values = s.split("_");
        try {
            //falls, wins
            //example: Parkour_falls_1
            if(values[0].equalsIgnoreCase("fails")) {
                ArrayList<StatCalculator.PlayerStatsHolder> statsHolders = plugin.statCalculator.getStatsHolders(StatCalculator.PlayerStatsComparator.SortType.FAILS);
                int index = Integer.parseInt(values[1]);
                if(index > statsHolders.size()) return "";
                StatCalculator.PlayerStatsHolder holder = statsHolders.get(Integer.parseInt(values[1]) - 1);

                String color = "";
                if (handler.getPlayerTeam(Bukkit.getPlayer(holder.name)) != null)
                    color = handler.getPlayerTeam(Bukkit.getPlayer(holder.name)).color;
                return color + holder.name + ChatColor.RESET + ": " + holder.fails;

            }
            if(values[0].equalsIgnoreCase("wins")) {
                ArrayList<StatCalculator.PlayerStatsHolder> statsHolders = plugin.statCalculator.getStatsHolders(StatCalculator.PlayerStatsComparator.SortType.WINS);
                int index = Integer.parseInt(values[1]);
                if(index > statsHolders.size()) return "";
                StatCalculator.PlayerStatsHolder holder = statsHolders.get(Integer.parseInt(values[1]) - 1);

                String color = "";
                if (handler.getPlayerTeam(Bukkit.getPlayer(holder.name)) != null)
                    color = handler.getPlayerTeam(Bukkit.getPlayer(holder.name)).color;
                return color + holder.name + ChatColor.RESET + ": " + holder.wins;
            }
        } catch (Exception e) {

        }


        return null;
    }
}
