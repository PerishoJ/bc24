package tx.thinkin.idears;

import tx.Cowboy;
import tx.map.BugPathing;
import tx.map.PathFinding;
import tx.thinkin.BigPicture;
import battlecode.common.*;

import java.util.Arrays;
import java.util.Optional;

public class SweetTea implements BrightIdea{

    PathFinding path;
    @Override
    public int howAboutThat(BigPicture bigPicture, RobotController rc) {
        if(path==null)path=new BugPathing(bigPicture,rc);
        bigPicture.friendInNeed = null;
        // heal people if nobody can shoot you, because it's just better to shoot back.
        if ( !bigPicture.compadres.isEmpty()  // friends
                && bigPicture.closestEnemyDist > GameConstants.ATTACK_RADIUS_SQUARED) { // but no bad guys in range
            RobotInfo friend = null;
            for(RobotInfo f : bigPicture.compadres){
                if ( f.getHealth() < GameConstants.DEFAULT_HEALTH){
                    friend = f;
                    break;
                }
            }
            if (friend!=null){
                bigPicture.friendInNeed = friend;
                return 5;
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
            yoursTruly.me.setIndicatorLine(yoursTruly.me.getLocation(), friendInNeed , 0, 255, 100);

        } else {
            try {
                path.go(friendInNeed);
                yoursTruly.me.setIndicatorLine(yoursTruly.me.getLocation(), friendInNeed , 0, 0 , 100);
            } catch (Exception e) {
                System.err.println("Error pathing to ally:" + e);
            }
        }
    }

    @Override
    public String getName() {
        return "Sweet Tea";
    }
}
