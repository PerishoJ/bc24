package tx.map;

import battlecode.common.Direction;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.TrapType;
import tx.thinkin.BigPicture;
import tx.util.ByteCodeLimiter;
import tx.util.ByteCodeLimiterIF;
import tx.util.OutOfTimeException;

import java.util.Objects;
import java.util.PriorityQueue;

public class TrailBlazer {
    public boolean shouldIgnorePain = false;
    public static final int HITS_TO_KILL_A_BOT = GameConstants.DEFAULT_HEALTH / 150;
    public static final int TURN_COST_OF_ATTACK = (GameConstants.ATTACK_COOLDOWN / 10);
    public static final int TIME_IT_TAKES_TO_MURDER = HITS_TO_KILL_A_BOT * TURN_COST_OF_ATTACK;
    public static final int WATER_HAZARD = 2;
    public static final int EXPLODE_DAMAGE = 750;
    public static final int HEALTH_PER_HEAL = 80;
    public static final int HEALING_COOLDOWN_TURNS = 3;
    BigPicture layOfTheLand;

    private int pathID = 0;

    MapScribbles current = null, goal = null;

    public ByteCodeLimiterIF getLimiter() {
        return limiter;
    }

    public void setLimiter(ByteCodeLimiterIF limiter) {
        this.limiter = limiter;
    }

    ByteCodeLimiterIF limiter ;
    PriorityQueue<MapScribbles> placeToCheck = new PriorityQueue<>();
    public TrailBlazer(BigPicture layOfTheLand) {
        this.layOfTheLand = layOfTheLand;
        limiter = new ByteCodeLimiter();
    }



    public int blazeATrail(MapLocation start, MapLocation destination) throws OutOfTimeException {
        return blazeATrail(start.x,start.y,destination.x,destination.y);
    }

    /**
     * This SHOULD map the map with a trail, and you SHOULD be at the beginning of it.
     * But shit happens.
     * Returns a Request ID to can verify the trail of any given {@link MapScribbles}
     *
     * @param startX start loc, x coord
     * @param startY start loc, y coord
     * @param endX end loc, x coord
     * @param endY end loc, y coord
     * @return
     */
    public int blazeATrail(int startX, int startY, int endX, int endY) throws OutOfTimeException {
        limiter.resetClock();
        int requestID = Objects.hash(startX,startY,endX,endY);
        if(isNewPathRequest(requestID)){ // update if new request.
            current = layOfTheLand.getLocalInfo(startX,startY);
            current.setNext(null);
            current.setPrev(null);
            current.setID(requestID);
            current.setPathLength(0);
            placeToCheck.add(current);
            goal = layOfTheLand.getLocalInfo(endX,endY);
            pathID = requestID;
        }
        // if the request ID is the same,
        // the means the start and end SHOULD be the same.
        // Meaning we can just pick up where we left off.
        
        while(current.loc().x != endX && current.loc().y != endY){ // Maybe make an expanding range as the algorithm fails? TODO approximate path
            for(Direction dir : cardinal){
                checkLocation(dir, current, goal, requestID, 1);
            }
            for(Direction dir : inter){
                checkLocation(dir, current, goal, requestID, 2);
            }
            current = placeToCheck.poll();
            limiter.tick();
        }
        backFillPath(current);
        return requestID;

    }

    private void backFillPath(MapScribbles last){
        MapScribbles t_node = last;
        while(t_node.getPrev()!=null){
            t_node.getPrev().setNext(t_node);
            t_node=t_node.getPrev();
        }
    }

    private boolean isNewPathRequest(int requestID) {
        return current == null || goal == null || requestID != this.pathID;
    }

    private void checkLocation(Direction dir, MapScribbles current, MapScribbles goal, int requestID, int distanceSquaredFromPrev) {
        MapLocation nextLoc = current.loc().add(dir);
        MapScribbles neighbor = layOfTheLand.getLocalInfo(nextLoc);
        if(shouldSkipNode(neighbor))
            return;
        int updatedPathLength = current.pathLength 
                + distanceSquaredFromPrev 
                + A_StarHeuristic(current, goal);
        if(isItChecked(neighbor, requestID)){ // update if shorter
            if (updatedPathLength < neighbor.pathLength){
                neighbor.pathLength = updatedPathLength;
                neighbor.prev = current;
                placeToCheck.remove(neighbor); // We know this has already been checked because of the request ID.
                placeToCheck.add(neighbor); // Refresh the heap to push correct value up.
            }
        } else { // update if not checked
            neighbor.pathLength = updatedPathLength;
            neighbor.prev = current;
            neighbor.setID(requestID);
            placeToCheck.add(neighbor); //Add to heap 
        }
    }

    private int obstacleHeuristic ( MapScribbles location) {
        int howShittyIsThis = 0;
        // social anxiety
        if(location.isOccupied) {
            if(location.occupantTeam.opponent()==layOfTheLand.myTeam) {
                howShittyIsThis += TIME_IT_TAKES_TO_MURDER ; // the most polite way to ask someone to move ... is murder. No person was ever left offended.
            } else if (location.occupantTeam == layOfTheLand.myTeam){
                howShittyIsThis += 10; // They might move...probably not tho
            }
        }
        // nobody likes getting wet unexpectedly
        if(location.info.isWater())
            howShittyIsThis += WATER_HAZARD;

        // I am moderately okay with getting exploded
        if (location.info.getTrapType()!= TrapType.NONE){
            switch(location.info.getTrapType()){
                case EXPLOSIVE:
                    howShittyIsThis += shouldIgnorePain ?
                            0 : // IGNORE THE PAIN! TAKE IT PUSSY!
                            (EXPLODE_DAMAGE * HEALING_COOLDOWN_TURNS / HEALTH_PER_HEAL);// How long to recover ?
                    break;
                case STUN:
                    howShittyIsThis += 4 ;// Doc says action cooldowns are set to 40...which is ~ 4 turns.
                    break;
                case WATER:
                    howShittyIsThis += TrapType.WATER.enterRadius * WATER_HAZARD; // the number of waters this is, effectively.
                    break;
            }
        }
        return howShittyIsThis;
    }
    private boolean shouldSkipNode( MapScribbles location){
       return location.isOccupied || location.info.isDam() || location.info.isWall();
    }

    private static int A_StarHeuristic(MapScribbles current, MapScribbles goal) {
        return current.loc().distanceSquaredTo(goal.loc());
    }

    private static boolean isItChecked(MapScribbles neighbor, int requestID) {
        return neighbor.reqID() == requestID;
    }

    Direction[] cardinal = {Direction.NORTH,Direction.SOUTH,Direction.EAST,Direction.WEST};
    Direction[] inter = {Direction.NORTHEAST,Direction.NORTHWEST,Direction.SOUTHEAST,Direction.SOUTHWEST};




}
