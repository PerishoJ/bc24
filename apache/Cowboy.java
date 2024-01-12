package apache;

import apache.thinkin.BigPicture;
import apache.thinkin.Noggin;
import apache.thinkin.idears.BrightIdea;
import battlecode.common.*;

import java.awt.*;
import java.util.Arrays;

/**
 * Yeehaw!!!
 *
 */
public class Cowboy {
    /**I think, therefore I am*/
    public RobotController me;
    /**That's usin' your noodle!*/
    public Noggin thinker = new Noggin();
    /**Big sky and broad plains.*/
    public BigPicture layOfTheLand=  new BigPicture();

    public static final int APPETITE_FOR_PUNISHMENT = GameConstants.DEFAULT_HEALTH / ;

    public Cowboy(RobotController me){
        this.me = me;
    }

    public BrightIdea takeStock(){
        try {
            RobotInfo[] folksRoundHere = me.senseNearbyRobots(-1, null);

            layOfTheLand.compadres = Arrays.stream(folksRoundHere)
                    .filter(folk -> folk !=null && folk.getTeam() == me.getTeam())
                    .toArray(RobotInfo[]::new);

            layOfTheLand.muchachos = (RobotInfo[]) Arrays.stream(folksRoundHere)
                    .filter(folk -> folk !=null && folk.getTeam() == me.getTeam().opponent())
                    .toArray(RobotInfo[]::new);

        } catch (GameActionException e){
            System.out.println("Trouble Lookin' Around");
            System.out.println(e);
            // kinda useless, but it won't crash, I guess.
            layOfTheLand = new BigPicture();
            layOfTheLand.muchachos = new RobotInfo[0];
            layOfTheLand.compadres = new RobotInfo[0];
        }

        return thinker.ponder(layOfTheLand, me);
    }

    public void move(Direction dir) throws GameActionException {
        layOfTheLand.lastLocation = me.getLocation();
        me.move(dir);
    }

}
