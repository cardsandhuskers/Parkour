# Parkour
Plugin for 1.20.1 to set up a parkour course 

## Commands:
**/startParkour [multiplier]**
- Starts the GameType with a multiplier, which can be any double.

**/setParkourSpawn:**
- sets the world spawn location to where you're standing

**/setParkourLobby**
- sets the lobby location to where you're standing

**/reloadParkour**
- reloads config for parkour

**/cancelParkour**
- description: cancels the parkour game

**/setParkourLevelStart [levelNum]**
- sets the start point for a level to where you're standing

**/setParkourLevelEnd [levelNum]**
- gives you a rod to mark the area at the end of a level. Left click on one corner of your area and right click on the other. The area between the points becomes the level end

**/setParkourStartWall**
- gives you a rod to mark the area to place a wall to block people from starting the parkour until the start time

**/verifyParkourSelection**
- enter to verify your selection with the rod for the level end or start wall

## PlaceholderAPI Hooks
%Parkour_timer% - returns current time remaining
<br>%Parkour_timerstage% - returns the stage of the game
<br>%Parkour_levelnum% - returns the level number that the player is on
<br>%Parkour_levelfails% - returns the number of times the player has failed the level they're on
<br>%Parkour_totalfails% - returns the number of times the player has failed on the parkour course

### Stat Learerboard Hooks
<br>%Parkour_falls_[num]% - returns the number of falls the person in that place has along with their username (falls leaderboard)
<br>%Parkour_falls_[num]% - returns the number of level wins (levels completed in first place) the person in that place has along with their username (wins leaderboard)


## Dependencies:
- Teams Plugin (https://github.com/cardsandhuskers/TeamsPlugin)
    - note: this must be manually set up as a local library on your machine to build this plugin
- Protocollib
- optional: PlaceholderAPI for scoreboard placeholders

## Compilation

Download the teams plugin and set Dfile to the location of the jar file

```
mvn install:install-file -Dfile="TeamsPlugin.jar" -DgroupId=io.github.cardsandhuskers -DartifactId=Teams -Dversion=1.0-SNAPSHOT -Dpackaging=jar -DgeneratePom=true
```

Once the neccessary project has been established, type:

```
mvn package
```
