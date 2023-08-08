package io.github.cardsandhuskers.parkourchallenge.commands;

import io.github.cardsandhuskers.parkourchallenge.ParkourChallenge;
import io.github.cardsandhuskers.parkourchallenge.listeners.FinishLineRodClickListener;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;

public class SaveLevelEndCommand implements CommandExecutor {
    private ParkourChallenge plugin;
    Location locA,locB;
    private int level;
    public Type type;


    public SaveLevelEndCommand(ParkourChallenge plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(sender instanceof  Player p && p.isOp()) {
            if(args.length > 0) {
                try {
                    level = Integer.parseInt(args[0]);
                } catch (Exception e) {
                    p.sendMessage(ChatColor.RED + "ERROR: Argument must be an integer");
                    return false;
                }
                type = Type.LEVEL_END;
                initializeCommand(p);
            } else {
                p.sendMessage(ChatColor.RED + "ERROR: Must specify a Level number");
            }

        } else if(sender instanceof Player p) {
            p.sendMessage(ChatColor.RED + "ERROR: You do not have sufficient permission to do this");
        } else {
            System.out.println(ChatColor.RED + "ERROR: Cannot run from console");
        }

        return true;
    }

    public void initializeCommand(Player p) {
        locA = null;
        locB = null;

        PlayerInventory inv = p.getInventory();
        int slot;
        if (inv.getItemInMainHand().getType() == Material.AIR) {
            slot = inv.getHeldItemSlot();
        } else {
            slot = 0;
            for (ItemStack item : inv.getStorageContents()) {
                if (item == null) {
                    break;
                } else {
                    slot++;
                }
            }
        }
        ItemStack selectionRod = new ItemStack(Material.BLAZE_ROD);
        ItemMeta selectionRodMeta = selectionRod.getItemMeta();
        selectionRodMeta.setDisplayName("Parkour Area Selector Rod");
        selectionRodMeta.setLore(Collections.singletonList("Left click for pos1, right click for pos2"));
        selectionRod.setItemMeta(selectionRodMeta);
        inv.setItem(slot, selectionRod);

        p.sendMessage("Use this rod to select a bounding box for the finish line for the level. " +
                "\nLeft click for position 1 and right click for position 2. Type /verifyParkourEnd to confirm your selection");

        FinishLineRodClickListener finishLineRodClickListener = new FinishLineRodClickListener(this);
        Bukkit.getServer().getPluginManager().registerEvents(finishLineRodClickListener, plugin);
        plugin.getCommand("verifyParkourSelection").setExecutor(new VerifyEndCommand(plugin, finishLineRodClickListener, this));


    }

    public void setLocA(Location loc) {
        locA = loc;
    }
    public void setLocB(Location loc) {
        locB = loc;
    }

    public boolean attemptSave(Player p) {
        if(locA == null || locB == null) return false;

        if(type == Type.LEVEL_END) {
            plugin.getConfig().set("end." + level + ".1", locA);
            plugin.getConfig().set("end." + level + ".2", locB);
        } else {
            plugin.getConfig().set("startWall.1", locA);
            plugin.getConfig().set("startWall.2", locB);
        }
        plugin.saveConfig();
        p.sendMessage("Arena " + level + " end set to: " + "(" + (int)locA.getX() + "," + (int)locA.getY() + "," + (int)locA.getZ() + ")" +
                      "  (" + (int)locB.getX() + "," + (int)locB.getY() + "," + (int)locB.getZ() + ")");

        locA = null;
        locB = null;

        Inventory inv = p.getInventory();
        for(ItemStack i : inv.getContents()) {
            if(i != null && i.getItemMeta().getDisplayName().equalsIgnoreCase("Parkour Area Selector Rod")) {
                inv.remove(i);
                break;
            }
        }
        return true;
    }
    enum Type {
        START_WALL,
        LEVEL_END
    }
}
