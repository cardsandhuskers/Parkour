package io.github.cardsandhuskers.parkourchallenge.objects;

import io.github.cardsandhuskers.parkourchallenge.ParkourChallenge;
import io.github.cardsandhuskers.parkourchallenge.commands.StartGameCommand;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import static io.github.cardsandhuskers.teams.Teams.handler;

public class StatCalculator {
    private ParkourChallenge plugin;
    private ArrayList<PlayerStatsHolder> playerStatsHolders;
    private StartGameCommand startGameCommand;

    public StatCalculator(ParkourChallenge plugin, StartGameCommand startGameCommand) {
        this.plugin = plugin;
        this.startGameCommand = startGameCommand;
    }

    public void calculateStats() throws Exception{
        HashMap<String, PlayerStatsHolder> playerStatsMap = new HashMap<>();
        FileReader reader = null;
        try {
            reader = new FileReader(plugin.getDataFolder() + "/stats.csv");
        } catch (IOException e) {
            plugin.getLogger().warning("Stats file not found!");
            return;
        }
        String[] headers = {"Event", "Team", "Name", "Wins", "Fails"};

        CSVFormat.Builder builder = CSVFormat.Builder.create();
        builder.setHeader(headers);
        CSVFormat format = builder.build();

        CSVParser parser;
        try {
            parser = new CSVParser(reader, format);
        } catch (IOException e) {
            throw new Exception(e);
        }
        List<CSVRecord> recordList = parser.getRecords();

        try {
            reader.close();
        } catch (IOException e) {
            throw new Exception(e);
        }

        for(CSVRecord r:recordList) {
            if (r.getRecordNumber() == 1) continue;
            String name = r.get(2);
            if(playerStatsMap.containsKey(name)) {
                playerStatsMap.get(name).wins += Integer.parseInt(r.get(3));
                playerStatsMap.get(name).fails += Integer.parseInt(r.get(4));
            }
            else playerStatsMap.put(name, new PlayerStatsHolder(name, Integer.parseInt(r.get(3)), Integer.parseInt(r.get(4))));
        }
        playerStatsHolders = new ArrayList<>(playerStatsMap.values());
        Comparator playerStatsComparator = new PlayerStatsComparator(PlayerStatsComparator.SortType.WINS);
        playerStatsHolders.sort(playerStatsComparator);
        Collections.reverse(playerStatsHolders);

    }

    public void saveRecords() throws IOException {
        //for(Player p:wins.keySet()) if(p != null) System.out.println(p.getDisplayName() + ": " + wins.get(p));
        //System.out.println("~~~~~~~~~~~~~~~");

        FileWriter writer = new FileWriter(plugin.getDataFolder() + "/stats.csv", true);
        FileReader reader = new FileReader(plugin.getDataFolder() + "/stats.csv");

        String[] headers = {"Event", "Team", "Name", "Wins", "Fails"};

        CSVFormat.Builder builder = CSVFormat.Builder.create();
        builder.setHeader(headers);
        CSVFormat format = builder.build();

        CSVParser parser = new CSVParser(reader, format);

        if(!parser.getRecords().isEmpty()) {
            format = CSVFormat.DEFAULT;
        }

        CSVPrinter printer = new CSVPrinter(writer, format);

        int eventNum;
        try {eventNum = Bukkit.getPluginManager().getPlugin("LobbyPlugin").getConfig().getInt("eventNum");} catch (Exception e) {eventNum = 1;}
        //printer.printRecord(currentGame);
        for(Player p:Bukkit.getOnlinePlayers()) {
            if(p == null) continue;
            if(handler.getPlayerTeam(p) == null) continue;
            int numWins = startGameCommand.gameStageHandler.levelHandler.getPlayerWins(p);
            int numFails = startGameCommand.gameStageHandler.levelHandler.getTotalFails(p);
            System.out.println("WINS: " + numWins + "\nFAILS: " + numFails);

            printer.printRecord(eventNum, handler.getPlayerTeam(p).getTeamName(), p.getDisplayName(), numWins, numFails);
        }
        writer.close();
        try {
            plugin.statCalculator.calculateStats();
        } catch (Exception e) {
            StackTraceElement[] trace = e.getStackTrace();
            String str = "";
            for(StackTraceElement element:trace) str += element.toString() + "\n";
            plugin.getLogger().severe("ERROR Calculating Stats!\n" + str);
        }

    }

    public ArrayList<PlayerStatsHolder> getStatsHolders(PlayerStatsComparator.SortType sortType) {
        ArrayList<PlayerStatsHolder> psh = new ArrayList<>(playerStatsHolders);
        Comparator playerStatsCompare = new PlayerStatsComparator(sortType);
        psh.sort(playerStatsCompare);
        Collections.reverse(psh);
        return psh;
    }

    public class PlayerStatsHolder {
        int wins, fails;
        String name;
        public PlayerStatsHolder(String name, int wins, int fails) {
            this.name = name;
            this.wins = wins;
            this.fails = fails;
        }
    }

    public class PlayerStatsComparator implements Comparator<PlayerStatsHolder> {
        public SortType sortType;
        public PlayerStatsComparator(SortType sortType) {
            this.sortType = sortType;
        }
        public int compare(PlayerStatsHolder h1, PlayerStatsHolder h2) {
            if(sortType == SortType.WINS) {
                int compare = Integer.compare(h1.wins, h2.wins);
                if(compare == 0) compare = h1.name.compareTo(h2.name);
                return compare;
            } else {
                int compare = Integer.compare(h1.fails, h2.fails);
                if(compare == 0) compare = h1.name.compareTo(h2.name);
                return compare;
            }
        }
        enum SortType {
            WINS,
            FAILS
        }
    }
}
