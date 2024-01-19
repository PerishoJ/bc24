package tx.thinkin.idears;

import battlecode.common.*;
import tx.Cowboy;
import tx.map.BugPathing;
import tx.thinkin.BigPicture;
/**
 * Get the F&$# Out of Dodge!
 */
public class GetOnGet implements BrightIdea{

    Direction retreatDir;
    BugPathing bug ;

    @Override
    public int howAboutThat(BigPicture bigPicture, RobotController rc) {
        if(bug==null) bug = new BugPathing(bigPicture,rc);
        if( rc.getHealth() <= Cowboy.APPETITE_FOR_PUNISHMENT && !bigPicture.muchachos.isEmpty() ){
            return 4;
        } else {
            return 0;
        }
    }

    @Override
    public void getErDone(Cowboy yoursTruly) throws GameActionException {
        RobotController I = yoursTruly.me;
        RobotInfo closestFriend = yoursTruly.layOfTheLand.closestHealthyAlly;
        retreatDir= null;
        if(closestFriend!=null){
            try {
                bug.go(closestFriend.location);
            } catch (Exception e) {
                System.err.println("error pathing to closest friend during retreat" + e);
            }
        } else {
            MapLocation closeSpawn = findClosestSpawn(I);
            // there HAS to be spawn locations...or there would be no game. Assume not null.
            try {
                bug.go(closeSpawn);
            } catch (Exception e) {
                System.err.println("Error trying to nav to nearest spawn." + e.getMessage());
            }
        }


        I.setIndicatorString("RETREAT!");
    }

    private static MapLocation findClosestSpawn(RobotController I) {
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
        return closeSpawn;
    }

    @Override
    public String getName() {
        return "Adios! *runs*";
    }
}
