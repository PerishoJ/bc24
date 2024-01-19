package tx.map.old;

import battlecode.common.*;
import tx.RobotPlayer;
import tx.map.AintSeenIt;
import tx.thinkin.BigPicture;

import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;

@Deprecated
public class aStarPathing extends Dijkstra<MapLocation>{
    public static final int WATER_HAZARD = GameConstants.FILL_COOLDOWN / 10;
    public static final int TURN_DAMAGE = 150;
    public static final int EXPLOSIVE_COST = TrapType.EXPLOSIVE.enterDamage / TURN_DAMAGE;
    private final BigPicture layOfTheLand;
    public aStarPathing(BigPicture layOfTheLand) {
        this.layOfTheLand = layOfTheLand;
        nodesToCheck = new PriorityQueue<>((a,b)->a.score-b.score); // We want the lowest score to be first on the queue
    }

    @Override
    protected boolean isGoal(PathNode<MapLocation> node) {
        return node.getBase().equals(goal);
    }


    @Override
    protected int scoreNode(PathNode<MapLocation> node) {
        //This got messy because of refactor TODO cleanup someday...maybe
        MapInfo target = layOfTheLand.getLocalInfo(node.base).getInfo();
        int distFromParent = node.parent.base.distanceSquaredTo(node.base);
        int distFromGoal = node.base.distanceSquaredTo(goal);
        int accumDist = node.parent.score;
        int astarHeuristic =  accumDist + distFromParent + distFromGoal ;
        if(!(target instanceof AintSeenIt)){
            if(target.isWater()) {
                return astarHeuristic + calcWaterHazard();
            }if (target.getTrapType()!= TrapType.NONE){
                switch(target.getTrapType()){
                    case STUN:
                        return astarHeuristic + 5;
                    case EXPLOSIVE:
                        return astarHeuristic + calcExplosiveHazard( node.parent , node.parent.score);

                }
            }
            //TODO need a cheaper way of testing where units are. Keep MapLocations in a set in BigPicture
        }
        return astarHeuristic;
    }

    protected int calcWaterHazard(){
        return WATER_HAZARD;
    } //TODO take # crumbs cost into account and total avail crumbs.
    protected int calcExplosiveHazard(PathNode<MapLocation> currentLoc , int distanceTraveled){
        return EXPLOSIVE_COST;
//        if(distanceTraveled>5) {
//            return EXPLOSIVE_COST;
//        } else { // This bomb is close ... so don't fuck it up. Think before you step in this man.
//            //TODO CACHE THIS FUCKING EXPENSIVE ASS calculation...and maybe add a message to the queue for others
//            int dmg = 1; // to you
//            for(RobotInfo friends : layOfTheLand.compadres){
//                if(friends.getLocation().distanceSquaredTo(currentLoc.base)<TrapType.EXPLOSIVE.enterRadius) dmg++;
//            }
//            return dmg * EXPLOSIVE_COST;
//        }
    }

    @Override
    public List<PathNode<MapLocation>> getNeighbors(PathNode<MapLocation> node) {
        List<PathNode<MapLocation>> neighbors = new LinkedList<>();
        for (Direction dir : RobotPlayer.directions){ // add a neighbor for each direction
            MapLocation neighborLocation = node.base.add(dir);
            // unless you can't, of course.
            if( !(  layOfTheLand.isOffMap(neighborLocation) // on the map
                    || neighborLocation.equals(node.base)// don't go backwards...just don't
                    || layOfTheLand.getLocalInfo(neighborLocation).getInfo().isWall() // don't check walls
                    || layOfTheLand.getLocalInfo(neighborLocation).getInfo().isDam() // don't check the dam
                )
            ){
                neighbors.add(new PathNode<MapLocation>(node,neighborLocation));
            }
        }
        return neighbors;
    }


    public PathNode<MapLocation> getPartial(){return current;}


}
