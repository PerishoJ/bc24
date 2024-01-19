package tx.thinkin.idears;

import battlecode.common.GameConstants;
import battlecode.common.RobotController;
import tx.Cowboy;
import tx.map.BugPathing;
import tx.map.PathFinding;
import tx.thinkin.BigPicture;

public class FlagThief implements BrightIdea {
    public static final int GRABBING_DISTANCE = 2;
    PathFinding bug;
    @Override
    public int howAboutThat(BigPicture bigPicture, RobotController rc) {
        if(bug==null) bug = new BugPathing(bigPicture,rc);
        // IS a flag close
        if(bigPicture.closestEnemyFlagDist < GameConstants.ATTACK_RADIUS_SQUARED){
            return 7;
        } else if (bigPicture.closestEnemyFlagDist < GameConstants.VISION_RADIUS_SQUARED){
            return 5;
        }

        // did you already steal a flag
        if(rc.hasFlag()){
            return 30;
        }

        return 0;
    }

    @Override
    public void getErDone(Cowboy me) throws Exception {
        //If you have the flag... GO HOME
        if(me.me.hasFlag()){
             bug.go(me.layOfTheLand.closestSpawn);
        }
        // or else try to steal it.
        if(canIGetFlag(me)){
            System.out.println("Trying to pickup flag");
            me.me.pickupFlag(me.layOfTheLand.closestEnemyFlag);
        } else {
            System.out.println("Trying to get to the flag.");
            bug.go(me.layOfTheLand.closestEnemyFlag);
        }
    }

    @Override
    public String getName() {
        return "flag thief";
    }

    private static boolean canIGetFlag(Cowboy me) {
        return me.layOfTheLand.closestEnemyFlag != null
                && me.me.canPickupFlag(me.layOfTheLand.closestEnemyFlag);
    }
}
