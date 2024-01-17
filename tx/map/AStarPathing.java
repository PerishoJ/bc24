package tx.map;

import battlecode.common.*;
import tx.Cowboy;
import tx.thinkin.BigPicture;

public class AStarPathing extends Dijkstra<MapLocation>{
    public static final int WATER_HAZARD = GameConstants.FILL_COOLDOWN / 10;
    public static final int TURN_DAMAGE = 150;
    public static final int EXPLOSIVE_COST = TrapType.EXPLOSIVE.enterDamage / TURN_DAMAGE;
    private final BigPicture layOfTheLand;
    private final Cowboy yoursTruly;
    public AStarPathing(BigPicture layOfTheLand, Cowboy yoursTruly) {
        this.layOfTheLand = layOfTheLand;
        this.yoursTruly=yoursTruly;
    }

    @Override
    protected boolean isGoal(PathNode<MapLocation> node) {
        return false;
    }


    @Override
    protected int scoreNode(PathNode<MapLocation> parent , MapLocation node) {
        MapInfo target = layOfTheLand.map[node.x][node.y];
        if(!(target instanceof UnknownMapInfo)){
            if(target.isWall() || target.isDam()){
                return Integer.MAX_VALUE;
            }
            int dist = parent.base.distanceSquaredTo(node);
            if(target.isWater()) {
                return dist + calcWaterHazard();
            }if (target.getTrapType()!= TrapType.NONE){
                switch(target.getTrapType()){
                    case STUN:
                        return dist + 5;
                    case EXPLOSIVE:
                        return dist + calcExplosiveHazard( parent , parent.score);

                }
            }
            //TODO need a cheaper way of testing where units are. Keep MapLocations in a set in BigPicture
        }
        return parent.base.distanceSquaredTo(node);
    }


    protected int calcWaterHazard(){
        return WATER_HAZARD;
    }
    protected int calcExplosiveHazard(PathNode<MapLocation> currentLoc , int distanceTraveled){
        if(distanceTraveled>5) {
            return EXPLOSIVE_COST;
        } else { // This bomb is close ... so don't fuck it up. Think before you step in this man.
            //TODO CACHE THIS FUCKING EXPENSIVE ASS calculation...and maybe add a message to the queue for others
            int dmg = 1; // to you
            for(RobotInfo friends : layOfTheLand.compadres){
                if(friends.getLocation().distanceSquaredTo(currentLoc.base)<TrapType.EXPLOSIVE.enterRadius) dmg++;
            }
            return dmg * EXPLOSIVE_COST;
        }
    }



}
