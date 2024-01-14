package tx.comms;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import tx.RobotPlayer;

/**
 * wrap it so we can isolate and test values
 */
public class RobotSharedArray implements SharedArrayWrapper{
    private RobotController rc;
    public RobotSharedArray(RobotController rc ){
        this.rc = rc;
    }


    @Override
    public boolean canWriteSharedArray(int index, int value) {
        return rc.canWriteSharedArray(index,value);
    }

    @Override
    public void writeSharedArray(int index, int value) throws GameActionException {
        rc.writeSharedArray(index,value);
    }

    @Override
    public int readSharedArray(int index) throws GameActionException {
        return rc.readSharedArray(index);
    }
}
