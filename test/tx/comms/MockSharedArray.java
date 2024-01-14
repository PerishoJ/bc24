package tx.comms;

import battlecode.common.GameActionException;
import battlecode.common.GameActionExceptionType;
import battlecode.common.GameConstants;
import tx.comms.SharedArrayWrapper;

import java.util.Random;

public class MockSharedArray implements SharedArrayWrapper {
    public static final int SIZE = GameConstants.SHARED_ARRAY_LENGTH;
    public static final int BIT_MASK_16_BITS = GameConstants.MAX_SHARED_ARRAY_VALUE; // we use this in case teh devs change it...because why not?? Right?
    int[] sharedArray = new int[SIZE];
    private static final Random rnd = new Random(System.currentTimeMillis());// want it unique every time.
    
    public MockSharedArray(){
        fillArrayWithTrash(); //assume the worst array init
    }

    private void fillArrayWithTrash() {
        for(int i = 0 ; i< SIZE ; i++){
            sharedArray[i] = rnd.nextInt();
        }
    }

    @Override
    public boolean canWriteSharedArray(int index, int value) {
        return isInBounds(index);
    }

    private static boolean isInBounds(int index) {
        return index < SIZE && index >= 0;
    }

    @Override
    public void writeSharedArray(int index, int value) throws GameActionException {
        if(canWriteSharedArray(index,value)){
            value = LimitBitsUsed(value); //only the first 16 bits are used.
            sharedArray[index] = value;
        } else {
            throw new GameActionException(GameActionExceptionType.OUT_OF_RANGE, "you can't write to " + index);
        }
    }

    private static int LimitBitsUsed(int value) {
        return value & BIT_MASK_16_BITS;
    }

    @Override
    public int readSharedArray(int index) throws GameActionException {
        if(canWriteSharedArray(index,0)){ //it's the same check...we just reuse it because lazy
            return LimitBitsUsed(sharedArray[index]); //only the first 16 bits are read.;
        } else {
            throw new GameActionException(GameActionExceptionType.OUT_OF_RANGE, "you can't write to " + index);
        }
    }
}
