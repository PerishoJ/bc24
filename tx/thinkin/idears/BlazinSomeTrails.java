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
    MapLocation goal = new MapLocation( 25,25 );

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
    @Override
    public void getErDone(Cowboy me) throws Exception {
        try {
            int requestId = trailBlazer.blazeATrail(me.me.getLocation(),goal);
            isFound = true;
        } catch (OutOfTimeException e) {
            System.err.println("Ran out of time. " + limiter.getTicks() + " ticks took " + BYTE_CODE_LIMIT + " total bytecodes");
        }
        if(isFound){
            try {
                Direction dir = findPathDirection(me);
                if(me.me.canMove(dir)){
                    me.me.move(dir);
                }
            } catch (Exception e) {
                System.err.println(e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private static Direction findPathDirection(Cowboy me) throws Exception {
        return me.me.getLocation().directionTo(getNextPathLoc(me));
    }

    private static MapLocation getNextPathLoc(Cowboy me) throws Exception {
        MapScribbles next = me.layOfTheLand.getLocalInfo(me.me.getLocation()).getNext();
        if(next!=null)
            return next.loc();
        else
            throw new Exception(" The path is yielding a null NEXT location, meaning it was never laid properly.");
    }

    @Override
    public String getName() {
        return "Trail Blazer";
    }
}
