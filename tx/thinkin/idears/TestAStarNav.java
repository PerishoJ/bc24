package tx.thinkin.idears;

import battlecode.common.*;
import tx.Cowboy;
import tx.map.old.AStarPathing;
import tx.map.old.PathNode;
import tx.thinkin.BigPicture;
import tx.util.ByteCodeLimiter;

@Deprecated
public class TestAStarNav implements BrightIdea{

    AStarPathing pathing;
    MapLocation sampleTargetLocation;
    PathNode<MapLocation> path;

    int retry = 25;
    int turn = 0;
    ByteCodeLimiter limiter = new ByteCodeLimiter();
    @Override
    public int howAboutThat(BigPicture bigPicture, RobotController rc) {
        turn++;
        if(pathing == null || turn > retry) {
            pathing = new AStarPathing(bigPicture);
            pathing.setByteCodeLimiter(limiter);
            pathing.setByteCodeLimit(10000);
            turn = 0;
            System.out.println("Retrying pathing ");
        }
        if (sampleTargetLocation == null) {
            sampleTargetLocation = new MapLocation( 10 , 16);
        }

        return 100;
    }

    @Override
    public void getErDone(Cowboy me) throws GameActionException {
        try {
            RobotController I = me.me;
            if(path==null) {
               path = pathing.findPath(I.getLocation(), sampleTargetLocation);
            }

            if(path.getBase().equals(I.getLocation())){
                Direction targetDir = I.getLocation().directionTo( path.getBase() );
                if(I.canMove(targetDir) ){
                    I.move(targetDir);
                    path = path.next; // if path == null, we're done...hypothetically
                }

            } else {
                System.out.println("Path "+ path.getBase() + " base is wrong. Current loc = " + me.me.getLocation() );
            }
        } catch (Exception e) {
            System.out.println("Timed out at " + pathing.getPartial().getBase() + "...Will continue next turn.\n" +
                    " Executed "+limiter.getTicks() + " ticks , averaging " + 10000/ limiter.getTicks() + "bytes per tick.\n");
        }
    }

    @Override
    public String getName() {
        return "Test Bug Nav";
    }
}
