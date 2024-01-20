package tx.util;


import battlecode.common.Clock;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * tracks bytecodes and throws exception when they are exceeded.
 */
public class ByteCodeLimiter implements ByteCodeLimiterIF,ByteCodeMonitorIF {


    int ticks = 0;
    /**
     * Occurs during each loop. Tracks time or resources during each loop.
     * Should be called during each loop of expensive operations.
     * @throws OutOfTimeException - thrown if out of time or resources to calculate.
     */

    @Override
    public void tick() throws OutOfTimeException {
        ticks++;
        if( Clock.getBytecodeNum() >= bytecodeTrigger){
            throw new OutOfTimeException();
        }
    }
    /**
     * Resets the time or resource count tracking cost.
     * <br/>
     *  - called at beginning of any calc intensive functions.
     */
    @Override
    public void resetClock() {
        ticks = 0;
        if (bytecodeLimit == Integer.MAX_VALUE) {
            bytecodeTrigger = Integer.MAX_VALUE;
        } else {
            bytecodeTrigger = Clock.getBytecodeNum() + bytecodeLimit; // When we need to cut-off the calculation slurpathon
        }
    }
    int bytecodeLimit = Integer.MAX_VALUE;
    /**# of bytecodes that trigger exception*/
    int bytecodeTrigger;
    @Override
    public void setByteCodeLimit(int limit){
        bytecodeLimit = limit;
    }

    @Override
    public int getTicks(){
        return ticks;
    }

    Map<String,Integer> mtrc = new HashMap<>(500);
    Map<String,Integer> clk = new HashMap<>(500);
    @Override
    public void startClock(String clock) {
        clk.put(clock,Clock.getBytecodeNum());
    }

    @Override
    public void stopClock(String clock) {
        int delta = Clock.getBytecodeNum() - clk.get(clock);
        clk.put(clock,delta);
    }

    @Override
    public int getClockDelta(String clock) {
        if(clk.get(clock)==null)
            return 0;
        else
            return clk.get(clock);
    }

    @Override
    public Set< Map.Entry<String, Integer> > getClocks() {
        return clk.entrySet();
    }


    @Override
    public void putMetric(String metric, Integer value) {
        mtrc.put(metric,value);
    }

    @Override
    public int getMetric(String metric) {
        return mtrc.get(metric);
    }

    @Override
    public Set<Map.Entry<String, Integer>> getMetrics() {
        return mtrc.entrySet();
    }


}
