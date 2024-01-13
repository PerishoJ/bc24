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
        int riledUp = 0;
        if(isThereAFight(bigPicture) ){
            riledUp += 3;
        } else {
            return 0;
        }
        if (doIhaveMoreAmigosThanYou(bigPicture))
            riledUp += 3;
        if(amILicked(rc))
            riledUp -= 3;
        howMuchTroubleWeStirUpAnyways(bigPicture, rc);
        riledUp -= repercusions(bigPicture,rc) * CHICKEN_SHIT;
        //TODO calculate how much your pals will mess em up
        return riledUp;
    }

    private void howMuchTroubleWeStirUpAnyways(BigPicture bigPicture, RobotController rc) {
        MapLocation whereWeAt = rc.getLocation();
        bigPicture.trouble = howMuchTroubleIsBrewin(bigPicture, whereWeAt);
    }

    /**
     * 
     * @param bigPicture - depends on {@link BigPicture#trouble}
     * @param rc
     * @return percent health remaining as float between 0.0 and 1.0;
     */
    private strictfp float repercusions(BigPicture bigPicture, RobotController rc) {
        return (float) aftermath(bigPicture, rc) / GameConstants.DEFAULT_HEALTH;
    }

    /**
     * 
     * @param bigPicture - depends on {@link BigPicture#trouble}
     * @param rc
     * @return
     */
    private static int aftermath(BigPicture bigPicture, RobotController rc) {
        int afterTheDustSettles = rc.getHealth() - bigPicture.trouble;
        if (afterTheDustSettles<0)
            afterTheDustSettles = 0;
        return afterTheDustSettles;
    }

    private static int howMuchTroubleIsBrewin(BigPicture bigPicture, MapLocation whereWeAt) {
        int trouble = 0;
        for(RobotInfo b : bigPicture.muchachos){
            if(b.getLocation().distanceSquaredTo(whereWeAt) <= GameConstants.ATTACK_RADIUS_SQUARED){
                trouble += ATTACK_DAMAGE;
            }
        }
        return trouble;
    }

    private static boolean isThereAFight(BigPicture bigPicture) {
        return bigPicture.muchachos.size() > 0;
    }

    private static boolean amILicked(RobotController rc) {
        return rc.getHealth() <= Cowboy.APPETITE_FOR_PUNISHMENT;
    }

    public static boolean doIhaveMoreAmigosThanYou(BigPicture bigPicture) {
        return bigPicture.compadres.size() + 1 >= bigPicture.muchachos.size();
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
        // It's never a good idea to chase. Don't do it.
        // Stand your ground. Take locations when the need arises.
        //or else
//        if(justOutOfReach(yoursTruly, poorSoul)){
//            //Find the closest bad guy
//            int minDist = Integer.MAX_VALUE;
//            RobotInfo closest = null;
//            for(RobotInfo bad:yoursTruly.layOfTheLand.muchachos){
//                int distToBaddie = bad.location.distanceSquaredTo(me.getLocation());
//                if (distToBaddie<minDist){
//                    closest = bad;
//                    minDist = distToBaddie;
//                }
//            }
//            //Move towards that dude
//            if(closest!=null) {
//                drawAttackLine(yoursTruly, closest);
//                Direction dir = me.getLocation().directionTo(closest.getLocation());
//                if (me.isActionReady() && me.canMove(dir)) {
//                    try {
//                        yoursTruly.move(dir);
//                    } catch (GameActionException e) {
//                        System.err.println("Error trying to move to the stragglers");
//                    }
//                }
//            }
//        }
    }

//    private static boolean justOutOfReach(Cowboy yoursTruly, RobotInfo poorSoul) {
//        return poorSoul==null && !yoursTruly.layOfTheLand.muchachos.isEmpty();
//    }

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
}
