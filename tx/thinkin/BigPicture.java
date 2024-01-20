package tx.thinkin;

import battlecode.common.*;
import tx.Cowboy;
import tx.map.MapScribbles;
import tx.map.AintSeenIt;

import java.util.List;

/**
 *  Just a list of stuff that we're concerned about.
 *
 *  yep, it ain't pretty, but neither is life. Get over it.
 */
public strictfp class BigPicture {

    final private MapScribbles[][] map;
    public final int mapWidth,mapHeight;
    /** bad guys*/
    public List<RobotInfo> muchachos ;

    public Team myTeam;


    /**Homies brah*/
    public List<RobotInfo> compadres ;

    /**
     * How much damage could surrounding enemies do to you this turn.
     * Figured in {@link tx.thinkin.idears.RaiseHell#howMuchTroubleWeStirUpAnyways(BigPicture, RobotController)}
     */
    public int trouble;
    public MapLocation lastLocation;

    public MapLocation closestSpawn;
    public int closestSpawnDist;

    public MapLocation closestAllyFlag;
    public int closestAllyFlagDist;

    //############# START bot statistics ####################
    public int closestAllyDist;
    public RobotInfo closestAlly;

    public RobotInfo closestHealthyAlly;
    public int closestHealthyAllyDist;
    public int closestDmgAllyDist;
    public RobotInfo closestDamagedAlly;
    public int closestEnemyDist;
    public RobotInfo closestEnemy;
    public int mostDamagedEnemyInRngHp;
    public RobotInfo mostDamagedEnemyInRange;

    public void clearRobotStatistics(){
         closestAllyDist = Integer.MAX_VALUE;
         closestAlly=null;
         closestDmgAllyDist = Integer.MAX_VALUE;
         closestDamagedAlly=null;
         closestEnemyDist = Integer.MAX_VALUE;
         closestEnemy=null;
         mostDamagedEnemyInRngHp = GameConstants.DEFAULT_HEALTH;
         mostDamagedEnemyInRange=null;
    }

    public void addBotToMap(RobotInfo info){
        MapScribbles loc = getLocalInfo(info.getLocation().x,info.getLocation().y);
        loc.setOccupied(true);
        loc.setOccupantTeam(info.getTeam());
    }

    public void removeBotFromMap(RobotInfo info){
        MapScribbles loc = getLocalInfo(info.getLocation().x,info.getLocation().y);
        loc.setOccupied(false);
        loc.setOccupantTeam(Team.NEUTRAL);
    }
    public void addEnemyStat(RobotInfo enemy, RobotController rc){
        int dist = enemy.getLocation().distanceSquaredTo(rc.getLocation());
        if(closestEnemy==null ){
            closestEnemy = enemy;
            closestEnemyDist = dist;
        } else if ( dist < closestSpawnDist) {
            closestEnemy = enemy;
            closestEnemyDist = dist;
        }
        if(dist < GameConstants.ATTACK_RADIUS_SQUARED ){
            if(mostDamagedEnemyInRange == null){
                mostDamagedEnemyInRange = enemy;
                mostDamagedEnemyInRngHp = enemy.getHealth();
            } else if ( enemy.getHealth() < mostDamagedEnemyInRngHp ){
                mostDamagedEnemyInRange = enemy;
                mostDamagedEnemyInRngHp = enemy.getHealth();
            }
        }
    }

    public void addAllyStat(RobotInfo ally, RobotController rc){
        int dist = ally.getLocation().distanceSquaredTo(rc.getLocation());
        if(closestAlly==null ){
            closestAlly = ally;
            closestAllyDist = dist;
        } else if ( dist < closestSpawnDist) {
            closestAlly = ally;
            closestAllyDist = dist;
        }
    }

    /**
     * must happen AFTER finding all allies and muchachos are found.
     * Very useful in finding good retreat places.
     * @param rc
     */
    public void findClosestSafeHealthyAlly(RobotController rc){
        int dist = Integer.MAX_VALUE;
        for(RobotInfo ally : compadres) {
            if (ally.getHealth() > Cowboy.APPETITE_FOR_PUNISHMENT ) {
                dist = ally.getLocation().distanceSquaredTo(rc.getLocation());
                boolean isHealzSafe = true;
                for(RobotInfo muchacho : muchachos){
                    if(muchacho.getLocation().distanceSquaredTo(ally.getLocation())<GameConstants.ATTACK_RADIUS_SQUARED){
                        isHealzSafe = false;
                    }
                }
                if(isHealzSafe) {
                    if (closestHealthyAlly == null) {
                        closestHealthyAlly = ally;
                        closestHealthyAllyDist = dist;
                    } else if (dist < closestHealthyAllyDist) {
                        closestHealthyAlly = ally;
                        closestHealthyAllyDist = dist;
                    }
                }
            }
        }
    }


    //############# END bot statistics ####################
    public MapLocation closestEnemyFlag;
    public int closestEnemyFlagDist;
    public RobotInfo friendInNeed = null;

    public BigPicture(int mapWidth, int mapHeight) {
        this.mapWidth = mapWidth;
        this.mapHeight = mapHeight;
        map = new MapScribbles[mapWidth][mapHeight];
    }

    public MapScribbles getLocalInfo(int x, int y){
        if(isOffMap(x,y)){
            throw new ArrayIndexOutOfBoundsException("You're checkin' something off the map. ["+x+","+y+"]");
        }
        MapScribbles loc = map[x][y];
        if(loc == null){ // Lazy load map info.
            loc = new MapScribbles( new AintSeenIt(x,y) );
            map[x][y] = loc;
        }
        return loc;
    }
    public MapScribbles getLocalInfo (MapLocation location) throws ArrayIndexOutOfBoundsException{
        return getLocalInfo(location.x,location.y);
    }

    public void updateLocalMap(MapInfo info) {
        MapScribbles loc = map[info.getMapLocation().x][info.getMapLocation().y];
        if (loc == null) {
            map[info.getMapLocation().x][info.getMapLocation().y] = new MapScribbles(info);
        } else {
            loc.setInfo(info);
        }
    }

    public boolean isOffMap(MapLocation location) {
        return isOffMap(location.x,location.y);
    }

    public boolean isOffMap(int x , int y) {
        return x < 0 || y < 0 || x >= mapWidth || y >= mapHeight;
    }

}
