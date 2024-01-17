package tx.thinkin.idears;

import battlecode.common.MapLocation;
import tx.Cowboy;
import tx.RobotPlayer;
import tx.map.BugPathing;
import tx.map.PathFinding;
import tx.thinkin.BigPicture;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;

import java.util.Map;

import static tx.RobotPlayer.directions;
import static tx.RobotPlayer.rng;

public class MeanderAround implements BrightIdea {

    public static final int MOVE_TRIES = 16;
    public static final int THRESHOLD_DISTANCE = 5;

    private PathFinding bug;

    MapLocation rndPt;
    int boredValue = 0;
    @Override
    public int howAboutThat(BigPicture bigPicture, RobotController rc) {
        if(bug == null) bug = new BugPathing(bigPicture,rc);
        resetTargetIfCloseOrBored(bigPicture, rc);
        // run away from badguys
        if( !bigPicture.muchachos.isEmpty() )
            return 0;
        else
            return 1;
    }

    private void resetTargetIfCloseOrBored(BigPicture bigPicture, RobotController rc) {
        if(rndPt == null || rndPt.distanceSquaredTo(rc.getLocation())< THRESHOLD_DISTANCE || boredValue<1) {
            MapLocation[] flags = rc.senseBroadcastFlagLocations();
            rndPt =  flags[ RobotPlayer.rng.nextInt(flags.length)];
            boredValue = rng.nextInt(MAX_PATIENCE());
        }
    }

    private static int MAX_PATIENCE() {
        return 64;
    }

    @Override
    public void getErDone(Cowboy yoursTruly) throws GameActionException {
        RobotController rc = yoursTruly.me;
        rc.setIndicatorString("Just meanderin' 'round");
        rc.setIndicatorDot(rndPt, 10,59,200);
        rc.setIndicatorLine(rc.getLocation(),rndPt,10,100,200);
        try {
           Direction dir = bug.go(rndPt);
           if(rc.canMove(dir)){
               yoursTruly.move(dir);
           } else {
               throw new Exception("");// just get caught and try to go somewhere.
           }
        } catch (Exception e) {
            for(int i = 0; i< MOVE_TRIES; i++){
                Direction dir = directions[rng.nextInt(directions.length)];
                if (rc.canMove(dir)){
                    yoursTruly.move(dir);
                    break;
                }
            }
        } finally {
            boredValue--;
        }

    }

    @Override
    public String getName() {
        return "Meanderin'";
    }
}
