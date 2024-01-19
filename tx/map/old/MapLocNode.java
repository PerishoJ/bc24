package tx.map.old;

import battlecode.common.MapLocation;

@Deprecated
public class MapLocNode extends PathNode<MapLocation>{
    /**
     * This is for initing nodes that haven't been scored yet.
     *
     * @param parent
     * @param base
     */
    public MapLocNode(PathNode<MapLocation> parent, MapLocation base) {
        super(parent, base);
    }

    public int getTravelCost(){
        return score;
    }

    public void setTravelCost(int travelCost){
        score = travelCost;
    }


}
