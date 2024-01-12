package apache.thinkin.idears;

import apache.Cowboy;
import apache.thinkin.BigPicture;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

import static apache.thinkin.idears.RaiseHell.doIhaveMoreAmigosThanYou;

/**
 * Get the F&$# Out of Dodge!
 */
public class GetOnGet implements BrightIdea{

    Direction retreatDir;
    @Override
    public int howAboutThat(BigPicture bigPicture, RobotController rc) {
        if( rc.getHealth() <= Cowboy.APPETITE_FOR_PUNISHMENT
                || !doIhaveMoreAmigosThanYou(bigPicture) ){
            return 10;
        } else {
            return 0;
        }
    }

    @Override
    public void getErDone(Cowboy yoursTruly) throws GameActionException {
        RobotController I = yoursTruly.me;


        int minSpawnDist = Integer.MAX_VALUE;
        MapLocation closeSpawn = null;
        int tempDist = Integer.MAX_VALUE;
        for(MapLocation spawn : I.getAllySpawnLocations()){
            tempDist = I.getLocation().distanceSquaredTo(spawn);
            if(closeSpawn==null){
                minSpawnDist = tempDist;
                closeSpawn =spawn;
            } else {
                if(tempDist<minSpawnDist){
                    minSpawnDist = tempDist;
                    closeSpawn =spawn;
                }
            }
        }
        // there HAS to be spawn locations...or there would be no game. Assume not null.
        retreatDir = I.getLocation().directionTo(closeSpawn);

        if(I.canMove(retreatDir)){
            I.move(retreatDir);
        } else if (I.canMove(retreatDir.rotateLeft())){
            I.move(retreatDir.rotateLeft());
        } else if (I.canMove(retreatDir.rotateRight())) {
            I.move(retreatDir.rotateRight());
        }else if (I.canMove(retreatDir.rotateLeft().rotateLeft())){
            I.move(retreatDir.rotateLeft().rotateLeft());
        } else if (I.canMove(retreatDir.rotateRight().rotateRight())) {
            I.move(retreatDir.rotateRight().rotateRight());
        }
    }
}
