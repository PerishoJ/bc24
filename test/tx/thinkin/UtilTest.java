package tx.thinkin;

import battlecode.common.GameConstants;
import battlecode.common.MapInfo;
import battlecode.common.MapLocation;
import battlecode.common.Team;
import junit.framework.TestCase;
import tx.comms.SerialUtil;

import java.util.Random;

public class UtilTest extends TestCase {

    public static final int MAP_SIZE_DIM_MAX = 61;
    public static Random rnd = new Random();

    boolean isAuth = false;

    // used to track values for time when initializing.
    // Handles the scenario when someone init's on a wallclock that hasn't been updated so that they don't move the clock bad if
    // they ever are the first to see the time.
    int initTimeTable[][] ;

    // How many turns this bot has been up
    int turnCount = 0;

    int gameTime = 0;

    public void testTypeSerialization(){
        SerialUtil uut = new SerialUtil(40); // some rnd value
        MapLocation loc = new MapLocation(37,43); //rnd
        Team homeTeam = Team.A;
        for(int i = 0 ; i<8 ; i ++){
            MapInfo info = uut.deserializeType(loc, (int)i, homeTeam);
            int ser = uut.serializeType(info,homeTeam);
            MapInfo deserInfo = uut.deserializeType(loc,ser,homeTeam);
            assertEquals("Testing case " + i, info.toString(), deserInfo.toString());
        }
    }


    public void testLocSerialization() throws SerialUtil.ForgotToInitMapSize {
        for(int i = 0 ; i< 100 ; i++){
            int rngx = GameConstants.MAP_MAX_WIDTH - GameConstants.MAP_MIN_WIDTH;
            int rngy = GameConstants.MAP_MAX_HEIGHT - GameConstants.MAP_MIN_HEIGHT;
            int boundx = GameConstants.MAP_MIN_WIDTH + rnd.nextInt(rngx);
            int boundy = GameConstants.MAP_MIN_HEIGHT + rnd.nextInt(rngy);
            SerialUtil uut = new SerialUtil(boundy);
            MapLocation loc = new MapLocation(rnd.nextInt(boundx),rnd.nextInt(boundy));
            int ser = uut.serializeMapLocation(loc);
            MapLocation deserLoc = uut.deserializeMapLoc(ser);
            assertEquals("Comparing oring "+loc+ " to " + deserLoc , loc.x, deserLoc.x);
            assertEquals("Comparing oring "+loc+ " to " + deserLoc , loc.y, deserLoc.y);

        }
    }
    


    public void testMapInfoSerialization() throws SerialUtil.ForgotToInitMapSize {
        Team homeTeam = Team.A;
        for(int i = 0 ; i<8 ; i ++){
            for(int j = 0 ; j< 100 ; j++){


                int rngx = GameConstants.MAP_MAX_WIDTH - GameConstants.MAP_MIN_WIDTH;
                int rngy = GameConstants.MAP_MAX_HEIGHT - GameConstants.MAP_MIN_HEIGHT;

                int boundx = GameConstants.MAP_MIN_WIDTH + rnd.nextInt(rngx);
                int boundy = GameConstants.MAP_MIN_HEIGHT + rnd.nextInt(rngy);
                SerialUtil uut = new SerialUtil(boundy);
                MapLocation loc = new MapLocation(rnd.nextInt(boundx),rnd.nextInt(boundy));

                MapInfo info = uut.deserializeType(loc, (int)i, homeTeam);

                int serInfo = uut.serializeMapInfo(info,homeTeam);

                MapInfo deserInfo = uut.deserializeMapInfo(serInfo,homeTeam);


                assertEquals("Comparing oring "+loc+ " to " + deserInfo.getMapLocation(), loc.x, deserInfo.getMapLocation().x);
                assertEquals("Comparing oring "+loc+ " to " + deserInfo.getMapLocation() , loc.y, deserInfo.getMapLocation().y);
                assertEquals("Testing case " + i, info.toString(), deserInfo.toString());

            }
        }
    }


    // The equivalent of null on the shared array.
    public void testNullMessage() throws SerialUtil.ForgotToInitMapSize {
        SerialUtil uut  = new SerialUtil( GameConstants.MAP_MAX_HEIGHT);
        int serInfo = 0b1111_1111_1111_1111;
        MapInfo deser = uut.deserializeMapInfo(serInfo,Team.A);
        System.out.println(deser);
        assert deser.getMapLocation().x > GameConstants.MAP_MAX_WIDTH;
    }

}