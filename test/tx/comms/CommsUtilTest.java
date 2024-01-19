package tx.comms;

import battlecode.common.*;
import jdk.jfr.Enabled;
import junit.framework.TestCase;

import java.util.Random;

public class CommsUtilTest extends TestCase {

    public static final int MAX_DATA_INT_SIZE_PLUS_1 = (1 << 16);
    public static Random rnd = new Random();

    private final MockSharedArray sharedArray = new MockSharedArray();

    private final TurnCount turnCount = new TurnCount();


    int gameTime = 0;

    public void testTypeSerialization(){
        CommsUtil uut = new CommsUtil(40,sharedArray,turnCount); // some rnd value
        MapLocation loc = new MapLocation(37,43); //rnd
        Team homeTeam = Team.A;
        for(int i = 0 ; i<8 ; i ++){
            MapInfo info = uut.deserializeType(loc, (int)i, homeTeam);
            int ser = uut.serializeType(info,homeTeam);
            MapInfo deserInfo = uut.deserializeType(loc,ser,homeTeam);
            assertEquals("Testing case " + i, info.toString(), deserInfo.toString());
        }
    }


    public void testLocSerialization() throws CommsUtil.ForgotToInitMapSize {
        for(int i = 0 ; i< 100 ; i++){
            int rngx = GameConstants.MAP_MAX_WIDTH - GameConstants.MAP_MIN_WIDTH;
            int rngy = GameConstants.MAP_MAX_HEIGHT - GameConstants.MAP_MIN_HEIGHT;
            int boundx = GameConstants.MAP_MIN_WIDTH + rnd.nextInt(rngx);
            int boundy = GameConstants.MAP_MIN_HEIGHT + rnd.nextInt(rngy);
            CommsUtil uut = new CommsUtil(boundy,sharedArray,turnCount);
            MapLocation loc = new MapLocation(rnd.nextInt(boundx),rnd.nextInt(boundy));
            int ser = uut.serializeMapLocation(loc);
            MapLocation deserLoc = uut.deserializeMapLoc(ser);
            assertEquals("Comparing oring "+loc+ " to " + deserLoc , loc.x, deserLoc.x);
            assertEquals("Comparing oring "+loc+ " to " + deserLoc , loc.y, deserLoc.y);

        }
    }
    


