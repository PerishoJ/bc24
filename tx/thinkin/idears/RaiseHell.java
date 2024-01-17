package tx.thinkin.idears;

import tx.Cowboy;
import tx.thinkin.BigPicture;
import battlecode.common.*;

import java.util.Optional;

import static java.util.Optional.ofNullable;

public strictfp class RaiseHell implements BrightIdea{


    public static final int ATTACK_DAMAGE = 150;

    /**
     * Where you're gonna call the end of a fight.
     */
    public static final int TOLERANCE = 450; // about 3 shots left before your dead.

    /**If you's chicken shit, you run from a figh'*/
    public static final int CHICKEN_SHIT = 10;

    @Override
    public int howAboutThat(BigPicture bigPicture, RobotController rc) {
        int anger = 0;
        if(bigPicture.muchachos.size() > 1){
            anger += 6;
        } else {
            return 0;
        }
        int muchachosInRange = 0;
        for(RobotInfo muchacho : bigPicture.muchachos){
            if(muchacho.getLocation().distanceSquaredTo(rc.getLocation())<=GameConstants.ATTACK_RADIUS_SQUARED){
                muchachosInRange ++;
            }
        }
        if(muchachosInRange == 1){
            anger += 10;
        }

        for(RobotInfo frnd : bigPicture.compadres){
            // friends in attack range.
            if(frnd.getLocation().distanceSquaredTo(rc.getLocation())<GameConstants.ATTACK_RADIUS_SQUARED){
                anger ++;
            }
        }

        return anger;
    }


    @Override
    public void getErDone(Cowboy yoursTruly) throws GameActionException {
        RobotController me = yoursTruly.me;
        me.setIndicatorString("Raisin Hell!");
        RobotInfo poorSoul = findTheWeakOne(yoursTruly);

        if (poorSoul != null )
        {
            try {
                drawAttackLine(yoursTruly, poorSoul);
                //try to murder them as best you can
                if(me.isActionReady()) {
                    me.attack(poorSoul.getLocation());
                }
            } catch (GameActionException e) {
                System.err.println("Had trouble attacking "+ poorSoul);
                System.err.println(e);
            }
        }

        // Stand your ground

    }


    private static void drawAttackLine(Cowboy yoursTruly, RobotInfo weakling) {
        yoursTruly.me.setIndicatorLine(yoursTruly.me.getLocation() , weakling.getLocation() , 255,0,0);
    }

    private static RobotInfo findTheWeakOne(Cowboy yoursTruly) {
        RobotController me = yoursTruly.me;
        int minHp = Integer.MAX_VALUE;
        RobotInfo closest = null;
        for(RobotInfo bad:yoursTruly.layOfTheLand.muchachos){
            if (me.canAttack(bad.location)){
                if(closest==null){
                    closest = bad;
                    minHp = bad.getHealth();
                }else if (bad.getHealth()<minHp){
                    closest = bad;
                    minHp = bad.getHealth();
                }
            }
        }

        return closest;
    }

    @Override
    public String getName() {
        return "Raisin' Hell";
    }
}
