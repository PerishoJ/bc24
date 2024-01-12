package tx.thinkin;

import battlecode.common.MapInfo;
import battlecode.common.MapLocation;
import battlecode.common.Team;
import junit.framework.TestCase;

public class UtilTest extends TestCase {

    public void testSerialization(){
        MapLocation loc = new MapLocation(37,43);
        Team homeTeam = Team.A;
        for(int i = 0 ; i<8 ; i ++){
            MapInfo info = Util.deserializeType(loc, (short)i, homeTeam);
            short ser = Util.serializeType(info,homeTeam);
            MapInfo deserInfo = Util.deserializeType(loc,ser,homeTeam);
            assertEquals("Testing case " + i, info.toString(), deserInfo.toString());
        }
    }



}