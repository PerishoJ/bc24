package tx;

import tx.thinkin.BigPicture;
import tx.thinkin.Noggin;
import tx.thinkin.Util;
import tx.thinkin.idears.BrightIdea;
import battlecode.common.*;

import java.util.Arrays;

import static java.lang.StrictMath.ceil;
import static java.lang.StrictMath.sqrt;

/**
 * Yeehaw!!!
 *
 */
public strictfp class Cowboy {
    /**I think, therefore I am*/
    public RobotController me;
    /**That's usin' your noodle!*/
    public Noggin thinker = new Noggin();
    /**Big sky and broad plains.*/
    public BigPicture layOfTheLand=  new BigPicture();

    public static final int APPETITE_FOR_PUNISHMENT = GameConstants.DEFAULT_HEALTH ;

    public Cowboy(RobotController me){
        this.me = me;
    }


    public void wakeup(){
        layOfTheLand.map = new MapInfo[me.getMapWidth()][me.getMapHeight()];
        Util.mapHeight = me.getMapHeight(); // important for serialization

        findChunkSize();

    }


    /**
     * later, we'll need to chunk the map.
     * This calculates how big the chunks need to be to fit in 1/2 the shared array (some arbitrary ratio...maybe adjust later)
     *
     * The math is kinda funky, and it doesn't catch the corners well AT ALL.
     * @return
     */
    private int findChunkSize(int maxWidth, int maxHeight , int alottedMemory) {
        /**The lot is always a square, so the W and H are the same. 12 is what I came up with for 60X60 @ 32 blocks. +2 is just 15% extra in case game specs change... as always*/
        int minLotWidth = 12 + 2;
        for (int i = 6 ; i <= 12 ; i++){
            if(( ceil(maxWidth/i) * ceil(maxHeight/i)) < alottedMemory){
                minLotWidth = i;
                break;
            }
        }
        return minLotWidth;
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
