package tx.thinkin.idears;

import battlecode.common.Direction;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.TrapType;
import battlecode.world.Trap;
import tx.Cowboy;
import tx.RobotPlayer;
import tx.thinkin.BigPicture;

import static tx.RobotPlayer.directions;
import static tx.RobotPlayer.rng;

public class Trapper implements BrightIdea{

    public static final int PERCENT_CHANCE_BLD_TRAP = 60;
    public static final int NUM_DIRECTIONS = 8;
    public static final int MAX_TRAP_ZONE = 100;
    public static final int MIN_TRAP_ZONE = 36;

    boolean isActive = false;
    TrapType trapType;
    @Override
    public int howAboutThat(BigPicture bigPicture, RobotController rc) {
        if( ( isGoldilocksZone(bigPicture) || enemyBaseIsClose(bigPicture))
                && whatTheHell()){
            if(!isActive){
                trapType = rng.nextBoolean() ? TrapType.EXPLOSIVE : TrapType.STUN;
            }
            isActive = true;
            if(rc.getCrumbs()>trapType.buildCost) {
                return 5;
            }
        }
        isActive = false;
        return 0;

    }

    private static boolean enemyBaseIsClose(BigPicture bigPicture) {
        return bigPicture.closestEnemyFlagDist < 100;
    }

    private static boolean whatTheHell() {
        return rng.nextInt(100) < PERCENT_CHANCE_BLD_TRAP;
    }


    /**
     * basically a ring around base.
     *
     * @param bigPicture
     * @return
     */
    private static boolean isGoldilocksZone(BigPicture bigPicture) {
        return (bigPicture.closestAllyFlagDist <= MAX_TRAP_ZONE && bigPicture.closestAllyFlagDist >= MIN_TRAP_ZONE)
                || (bigPicture.closestSpawnDist <= MAX_TRAP_ZONE && bigPicture.closestSpawnDist >= MIN_TRAP_ZONE);
    }

    @Override
    public void getErDone(Cowboy me) throws Exception {
        Direction trapDir = directions[rng.nextInt(directions.length)];
        for(int i = 0 ; i< NUM_DIRECTIONS ; i++){
            MapLocation trapLoc = me.me.getLocation().add(trapDir);
            if(me.me.canBuild(trapType,trapLoc)){
                me.me.build(trapType,trapLoc);
            } else {
                trapDir = trapDir.rotateLeft();
            }
        }
    }


    @Override
    public String getName() {
        return "trapping";
    }
}
