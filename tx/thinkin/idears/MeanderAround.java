package tx.thinkin.idears;

import tx.Cowboy;
import tx.thinkin.BigPicture;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;

import static tx.RobotPlayer.directions;
import static tx.RobotPlayer.rng;

public class MeanderAround implements BrightIdea {

    public static final int MOVE_TRIES = 16;

    @Override
    public int howAboutThat(BigPicture bigPicture, RobotController rc) {
        if( !bigPicture.muchachos.isEmpty() )
            return -1;
        else
            return 1;
    }

    @Override
    public void getErDone(Cowboy yoursTruly) throws GameActionException {
        RobotController rc = yoursTruly.me;
        rc.setIndicatorString("Just meanderin' 'round");
        for(int i = 0; i< MOVE_TRIES; i++){
            Direction dir = directions[rng.nextInt(directions.length)];
            if (rc.canMove(dir)){
                yoursTruly.move(dir);
                break;
            }
        }
    }
}
