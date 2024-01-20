package tx.thinkin.idears;

import battlecode.common.Direction;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import tx.Cowboy;
import tx.map.MapScribbles;
import tx.map.TrailBlazer;
import tx.thinkin.BigPicture;
import tx.util.ByteCodeLimiter;
import tx.util.ByteCodeLimiterIF;
import tx.util.OutOfTimeException;

import java.util.Map;

public class BlazinSomeTrails implements BrightIdea{

    public static final int BYTE_CODE_LIMIT = 12000;
    TrailBlazer trailBlazer ;

    ByteCodeLimiterIF limiter ;
    MapLocation goal = new MapLocation( 24,25 );

    @Override
    public int howAboutThat(BigPicture bigPicture, RobotController rc) {
        if(trailBlazer==null) {
            trailBlazer = new TrailBlazer(bigPicture);
            limiter = trailBlazer.getLimiter();
            limiter.setByteCodeLimit(BYTE_CODE_LIMIT); // about half a turn
        }
        return 100;
    }

    boolean isFound = false;
    int salt = 1;
    @Override
    public void getErDone(Cowboy me) throws Exception {
        try {
            int requestId = trailBlazer.blazeATrail(me.me.getLocation(),goal,salt);
            isFound = true;
        } catch (OutOfTimeException e) {
            System.err.println("Ran out of time. " + limiter.getTicks() + " ticks took " + BYTE_CODE_LIMIT + " total bytecodes");
        }
        if(isFound){
            try {
                MapLocation next = getNextPathLoc(me); // a null value means that you're at the end of the line
                lightUpTrail(next,me);
                if(isNotEndOfPath(next)) {
                    Direction dir = me.me.getLocation().directionTo(next);
                    if (me.me.canMove(dir)) {
                        me.me.move(dir);
                    } else {
                        if(me.me.getMovementCooldownTurns()<10) {
                            isFound = false;
                            salt++;
                        }
                    }
                } else {
                    //TODO check end
                    isFound = false;
                }
            } catch (Exception e) {
                System.err.println(e.getMessage());
                e.printStackTrace();
            } finally {
                System.out.println("========================================================\n");
                for(Map.Entry<String, Integer> clock : trailBlazer.getMonitor().getClocks()){
                    System.out.println(clock.getKey() + " : " + clock.getValue() + "\n");
                }
                System.out.println("========================================================\n");
                System.out.println("========================================================\n");
                for(Map.Entry<String, Integer> metric : trailBlazer.getMonitor().getMetrics()){
                    System.out.println(metric.getKey() + " : " + metric.getValue() + "\n");
                }
                System.out.println("========================================================\n");
            }
        }
    }

    private void lightUpTrail(MapLocation s, Cowboy I){
        if(s==null) return;
        MapScribbles start = I.layOfTheLand.getLocalInfo(s);
        while(start!=null){
            I.me.setIndicatorDot(start.loc(),0,200,200);
            start = start.getNext();
        }
    }

    private static boolean isNotEndOfPath(MapLocation next) {
        return next != null;
    }


    private static MapLocation getNextPathLoc(Cowboy me) throws Exception {
        MapScribbles next = me.layOfTheLand.getLocalInfo(me.me.getLocation()).getNext();
        if(next!=null)
            return next.loc();
        else
            return null;// You're at the end, or you hit a deadend
//        else
//            throw new Exception(" The path is yielding a null NEXT location, meaning it was never laid properly.");
    }

    @Override
    public String getName() {
        return "Trail Blazer";
    }
}
