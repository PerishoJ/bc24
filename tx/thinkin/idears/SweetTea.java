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
        if ( areThereCompadresAround(bigPicture)) {
            Optional<RobotInfo> friendInNeed = Arrays.stream(bigPicture.compadres)
                    .filter(f -> f.getHealth() < GameConstants.DEFAULT_HEALTH)
                    .filter(f -> rc.canHeal(f.location))
                    .findAny();

            if (friendInNeed.isPresent()){
                bigPicture.friendInNeed = friendInNeed.get();
                return 15;
            }
        }
        return 0;
    }

    private static boolean areThereCompadresAround(BigPicture bigPicture) {
        return bigPicture.compadres.length > 0;
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
