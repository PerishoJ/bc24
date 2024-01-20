package tx.map;

import battlecode.common.*;
import tx.thinkin.BigPicture;
import tx.util.ByteCodeLimiter;
import tx.util.ByteCodeLimiterIF;
import tx.util.ByteCodeMonitorIF;
import tx.util.OutOfTimeException;

import java.util.HashSet;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.Set;

public class TrailBlazer {
    public static final int MAP_AREA_IS_THE_HEAP_INIT_CAPACITY = 60 * 60;
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

    ByteCodeMonitorIF monitor;

    long cycleNumber = 0; // Used for tracking the

    PriorityQueue<MapScribbles> placeToCheck
            = new PriorityQueue<MapScribbles>(MAP_AREA_IS_THE_HEAP_INIT_CAPACITY, (a, b)->(a.getPathLength()+a.distanceToTarget)
            -(b.getPathLength()+b.distanceToTarget)) ; //Reverse order, adding A* heuristic to each distance.

    Set<MapScribbles> alreadyChecked = new HashSet<>((int)(MAP_AREA_IS_THE_HEAP_INIT_CAPACITY * 1.3));
    public TrailBlazer(BigPicture layOfTheLand) {
        this.layOfTheLand = layOfTheLand;
        limiter = new ByteCodeLimiter();
        monitor = (ByteCodeMonitorIF) limiter;
    }
    public int blazeATrail(MapLocation start, MapLocation destination) throws OutOfTimeException {
        return blazeATrail(start.x,start.y,destination.x,destination.y, null);
    }

    public int blazeATrail(MapLocation start, MapLocation destination, Integer salt) throws OutOfTimeException {
        return blazeATrail(start.x,start.y,destination.x,destination.y, salt);
    }

