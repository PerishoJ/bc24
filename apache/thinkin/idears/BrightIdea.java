package apache.thinkin.idears;

import apache.thinkin.BigPicture;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;

import java.awt.*;
import apache.Cowboy;
public interface BrightIdea {
    /**
     * how good is this wild hare up that's found it's way up your behind?
     *
     * @return you know what it is
     */
    int howAboutThat(BigPicture bigPicture , RobotController rc);

    /**
     * Do it to it
     */
    void getErDone(Cowboy me) throws GameActionException;

}
