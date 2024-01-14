package tx;

import tx.comms.TurnCount;
import tx.thinkin.BigPicture;
import tx.thinkin.Noggin;
import tx.comms.CommsUtil;
import tx.thinkin.idears.BrightIdea;
import battlecode.common.*;
import tx.map.UnknownMapInfo;

import java.util.LinkedList;

import static java.lang.StrictMath.ceil;

/**
 * Yeehaw!!!
 *
 */
public strictfp class Cowboy {
    public static final int INVALID_MAP_MESSAGE = 0b1111_1111_1111_1111;
    /**I think, therefore I am*/
    public RobotController me;
    /**That's usin' your noodle!*/
    public Noggin thinker = new Noggin();
    /**Big sky and broad plains.*/
    public BigPicture layOfTheLand=  new BigPicture();

    public final TurnCount turnCount;
    private CommsUtil serialUtil;
    public static final int APPETITE_FOR_PUNISHMENT = 450 ; // about 3 hits

    public Cowboy(RobotController me, TurnCount turnCount){
        this.me = me;
        this.turnCount = turnCount;
    }


    public void wakeup(){
        layOfTheLand.map = new MapInfo[me.getMapWidth()][me.getMapHeight()];
        pencilOutTheMap();
        serialUtil = new CommsUtil( me.getMapHeight() , me,turnCount);

        findChunkSize(me.getMapWidth(),me.getMapHeight(),GameConstants.SHARED_ARRAY_LENGTH / 2);

    }

    /**
     * Just dump in some junk for now. We'll fill this out later.
     */
    private void pencilOutTheMap() {
        for(int x = 0 ; x < me.getMapWidth() ; x++){
            for (int y = 0 ; y < me.getMapHeight() ; y++){
                layOfTheLand.map[x][y] = new UnknownMapInfo(x,y);
            }
        }
    }


    /**
     * TODO We need to chunk the map eventually...this way, I guess.
     * This calculates how big the chunks need to be to fit in 1/2 the shared array (some arbitrary ratio...maybe adjust later)
     *
     * Probably belongs somewhere else...but we'll get there when we get there.
     *
     * The math is kinda funky, and it doesn't catch the corners well AT ALL.
     * @return
     */
    private int findChunkSize(int maxWidth, int maxHeight , int alottedMemory) {
        /**The lot is always a square, so the W and H are the same. 12 is what I came up with for 60X60 @ 32 blocks. +2 is just 15% extra in case game specs change... as always*/
        int minLotWidth = 12 + 2;
        // Try to divide the map into bigger squares.
        // Just check until progressively larger values until you find one that works for this particular map.
        // P.S. The top and the right side of the map won't usually map out perfectly...So lets just take the center ground.
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

            layOfTheLand.compadres = new LinkedList<>();
            layOfTheLand.muchachos = new LinkedList<>();

            for (RobotInfo bot : folksRoundHere) {
                if (bot.getTeam() == me.getTeam()) {
                    layOfTheLand.compadres.add(bot);
                } else {
                    layOfTheLand.muchachos.add(bot);
                }
            }

            //TODO setup the comms logic for sharing map data for the first 200 turns.

        } catch (GameActionException e){
            System.out.println("Trouble Lookin' Around");
            System.out.println(e);
            // kinda useless, but it won't crash, I guess.
            //layOfTheLand = new BigPicture();
            layOfTheLand.muchachos = new LinkedList<>();
            layOfTheLand.compadres = new LinkedList<>();
        }

        return thinker.ponder(layOfTheLand, me);
    }

    private boolean isLocationInMap(MapLocation loc) {
        return loc.x < me.getMapWidth() && loc.y < me.getMapHeight() && loc.x >= 0 && loc.y >= 0;
    }

    public void move(Direction dir) throws GameActionException {
        layOfTheLand.lastLocation = me.getLocation();
        me.move(dir);
    }

}
