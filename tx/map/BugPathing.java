package tx.map;

import battlecode.common.*;
import tx.RobotPlayer;
import tx.thinkin.BigPicture;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

/**
 * Shooting for simple Bug or A*
 *
* X = player
* <- = wanted direction
* ## = wall
* CC = CounterClockwise obstacle check start
* CW = ClockWise obstacle check Start
* T = Target

 */
public class BugPathing implements PathFinding {
    public static final int NUMBER_OF_DIRECTIONS = 8;
    private final BigPicture layOfTheLand;
    private final RobotController me;

    private static final int MAX_HISTORY_SIZE=32;
    public static final int HISTORY_HASH_TABLE_INIT = (int) (MAX_HISTORY_SIZE * 1.3); // 1.3 came from Google...I have no idea if this is good.
    Queue<MapLocation> historyQueue = new ArrayDeque<>(MAX_HISTORY_SIZE);
    Set<MapLocation> historySet = new HashSet<>(HISTORY_HASH_TABLE_INIT);

    public BugPathing(BigPicture layOfTheLand , RobotController rc) {
        this.layOfTheLand = layOfTheLand;
        this.me = rc;
    }

    boolean isFollowing = false;

    /** Take distance to target when first encounter wall.  Don't stop following the wall until you are closer than this distance. */
    int wallFollowInitialDistance = Integer.MAX_VALUE;
    Direction moveDir;

    boolean counterClockWise = true;

    /**
     * Stupidly bumble in the general direction of something.
     * @param target
     * @return
     * @throws Exception - You are walled in. Trapped. There is no escape.
     * <pre>
     *     |==|==|==|
     *     |==|X |==|
     *     |==|==|==|
     * </pre>
     */
    public Direction go(MapLocation target) throws Exception {
        Direction dir = loggedGo(target);
        printLog();
        return dir;
    }
    private Direction loggedGo(MapLocation target) throws Exception{

        Direction beeline = me.getLocation().directionTo(target);
        MapLocation desiredLoc = me.getLocation().add(beeline);
        MapInfo mapInfo = layOfTheLand.getLocalInfo(desiredLoc);

        Direction moveDir = beeline;
        try {
            if(!isFollowing){
                if( canTravel(mapInfo) ) {
                    log("Following Beeline");
                    return beeline;
                } else {
                    startFollowingWall(target, beeline);
                    log("START follow wall");
                }
            }

            if(isFollowing){
                if(isClearOfObstacle(target, desiredLoc, mapInfo)){
                    stopFollowingWall();
                   log("STOP follow wall.");
                    clearHistory();
                } else {
                    log("FOLLOWING wall");
                    moveDir = followWall();
                }
            }
        } catch (Exception e) {
           log("\n I'M STUCK");
        }
        return moveDir;
    }

    private void addToHistory(MapLocation loc){
        if(historyQueue.size() == MAX_HISTORY_SIZE){
            historySet.remove( historyQueue.poll() );
        }

        if(historyQueue.offer(loc)){
            historySet.add(loc);
        }

    }

    private void clearHistory(){
        historyQueue.clear();
        historySet.clear();
    }

    /**
     * I haven't proved this yet...but I think this should work
     * @param target
     * @param desiredLoc
     * @param mapInfo
     * @return
     */
    private boolean isClearOfObstacle(MapLocation target, MapLocation desiredLoc, MapInfo mapInfo) throws GameActionException {
        return !layOfTheLand.isOffMap(desiredLoc)
                && desiredLoc.distanceSquaredTo(target) < wallFollowInitialDistance
                && canTravel(mapInfo);
    }

    /**
     *<pre>
     * |##|  |  |CC|
     * |##|<-|X |  |
     * |##|  |  |CW|
     * Notice how we don't check DIRECTLY behind first.
     * because in narrow passages it will select the wrong NEXT location in infinite loop.
     * |##|##|##|  |  |
     * |##|  |  | X|->
     * |##|##|##|  |  |
     * <pre/>
     */
    private Direction followWall() throws Exception {
        Direction checkDir = moveDir.opposite() ;
        MapInfo checkBlock = null ;
        int howManyDirsChecked = 0;
        //rotate - find first TRAVERSABLE block
        do{
            howManyDirsChecked = surroundedCheck(howManyDirsChecked);
            checkDir = nextDirection(checkDir);
            MapLocation checkLocation = me.getLocation().add(checkDir);
            if(!layOfTheLand.isOffMap(checkLocation)) {
                checkBlock = layOfTheLand.getLocalInfo(checkLocation);
            }
        } while( !canTravel(checkBlock) );

        //rotate - find fist OBSTACLE
        do{
            howManyDirsChecked = surroundedCheck(howManyDirsChecked);
            checkDir = nextDirection(checkDir);
            MapLocation checkLocation = me.getLocation().add(checkDir);
            if(!layOfTheLand.isOffMap(checkLocation)) {
                checkBlock = layOfTheLand.getLocalInfo(checkLocation);
            }
        } while( canTravel(checkBlock) );

        // rotate back to the last block, which was TRAVERSABLE
        if(historySet.contains(checkBlock.getMapLocation())){
            counterClockWise = RobotPlayer.rng.nextBoolean();// Try going another way if it doesn't work
        }
        addToHistory(checkBlock.getMapLocation());
        return prevDirection(checkDir);
    }

    /**
     * Once you've checked all the directions, accept that there is nowhere to go.
     * Accept it.
     * Throw an exception so you're bot doesn't burn bytecodes needlessly trying to calculate this.
     * @param howManyDirsChecked
     * @return
     * @throws Exception
     */
    private static int surroundedCheck(int howManyDirsChecked) throws Exception {
        howManyDirsChecked++;
        if(howManyDirsChecked >NUMBER_OF_DIRECTIONS){ // during heavy traffic, sometimes you want to move back.
            throw new Exception("This bot is in an enclosed circle and cannot move.");
        }
        return howManyDirsChecked;
    }


    private boolean canTravel(MapInfo checkBlock) throws GameActionException {
        return checkBlock != null
                && !layOfTheLand.isOffMap(checkBlock.getMapLocation())
                && checkBlock.isPassable()
                && !me.isLocationOccupied(checkBlock.getMapLocation());
    }


    Direction nextDirection(Direction dir){
        if(counterClockWise){
            return dir.rotateRight();
        } else {
            return dir.rotateLeft();
        }
    }

    Direction prevDirection(Direction dir){
        if(counterClockWise){
            return dir.rotateLeft();
        } else {
            return dir.rotateRight();
        }
    }


    private void startFollowingWall(MapLocation target, Direction beeline) {
        isFollowing = true;
        wallFollowInitialDistance = me.getLocation().distanceSquaredTo(target);
        moveDir = beeline;
        counterClockWise = RobotPlayer.rng.nextBoolean();
    }

    private void stopFollowingWall() {
        isFollowing = false;
        wallFollowInitialDistance = Integer.MAX_VALUE;
    }

    StringBuilder logString = new StringBuilder();
    public boolean IS_LOG_ENABLED = false;
    private void log(String log){
        if (IS_LOG_ENABLED) {
            logString.append(log);
        }
    }

    private void printLog(){
        if (IS_LOG_ENABLED) {
            me.setIndicatorString(logString.toString());
            logString = new StringBuilder();
        }
    }
}
