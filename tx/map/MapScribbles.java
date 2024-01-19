package tx.map;

import battlecode.common.MapInfo;
import battlecode.common.MapLocation;

public class MapScribbles {
    boolean isOccupied = false;
    /**Every Pathing operation marks up the map. If we keep an ID, we don't have to erase the whole map every iteration*/
    int requestID = -1;
    MapInfo info;

    public MapScribbles(MapInfo info) {
        this.info = info;
    }

    public MapLocation getMapLocation(){
        return info.getMapLocation();
    }

    public MapInfo getInfo(){
        return info;
    }

    public void setInfo(MapInfo mapInfo){
        info = mapInfo;
    }

    public boolean isPassable(){
        return info.isPassable();
    }
}