    public void testMapInfoSerialization() throws CommsUtil.ForgotToInitMapSize {
        Team homeTeam = Team.A;
        for(int i = 0 ; i<8 ; i ++){
            for(int j = 0 ; j< 100 ; j++){
                int rngx = GameConstants.MAP_MAX_WIDTH - GameConstants.MAP_MIN_WIDTH;
                int rngy = GameConstants.MAP_MAX_HEIGHT - GameConstants.MAP_MIN_HEIGHT;

                int boundx = GameConstants.MAP_MIN_WIDTH + rnd.nextInt(rngx);
                int boundy = GameConstants.MAP_MIN_HEIGHT + rnd.nextInt(rngy);
                CommsUtil uut = new CommsUtil(boundy,sharedArray,turnCount);
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
    public void testNullMessage() throws CommsUtil.ForgotToInitMapSize {
        CommsUtil uut  = new CommsUtil( GameConstants.MAP_MAX_HEIGHT,sharedArray,turnCount);
        int serInfo = 0b1111_1111_1111_1111;
        MapInfo deser = uut.deserializeMapInfo(serInfo,Team.A);
        System.out.println(deser);
        assert deser.getMapLocation().x > GameConstants.MAP_MAX_WIDTH;
    }


    public void testInitForWholeGame() throws GameActionException {
        //GIVEN
        CommsUtil uut  = new CommsUtil( GameConstants.MAP_MAX_HEIGHT,sharedArray,turnCount);
        //WHEN
        uut.preTurnWarmup();
        //THEN
        int initLastModifiedValue = CommsUtil.LastModified.createBinary(turnCount.get()).rawBits;
        assertEquals(initLastModifiedValue , sharedArray.readSharedArray(0));
        assertEquals(0 , sharedArray.readSharedArray(1));
    }

    public void testTestReadWriteCycleFirstTurn() throws GameActionException {
        //GIVEN
        // some unit inits game
        CommsUtil uut  = new CommsUtil( GameConstants.MAP_MAX_HEIGHT,sharedArray,turnCount);
        uut.preTurnWarmup();
        int[] inputData = new int[]{5,6,7};
        for(int i = 0 ; i<inputData.length ; i++) {
            uut.writeToArrayBuffer(inputData[i]=rnd.nextInt(MAX_DATA_INT_SIZE_PLUS_1));
        }
        // Write to evens
        uut.flushArrayBuffer();

        // We read from odds
        turnCount.inc();
        uut.preTurnWarmup();
        int[] data = uut.readAll();

        assertEquals(inputData.length,data.length);
        for(int i = 0 ; i<inputData.length ; i++) {
            assertEquals(inputData[i], data[i]);
        }
    }


    public void testReadWriteCycleSecondTurn() throws GameActionException {
        //GIVEN
        // some unit inits game
        CommsUtil uut  = new CommsUtil( GameConstants.MAP_MAX_HEIGHT,sharedArray,turnCount);
        uut.preTurnWarmup();
        int[] inputData = new int[3];
        writeSomeData(inputData, uut);
        // Write to evens
        uut.flushArrayBuffer();

        // Write to some odds
        turnCount.inc();
        uut.preTurnWarmup();
        // This will overwrite inputData with fresh values and write them
        writeSomeData(inputData, uut);
        uut.flushArrayBuffer();

        turnCount.inc();
        uut.preTurnWarmup();
        int[] data = uut.readAll();

        assertEquals(inputData.length,data.length);
        for(int i = 0 ; i<inputData.length ; i++) {
            assertEquals(inputData[i], data[i]);
        }
    }

    public void testReadWriteCycleThirdTurn() throws GameActionException {
        //GIVEN
        // some unit inits game
        CommsUtil uut  = new CommsUtil( GameConstants.MAP_MAX_HEIGHT,sharedArray,turnCount);
        uut.preTurnWarmup();
        int[] inputData = new int[3];
        writeSomeData(inputData, uut);
        // Write to evens
        uut.flushArrayBuffer();

        // Write to some odds
        turnCount.inc();
        uut.preTurnWarmup();
        // This will overwrite inputData with fresh values and write them
        writeSomeData(inputData, uut);
        uut.flushArrayBuffer();

        // Write to some odds
        turnCount.inc();
        uut.preTurnWarmup();
        // This will overwrite inputData with fresh values and write them
        writeSomeData(inputData, uut);
        uut.flushArrayBuffer();

        turnCount.inc();
        uut.preTurnWarmup();
        int[] data = uut.readAll();

        assertEquals(inputData.length,data.length);
        for(int i = 0 ; i<inputData.length ; i++) {
            assertEquals(inputData[i], data[i]);
        }
    }

    public void testReadWriteCycleFirstTurn_TwoBots() throws GameActionException {
        //GIVEN
        // first bot gets its first  turn
        CommsUtil uut  = new CommsUtil( GameConstants.MAP_MAX_HEIGHT,sharedArray,turnCount);
        uut.preTurnWarmup();
        int[] inputData = new int[3];
        writeSomeData(inputData, uut);
        uut.flushArrayBuffer();

        // then the second bot gets its first turn
        CommsUtil anotherBot  = new CommsUtil( GameConstants.MAP_MAX_HEIGHT,sharedArray,turnCount);
        anotherBot.preTurnWarmup();
        int[] otherInputData = new int[4];
        writeSomeData(otherInputData, anotherBot);
        anotherBot.flushArrayBuffer();

        turnCount.inc();
        //Check the first turn
        int[] allValidData = concatArrays(inputData, otherInputData);

        uut.preTurnWarmup();
        int[] data = uut.readAll();
        // Validate turn 1 values for first bot
        assertEquals(allValidData.length,data.length);
        for(int i = 0 ; i<data.length ; i++) {
            assertEquals("Error on index " + i, allValidData[i], data[i]);
        }

        inputData = new int[3];
        writeSomeData(inputData, uut);
        uut.flushArrayBuffer();
        // Validate turn 1 values for second both
        assertEquals(allValidData.length,data.length);
        anotherBot.preTurnWarmup();
        int[] dataII = uut.readAll();

        for(int i = 0 ; i<dataII.length ; i++) {
            assertEquals("Error on index " + i,allValidData[i], dataII[i]);
        }
        otherInputData = new int[4];
        writeSomeData(otherInputData, anotherBot);
        anotherBot.flushArrayBuffer();

        // WHEN
        turnCount.inc();
        uut.preTurnWarmup();
        anotherBot.preTurnWarmup();
        data = uut.readAll();
        dataII = uut.readAll();


        allValidData = concatArrays(inputData, otherInputData);
        //THEN
        assertEquals(allValidData.length,data.length);
        for(int i = 0 ; i<data.length ; i++) {
            assertEquals("Error on index " + i, allValidData[i], data[i]);
        }
        for(int i = 0 ; i<dataII.length ; i++) {
            assertEquals("Error on index " + i,allValidData[i], dataII[i]);
        }
    }

    private int[] concatArrays(int[] inputData, int[] otherInputData) {
        int[] validData = new int [inputData.length + otherInputData.length];
        for(int i = 0; i < inputData.length ; i ++){
            validData[i] = inputData[i];
        }
        for(int i = 0; i < otherInputData.length ; i ++){
            validData[i + inputData.length ] = otherInputData[i];
        }
        return validData;
    }


    public void testLastModifiedBlock(){
        for(int i =0 ; i< 100 ; i++) {
            CommsUtil.LastModified lastModified = CommsUtil.LastModified.createBinary(i);
            assertTrue(lastModified.isValid());
            CommsUtil.LastModified deser = CommsUtil.LastModified.readBinary(lastModified.rawBits);
            assertTrue(deser.isValid());
        }

    }


    private static void writeSomeData(int[] inputData, CommsUtil uut) throws GameActionException {
        for(int i = 0; i< inputData.length ; i++) {
            inputData[i]=rnd.nextInt(MAX_DATA_INT_SIZE_PLUS_1);
            uut.writeToArrayBuffer(inputData[i]);
        }
    }

}