package tx.comms;

import battlecode.common.MapInfo;
import battlecode.common.MapLocation;
import battlecode.common.Team;
import battlecode.common.TrapType;
import junit.framework.TestCase;

import static org.junit.Assert.assertEquals;

public class BcApiTest extends TestCase {

    public void testApiSpawnA(){
        Team hometeam = Team.A;
        MapLocation loc = new MapLocation( 10 ,17);
        MapInfo info = new MapInfo(loc,true ,false, false ,hometeam.ordinal(),false,0, TrapType.NONE, Team.NEUTRAL);
        assertEquals( Team.A.ordinal() , info.getSpawnZoneTeam());
    }

    public void testApiSpawnB(){
        Team hometeam = Team.B;
        MapLocation loc = new MapLocation( 10 ,17);
        MapInfo info = new MapInfo(loc,true ,false, false ,hometeam.ordinal(),false,0, TrapType.NONE, Team.NEUTRAL);
        assertEquals( Team.B.ordinal() , info.getSpawnZoneTeam());
    }
}
