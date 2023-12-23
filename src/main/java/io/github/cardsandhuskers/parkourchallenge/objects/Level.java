package io.github.cardsandhuskers.parkourchallenge.objects;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public class Level {
    public int levelNum;
    public Location start;
    public int lowerX, lowerY, lowerZ, higherX, higherY, higherZ;
    public Level(int levelNum, Location start, Location endA, Location endB) {
        this.levelNum = levelNum;
        this.start = start;
        setCoordinates(endA, endB);

    }

    /**
     * Sets ending box coordinates, calculates the lower coordinate on each axis
     * @param locA - corner A
     * @param locB - corner B
     */
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

    /**
     * Checks if the player is in the end of this level
     * @param p - player
     * @return boolean - if player is in the end of the level
     */
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
