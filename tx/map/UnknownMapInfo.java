package tx.map;

import battlecode.common.MapInfo;
import battlecode.common.MapLocation;
import battlecode.common.Team;
import battlecode.common.TrapType;

/**
 * Reserved only for showing unknown values on the local map while also making it usable for navigation and etc.
 */
public class UnknownMapInfo extends MapInfo {
    public UnknownMapInfo(MapLocation loc, boolean isPassable, boolean isWall, boolean isDam, int spawnZone, boolean isWater, int crumbsAmount, TrapType trapType, Team territory) {
        super(loc, isPassable, isWall, isDam , spawnZone, isWater, crumbsAmount, trapType , territory);
    }

    public UnknownMapInfo(int x , int y){
        this(new MapLocation(x,y));
    }

    public UnknownMapInfo(MapLocation loc){
       super(loc,true,false,false,0,false,0,TrapType.NONE, Team.NEUTRAL);
    }
}
