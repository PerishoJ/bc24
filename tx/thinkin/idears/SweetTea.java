package tx.thinkin.idears;

import tx.Cowboy;
import tx.thinkin.BigPicture;
import battlecode.common.*;

import java.util.Arrays;
import java.util.Optional;

public class SweetTea implements BrightIdea{


    @Override
    public int howAboutThat(BigPicture bigPicture, RobotController rc) {
        bigPicture.friendInNeed = null;
        // heal people if nobody can shoot you, because it's just better to shoot back.
        if ( !bigPicture.compadres.isEmpty()  // friends
                && bigPicture.muchachos.isEmpty()) { // but no bad guys
            RobotInfo friend = null;
            for(RobotInfo f : bigPicture.compadres){
                if ( f.getHealth() < GameConstants.DEFAULT_HEALTH
                && rc.canHeal(f.location)){
                    friend = f;
                    break;
                }
            }

            if (friend!=null){
                bigPicture.friendInNeed = friend;
                return 15;
            }
        }
        return 0;
    }


    @Override
    public void getErDone(Cowboy yoursTruly) throws GameActionException {
        RobotController I = yoursTruly.me;
        MapLocation friendInNeed = yoursTruly.layOfTheLand.friendInNeed.getLocation();
        if(I.canHeal(friendInNeed)){
            I.heal(friendInNeed);
        }
    }
}
