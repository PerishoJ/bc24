package tx.util;


import battlecode.common.Clock;

/**
 * tracks bytecodes and throws exception when they are exceeded.
 */
public class ByteCodeLimiter {

    /**
     * Occurs during each loop. Tracks time or resources during each loop.
     * Should be called during each loop of expensive operations.
     * @throws OutOfTimeException - thrown if out of time or resources to calculate.
     */

    public void tick() throws OutOfTimeException {
        if( Clock.getBytecodeNum() >= bytecodeTrigger){
            throw new OutOfTimeException();
        }
    }
    /**
     * Resets the time or resource count tracking cost.
     * <br/>
     *  - called at beginning of any calc intensive functions.
     */
    public void resetClock() {
        if (bytecodeLimit == Integer.MAX_VALUE) {
            bytecodeTrigger = Integer.MAX_VALUE;
        } else {
            bytecodeTrigger = Clock.getBytecodeNum() + bytecodeLimit; // When we need to cut-off the calculation slurpathon
        }
    }
    int bytecodeLimit = Integer.MAX_VALUE;
    /**# of bytecodes that trigger exception*/
    int bytecodeTrigger;
    public void setByteCodeLimit(int limit){
        bytecodeLimit = limit;
    }


}
