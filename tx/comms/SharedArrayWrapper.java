package tx.comms;

import battlecode.common.GameActionException;

public interface SharedArrayWrapper {
    boolean canWriteSharedArray(int index, int value);
    void writeSharedArray(int index, int value) throws GameActionException;
    int readSharedArray(int index) throws GameActionException;
}
