package tx.thinkin;

import battlecode.common.MapLocation;

/**
 * One hectare o' property.
 */
public class Plot {
    MapLocation loc;
    public enum LandType{
        WATER,CLIFFS,PLAIN,UNKNOWN
    }
}
