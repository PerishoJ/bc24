package tx.thinkin;

import battlecode.common.MapInfo;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;

import java.util.List;

/**
 *  Just a list of stuff that we're concerned about.
 *
 *  yep, it ain't pretty, but neither is life. Get over it.
 */
public strictfp class BigPicture {

    public MapInfo[][] map;
    /** bad guys*/
    public List<RobotInfo> muchachos ;


    /**Homies brah*/
    public List<RobotInfo> compadres ;

    /**
     * How much damage could surrounding enemies do to you this turn.
     * Figured in {@link tx.thinkin.idears.RaiseHell#howMuchTroubleWeStirUpAnyways(BigPicture, RobotController)}
     */
    public int trouble;
    public MapLocation lastLocation;

    public RobotInfo friendInNeed = null;
}
