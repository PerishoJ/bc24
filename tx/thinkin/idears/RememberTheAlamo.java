package tx.thinkin.idears;

import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import tx.Cowboy;
import tx.map.BugPathing;
import tx.thinkin.BigPicture;

public class RememberTheAlamo implements BrightIdea{

    BugPathing bug;
    @Override
    public int howAboutThat(BigPicture LOL, RobotController rc) {
        if(bug == null) bug = new BugPathing(LOL,rc);
        if((LOL.closestSpawnDist < 25 || LOL.closestAllyFlagDist<25) && !LOL.muchachos.isEmpty() ){
            return 25; // LEROY JENKINS!
        } else {
            return 0;
        }
    }

    @Override
    public void getErDone(Cowboy me) throws Exception {
        MapLocation[] muchLocs = new MapLocation[me.layOfTheLand.muchachos.size()];
        for(int i = 0 ; i<me.layOfTheLand.muchachos.size() ; i++){
            muchLocs[i] = me.layOfTheLand.muchachos.get(i).getLocation();
        }
        MapLocation badLoc = Cowboy.findClosest(muchLocs, me.me);
        if(badLoc != null) {
            if (badLoc.distanceSquaredTo(me.me.getLocation()) < GameConstants.ATTACK_RADIUS_SQUARED) {
                if (me.me.canAttack(badLoc)) {
                    me.me.attack(badLoc);
                }
            } else {
              bug.go(badLoc);
            }
        }
    }

    @Override
    public String getName() {
        return "Remember the Alamo";
    }
}
