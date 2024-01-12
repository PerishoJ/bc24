package apache.thinkin;

import battlecode.common.MapInfo;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;

/**
 *  Just a list of stuff that we're concerned about.
 *
 *  yep, it ain't pretty, but neither is life. Get over it.
 */
public strictfp class BigPicture {

    /** bad guys*/
    public RobotInfo[] muchachos ;


    /**Homies brah*/
    public RobotInfo[] compadres ;

    /**
     * How much damage could surrounding enemies do to you this turn.
     * Figured in {@link apache.thinkin.idears.RaiseHell#howMuchTroubleWeStirUpAnyways(BigPicture, RobotController)}
     */
    public int trouble;
    public MapLocation lastLocation;

    public RobotInfo friendInNeed = null;

    public static Plot[][] map;
}
