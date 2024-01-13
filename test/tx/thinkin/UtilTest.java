package tx.thinkin;

import battlecode.common.GameConstants;
import battlecode.common.MapInfo;
import battlecode.common.MapLocation;
import battlecode.common.Team;
import junit.framework.TestCase;

import java.util.Random;

public class UtilTest extends TestCase {

    public static final int MAP_SIZE_DIM_MAX = 61;
    public static Random rnd = new Random();

    public void testTypeSerialization(){
        MapLocation loc = new MapLocation(37,43);
        Team homeTeam = Team.A;
        for(int i = 0 ; i<8 ; i ++){
            MapInfo info = Util.deserializeType(loc, (int)i, homeTeam);
            int ser = Util.serializeType(info,homeTeam);
            MapInfo deserInfo = Util.deserializeType(loc,ser,homeTeam);
            assertEquals("Testing case " + i, info.toString(), deserInfo.toString());
        }
    }


    public void testLocSerialization() throws Util.ForgotToInitMapSize {
        for(int i = 0 ; i< 100 ; i++){
            int rngx = GameConstants.MAP_MAX_WIDTH - GameConstants.MAP_MIN_WIDTH;
            int rngy = GameConstants.MAP_MAX_HEIGHT - GameConstants.MAP_MIN_HEIGHT;

            int boundx = GameConstants.MAP_MIN_WIDTH + rnd.nextInt(rngx);
            int boundy = GameConstants.MAP_MIN_HEIGHT + rnd.nextInt(rngy);
            Util.mapHeight = (int)boundy;
            MapLocation loc = new MapLocation(rnd.nextInt(boundx),rnd.nextInt(boundy));
            int ser = Util.serializeMapLocation(loc);
            MapLocation deserLoc = Util.deserializeMapLoc(ser);
            assertEquals("Comparing oring "+loc+ " to " + deserLoc , loc.x, deserLoc.x);
            assertEquals("Comparing oring "+loc+ " to " + deserLoc , loc.y, deserLoc.y);

        }
    }
    


    public void testMapInfoSerialization() throws Util.ForgotToInitMapSize {
        Team homeTeam = Team.A;
        for(int i = 0 ; i<8 ; i ++){

            for(int j = 0 ; j< 100 ; j++){
                int rngx = GameConstants.MAP_MAX_WIDTH - GameConstants.MAP_MIN_WIDTH;
                int rngy = GameConstants.MAP_MAX_HEIGHT - GameConstants.MAP_MIN_HEIGHT;

                int boundx = GameConstants.MAP_MIN_WIDTH + rnd.nextInt(rngx);
                int boundy = GameConstants.MAP_MIN_HEIGHT + rnd.nextInt(rngy);
                Util.mapHeight = (int)boundy;
                MapLocation loc = new MapLocation(rnd.nextInt(boundx),rnd.nextInt(boundy));

                MapInfo info = Util.deserializeType(loc, (int)i, homeTeam);

                int serInfo = Util.serializeMapInfo(info,homeTeam);

                MapInfo deserInfo = Util.deserializeMapInfo(serInfo,homeTeam);


                assertEquals("Comparing oring "+loc+ " to " + deserInfo.getMapLocation(), loc.x, deserInfo.getMapLocation().x);
                assertEquals("Comparing oring "+loc+ " to " + deserInfo.getMapLocation() , loc.y, deserInfo.getMapLocation().y);
                assertEquals("Testing case " + i, info.toString(), deserInfo.toString());

            }





        }


    }

}