    public int blazeATrail(int startX, int startY, int endX, int endY ) throws OutOfTimeException {
        return blazeATrail( startX,  startY, endX, endY , null);
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
    public int blazeATrail(int startX, int startY, int endX, int endY , Integer salt) throws OutOfTimeException {
        limiter.resetClock();
        int requestID = salt==null?
                Objects.hash(startX,startY,endX,endY):
                Objects.hash(startX,startY,endX,endY,salt);
        if(isNewPathRequest(requestID)){ // update if new request.
            current = layOfTheLand.getLocalInfo(startX,startY);
            current.setNext(null);
            current.setPrev(null);
            current.setID(requestID);
            current.setPathLength(0);
            placeToCheck.clear();
            alreadyChecked.clear();
            cycleNumber = 0;
            placeToCheck.add(current);
            goal = layOfTheLand.getLocalInfo(endX,endY);
            pathID = requestID;
        }
        // if the request ID is the same,
        // the means the start and end SHOULD be the same.
        // Meaning we can just pick up where we left off.

        while(isNotDeadEnd() && isNotDestination(endX,endY)){
            for(Direction dir : cardinal){
                checkLocation(dir, current, goal, requestID, 1);
            }
            for(Direction dir : inter){
                checkLocation(dir, current, goal, requestID, 2);
            }
            alreadyChecked.add(current);
            int start = Clock.getBytecodeNum();
            current = placeToCheck.poll();
            System.out.println("Poll ("+placeToCheck.size()+" items) clocks:"+(Clock.getBytecodeNum()-start));
            limiter.tick();
        }
        backFillPath(current);
        return requestID;

    }
    private boolean isNotDestination(int endX, int endY) {
        return current.loc().x != endX && current.loc().y != endY;
    }

    private boolean isNotDeadEnd() {
        return current != null;
    }
    private void backFillPath(MapScribbles last){
        MapScribbles t_node = last;
        while(t_node!=null && t_node.getPrev()!=null){
            t_node.getPrev().setNext(t_node);
            t_node=t_node.getPrev();
        }
    }

    private boolean isNewPathRequest(int requestID) {
        return current == null || goal == null || requestID != this.pathID;
    }

    private void checkLocation(Direction dir, MapScribbles current, MapScribbles goal, int requestID, int distanceSquaredFromPrev) {
        MapLocation nextLoc = current.loc().add(dir);
        if(alreadyChecked.contains(nextLoc)) {
            return;
        }
        if(layOfTheLand.isOffMap(nextLoc)) {
            return;
        }


        MapScribbles neighbor = null;
        neighbor = layOfTheLand.getLocalInfo(nextLoc);

        if(shouldSkipNode(neighbor))
            return;
        
        int updatedPathLength = current.pathLength
                + distanceSquaredFromPrev
                + obstacleHeuristic(neighbor) ;

        if(isItChecked(neighbor, requestID)){ // update if shorter
            if (updatedPathLength < neighbor.pathLength){
                neighbor.pathLength = updatedPathLength;
                neighbor.prev = current;
                updateChecked(neighbor);
            }
        } else { // update if not checked
            neighbor.pathLength = updatedPathLength;
            neighbor.prev = current;
            neighbor.setID(requestID);
            neighbor.distanceToTarget = neighbor.loc().distanceSquaredTo(goal.loc());
            addChecked(neighbor);
        }
    }

    private void addChecked(MapScribbles neighbor){
        int start = Clock.getBytecodeNum();
        placeToCheck.add(neighbor);
        System.out.println("Add ("+placeToCheck.size()+" total items) clocks:"+(Clock.getBytecodeNum()-start));
    }

    private void updateChecked(MapScribbles neighbor){
        int start = Clock.getBytecodeNum();
        placeToCheck.remove(neighbor); // We know this has already been checked because of the request ID.
        placeToCheck.add(neighbor); // Refresh the heap to push correct value up.
        System.out.println("update ("+placeToCheck.size()+" total items) clocks:"+(Clock.getBytecodeNum()-start));
    }


    private int obstacleHeuristic ( MapScribbles location) {
        if (location.obstacle_value == MapScribbles.UNCALCULATED_OBSTACLE_CONSTANT)
        {
            location.obstacle_value = calculateObstacleHeuristic(location);
        }
        return location.obstacle_value;
    }

    private int calculateObstacleHeuristic(MapScribbles location) {
        int howShittyIsThis = 0;
        // social anxiety
        if (location.isOccupied) {
            if (location.occupantTeam.opponent() == layOfTheLand.myTeam) {
                howShittyIsThis += TIME_IT_TAKES_TO_MURDER; // the most polite way to ask someone to move ... is murder. No person was ever left offended.
            } else if (location.occupantTeam == layOfTheLand.myTeam) {
                howShittyIsThis += 10; // They might move...probably not tho
            }
        }
        // nobody likes getting wet unexpectedly
        if (location.info.isWater())
            howShittyIsThis += WATER_HAZARD;

        // I am moderately okay with getting exploded
        if (location.info.getTrapType() != TrapType.NONE) {
            switch (location.info.getTrapType()) {
                case EXPLOSIVE:
                    howShittyIsThis += shouldIgnorePain ?
                            0 : // IGNORE THE PAIN! TAKE IT PUSSY!
                            (EXPLODE_DAMAGE * HEALING_COOLDOWN_TURNS / HEALTH_PER_HEAL);// How long to recover ?
                    break;
                case STUN:
                    howShittyIsThis += 4;// Doc says action cooldowns are set to 40...which is ~ 4 turns.
                    break;
                case WATER:
                    howShittyIsThis += TrapType.WATER.enterRadius * WATER_HAZARD; // the number of waters this is, effectively.
                    break;
            }
        }
        if(howShittyIsThis<0) howShittyIsThis=0; // clamp at zero.
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

    public ByteCodeMonitorIF getMonitor() {
        return monitor;
    }

    public void setMonitor(ByteCodeMonitorIF monitor) {
        this.monitor = monitor;
    }



}
