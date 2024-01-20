package tx;

import tx.comms.TurnCount;
import tx.map.MapScribbles;
import tx.thinkin.BigPicture;
import tx.thinkin.Noggin;
import tx.comms.CommsUtil;
import tx.thinkin.idears.BrightIdea;
import battlecode.common.*;
import tx.map.AintSeenIt;

import java.util.LinkedList;
import java.util.List;

import static java.lang.StrictMath.ceil;

/**
 * Yeehaw!!!
 *
 */
public strictfp class Cowboy {
    public static final int INVALID_MAP_MESSAGE = 0b1111_1111_1111_1111;
    /**I think, therefore I am*/
    public RobotController me;
    /**That's usin' your noodle!*/
    public Noggin thinker;
    /**Big sky and broad plains.*/
    public BigPicture layOfTheLand;

    public final TurnCount turnCount;
    private CommsUtil comms;
    public static final int APPETITE_FOR_PUNISHMENT = 300 ; // about 3 hits

    public Cowboy(RobotController me, TurnCount turnCount){
        this.me = me;
        this.turnCount = turnCount;
    }


    public void wakeup(){
        layOfTheLand = new BigPicture(me.getMapWidth(),me.getMapHeight());
        pencilInMap(); // you have cycles on startup. USE THEM. Don't lazy load because we'll need those cycles more after turn 200.
        layOfTheLand.myTeam = me.getTeam();
        comms = new CommsUtil( me.getMapHeight() , me,turnCount);
        thinker = new Noggin(layOfTheLand, this); // TODO This circular dependency is probably less than good :(
        findChunkSize(me.getMapWidth(),me.getMapHeight(),GameConstants.SHARED_ARRAY_LENGTH / 2);
    }

    private void pencilInMap() {
        for (int i = 0 ; i< me.getMapWidth() ; i++ ){
            for (int j = 0 ; j < me.getMapHeight() ; j++){
                layOfTheLand.updateLocalMap(new AintSeenIt(i,j));
            }
        }
    }


    /**
     * TODO We need to chunk the map eventually...this way, I guess.
     * This calculates how big the chunks need to be to fit in 1/2 the shared array (some arbitrary ratio...maybe adjust later)
     *
     * Probably belongs somewhere else...but we'll get there when we get there.
     *
     * The math is kinda funky, and it doesn't catch the corners well AT ALL.
     * @return
     */
    private int findChunkSize(int maxWidth, int maxHeight , int alottedMemory) {
        /**The lot is always a square, so the W and H are the same. 12 is what I came up with for 60X60 @ 32 blocks. +2 is just 15% extra in case game specs change... as always*/
        int minLotWidth = 12 + 2;
        // Try to divide the map into bigger squares.
        // Just check until progressively larger values until you find one that works for this particular map.
        // P.S. The top and the right side of the map won't usually map out perfectly...So lets just take the center ground.
        for (int i = 6 ; i <= 12 ; i++){
            if(( ceil(maxWidth/i) * ceil(maxHeight/i)) < alottedMemory){
                minLotWidth = i;
                break;
            }
        }
        return minLotWidth;
    }


    public void addABotLocation(RobotInfo botinfo){

    }
    public void move(Direction dir) throws GameActionException {
        if(dir == null){
            System.err.println("Tried to move in a null direction...didn't");
            me.setIndicatorDot(me.getLocation(),255,0,0);
            return;
        }

        if(me.canMove(dir)){
            layOfTheLand.lastLocation = me.getLocation();
            me.move(dir);
        } /*else if (me.canMove(dir.rotateLeft())){
            layOfTheLand.lastLocation = me.getLocation();
            me.move(dir.rotateLeft());
        } else if (me.canMove(dir.rotateRight())) {
            layOfTheLand.lastLocation = me.getLocation();
            me.move(dir.rotateRight());
        }else if (me.canMove(dir.rotateLeft().rotateLeft())){
            layOfTheLand.lastLocation = me.getLocation();
            me.move(dir.rotateLeft().rotateLeft());
        } else if (me.canMove(dir.rotateRight().rotateRight())) {
            layOfTheLand.lastLocation = me.getLocation();
            me.move(dir.rotateRight().rotateRight());
        }*/
    }
    public BrightIdea takeStock(){
        try {
            RobotInfo[] folksRoundHere = me.senseNearbyRobots(-1, null);
            removeOldRobotsFromMap();
            layOfTheLand.compadres = new LinkedList<>();
            layOfTheLand.muchachos = new LinkedList<>();
            layOfTheLand.clearRobotStatistics();

            for (RobotInfo bot : folksRoundHere) {
                if (bot.getTeam() == me.getTeam()) {
                    layOfTheLand.addAllyStat(bot,me);
                    layOfTheLand.compadres.add(bot);
                } else {
                    layOfTheLand.addEnemyStat(bot,me);
                    layOfTheLand.muchachos.add(bot);
                }
                MapScribbles scbl = layOfTheLand.getLocalInfo(bot.getLocation());
                scbl.setOccupied(true);
                scbl.setOccupantTeam(bot.getTeam());
            }

            findClosestSpawn();
            findClosestAllyFlag();
            findClosestEnemyFlag();

            me.senseNearbyFlags(GameConstants.VISION_RADIUS_SQUARED , me.getTeam());

            scanAndUpdateMap();
        } catch (GameActionException e){
            System.out.println("Trouble Lookin' Around");
            System.out.println(e + "\n");
            e.printStackTrace();
            // kinda useless, but it won't crash, I guess.
            //layOfTheLand = new BigPicture();
            layOfTheLand.muchachos = new LinkedList<>();
            layOfTheLand.compadres = new LinkedList<>();
        }

        return thinker.ponder(layOfTheLand);
    }

    private void removeOldRobotsFromMap() {
        if(layOfTheLand.compadres!=null && !layOfTheLand.compadres.isEmpty()) {
            for (RobotInfo compadre : layOfTheLand.compadres) {
                layOfTheLand.removeBotFromMap(compadre);
            }
        }
        if(layOfTheLand.muchachos!=null && !layOfTheLand.muchachos.isEmpty()) {
            for (RobotInfo muchacho : layOfTheLand.muchachos) {
                layOfTheLand.removeBotFromMap(muchacho);
            }
        }
    }

    private void findClosestSpawn() {
        layOfTheLand.closestSpawn = findClosest(me.getAllySpawnLocations(),me);
        if(layOfTheLand.closestSpawn!=null)
            layOfTheLand.closestSpawnDist = me.getLocation().distanceSquaredTo(layOfTheLand.closestSpawn);
        else
            layOfTheLand.closestSpawnDist = Integer.MAX_VALUE;
    }

    private void findClosestAllyFlag() throws GameActionException {

        layOfTheLand.closestAllyFlag = findClosest(me.senseNearbyFlags(GameConstants.VISION_RADIUS_SQUARED,me.getTeam()),me);
        if(layOfTheLand.closestAllyFlag !=null)
            layOfTheLand.closestAllyFlagDist = me.getLocation().distanceSquaredTo(layOfTheLand.closestAllyFlag);
        else
            layOfTheLand.closestAllyFlagDist = Integer.MAX_VALUE;
    }

    private void findClosestEnemyFlag() throws GameActionException {
        layOfTheLand.closestEnemyFlag = findClosest(me.senseNearbyFlags(GameConstants.VISION_RADIUS_SQUARED,me.getTeam().opponent()),me);
        if(layOfTheLand.closestEnemyFlag !=null)
            layOfTheLand.closestEnemyFlagDist = me.getLocation().distanceSquaredTo(layOfTheLand.closestEnemyFlag);
        else
            layOfTheLand.closestEnemyFlagDist = Integer.MAX_VALUE;
    }
    public static MapLocation findClosest(FlagInfo[] locs , RobotController me){
        MapLocation[] flags = new MapLocation[locs.length];
        for(int i =0 ; i< locs.length ; i++){
            flags[i] = locs[i].getLocation();
        }
        return findClosest(flags,me);
    }

    public static MapLocation findClosest(MapLocation[] locs , RobotController me){
        int closestDist = Integer.MAX_VALUE;
        MapLocation closest = null;
        int dist = Integer.MAX_VALUE;
        for(MapLocation loc : locs){
            dist = loc.distanceSquaredTo(me.getLocation());
            if(closest==null || dist<closestDist){
                closest = loc;
                closestDist = dist;
            };
        }
        return closest;
    }

    private void scanAndUpdateMap() throws GameActionException {
        if(turnCount.isSetupRound()) {
            MapInfo[] infos = me.senseNearbyMapInfos(); // expensive...but screw it, we can optimize scanning stuff later.
            for(MapInfo mapInfo : infos ){
                if(hasBeenSeen(mapInfo)) {
                    layOfTheLand.updateLocalMap(mapInfo);
                    shareFindings(mapInfo);
                }
            }
            comms.flushArrayBuffer();
        }
    }

    private void shareFindings(MapInfo info) throws GameActionException {
        try {
            if( comms.canWrite() && isLandInteresting(info)
            ){
                comms.writeToArrayBuffer(info);
            }
        } catch (CommsUtil.ForgotToInitMapSize e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean isLandInteresting(MapInfo info) {
        return info.getTrapType() != TrapType.NONE
                || !info.isPassable()
                || info.isSpawnZone();
    }


    private boolean hasBeenSeen(MapInfo info) {
        return layOfTheLand.getLocalInfo(info.getMapLocation()).getInfo() instanceof AintSeenIt;
    }

    public void readNews(){
        // In the setup rounds, everything is map awareness.
        if( turnCount.isSetupRound() ) {
            updateMapInfo();
        }
    }

    private void updateMapInfo() {
        try {
            List<MapInfo> updates = comms.readAllAsMapInfo();
            for(MapInfo update : updates){
                layOfTheLand.updateLocalMap(update);
                me.setIndicatorLine(me.getLocation(),update.getMapLocation(),100,25,100);
            }
        } catch (GameActionException | CommsUtil.ForgotToInitMapSize e) {
            e.printStackTrace();
            System.err.println(e);
        }
    }

    private boolean isLocationInMap(MapLocation loc) {
        return loc.x < me.getMapWidth() && loc.y < me.getMapHeight() && loc.x >= 0 && loc.y >= 0;
    }

}
