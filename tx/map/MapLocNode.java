package tx.map;

import battlecode.common.Direction;
import battlecode.common.MapLocation;
import tx.RobotPlayer;
import tx.thinkin.BigPicture;

import java.util.ArrayList;
import java.util.List;

public class MapLocNode extends PathNode<MapLocation>{
    /**
     * This is for initing nodes that haven't been scored yet.
     *
     * @param parent
     * @param base
     */

    BigPicture layOfTheLand;

    public MapLocNode(PathNode<MapLocation> parent, MapLocation base, BigPicture layOfTheLand) {
        super(parent, base);
        this.layOfTheLand =layOfTheLand;
    }

    public int getTravelCost(){
        return score;
    }

    public void setTravelCost(int travelCost){
        score = travelCost;
    }

    @Override
    public List<MapLocation> getNeighbors() {
        List<MapLocation> neighbors = new ArrayList<>(8);
        for (Direction dir : RobotPlayer.directions){
            MapLocation neighbor = base.add(dir);
            if(! layOfTheLand.isOffMap(neighbor)){
                neighbors.add(neighbor);
            }
        }
        return neighbors;
    }

}
