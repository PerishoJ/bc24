package tx.thinkin.idears;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import tx.Cowboy;
import tx.map.BugPathing;
import tx.thinkin.BigPicture;

public class TestBugNav implements BrightIdea{

    BugPathing bug ;
    MapLocation edge;
    @Override
    public int howAboutThat(BigPicture bigPicture, RobotController rc) {
        if(bug == null) bug = new BugPathing(bigPicture,rc);
        edge = new MapLocation( rc.getMapWidth(), rc.getMapHeight());
        return 100;
    }

    @Override
    public void getErDone(Cowboy me) throws GameActionException {
        try {
            RobotController I = me.me;
            Direction dir = bug.go(edge);
            if(I.canMove(dir)){
                I.move(dir);
                I.setIndicatorDot(I.getLocation().add(dir) , 0,100,0);
            } else if(I.canMove(dir.rotateLeft())){
                I.setIndicatorDot(I.getLocation().add(dir) , 255,100,0);
                I.move(dir.rotateLeft());
                I.setIndicatorDot(I.getLocation().add(dir.rotateLeft()) , 0,100,0);
            }else if(I.canMove(dir.rotateRight())){
                I.setIndicatorDot(I.getLocation().add(dir) , 255,100,0);
                I.setIndicatorDot(I.getLocation().add(dir.rotateLeft()) , 255,100,0);
                I.setIndicatorDot(I.getLocation().add(dir.rotateRight()) , 0,100,0);
                I.move(dir.rotateRight());
            } else {
                I.setIndicatorDot(I.getLocation().add(dir) , 255,100,0);
                I.setIndicatorDot(I.getLocation().add(dir.rotateLeft()) , 255,100,0);
                I.setIndicatorDot(I.getLocation().add(dir.rotateRight()) , 255,100,0);
            }
        } catch (Exception e) {
            // This was getting SPAMMY because
            // System.err.println(e);
        }

    }

    @Override
    public String getName() {
        return "Test Bug Nav";
    }
}
