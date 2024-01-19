package tx.map;

import battlecode.common.Direction;
import battlecode.common.MapLocation;

public interface PathFinding {
    void go(MapLocation loc) throws Exception;
}
