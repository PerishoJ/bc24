package tx.comms;

import battlecode.common.*;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class CommsUtil {

    /** This is hardcoded!! YAY! :D */
    public static final int BITS_TO_REPRESENT_MAX_TURN_VALUE = 11;

    /**essentially a 5 bit mask...uses {@link #BITS_TO_REPRESENT_MAX_TURN_VALUE} to account for when teh devs change things, we can change quickly*/
    public static final int HASH_MASK = (1 << (16 - BITS_TO_REPRESENT_MAX_TURN_VALUE)) - 1; 
    public static final int TURN_COUNT_BIT_MASK = (1<<BITS_TO_REPRESENT_MAX_TURN_VALUE) -1;
    public static final int TURN_COUNT_OFFSET = 1;
    public static final int LAST_MODIFIED_ADDR = 0;
    public static final int INDEX_ADDR = 1;
    public static final int PRE_200_PARTITION_SIZE = 31;
    public static final int INDEX_HEADER = 2;
    public static final int INDEX_PARTITION_BITS = 8;
    private final int mapHeight;

    private final SharedArrayWrapper sharedArray;

    private final TurnCount turnCount;

    private final Team homeTeam;

    public CommsUtil(int mapHeight, SharedArrayWrapper sharedArray, TurnCount turnCount, Team hometeam){
        this.mapHeight = mapHeight;
        this.sharedArray =sharedArray;
        this.turnCount = turnCount;
        this.homeTeam = hometeam;
    }

    /**
     * FOR TESTING ONLY, because I added the Hometeam variable later, and was too lazy to add it in everywhere.
     * @param mapHeight
     * @param sharedArray
     * @param turnCount
     */
    CommsUtil(int mapHeight, SharedArrayWrapper sharedArray, TurnCount turnCount){
        this.mapHeight = mapHeight;
        this.sharedArray =sharedArray;
        this.turnCount = turnCount;
        this.homeTeam = Team.A; // Because we ARE team A. Duh!
    }

    public CommsUtil(int mapHeight , RobotController rc , TurnCount turnCount){
        this.mapHeight = mapHeight;
        sharedArray = new RobotSharedArray(rc);
        this.turnCount = turnCount;
        this.homeTeam = rc.getTeam();
    }

    //###############################################################################################
    //############# serialization stuff
    //################################################################################################

    /** artifact for when this class was just static methods*/
    @Deprecated  public  class ForgotToInitMapSize extends Exception{}

    public  int serializeMapLocation ( MapLocation loc) throws ForgotToInitMapSize {
        if(mapHeight ==0) throw new ForgotToInitMapSize();
        return (int)((loc.x * mapHeight) + loc.y);
    }

    public  MapLocation deserializeMapLoc( int loc) throws ForgotToInitMapSize {
        if(mapHeight ==0) throw new ForgotToInitMapSize();
        return new MapLocation(( /*int division == floor func*/ loc/ mapHeight) , loc% mapHeight);
    }

    public  int serializeMapInfo(MapInfo info , Team homeTeam) throws ForgotToInitMapSize {
        if(mapHeight ==0) throw new ForgotToInitMapSize();
        int type = serializeType(info,homeTeam);
        int loc = serializeMapLocation(info.getMapLocation());
        return (int)((type<<12) | loc);
    }

    public  MapInfo deserializeMapInfo(int ser , Team homeTeam) throws ForgotToInitMapSize {
        if(mapHeight ==0) throw new ForgotToInitMapSize();
        int rawLoc  = ser & 0b0000_1111_1111_1111;
        int rawType =(int) (ser >> 12);
        MapLocation loc = deserializeMapLoc((int)rawLoc);
        return deserializeType( loc , rawType , homeTeam );
    }

    public  int serializeType(MapInfo loc , Team homeTeam) {
        Team spawnZone = null;
        switch (loc.getSpawnZoneTeam()){
            case 1:
                spawnZone = Team.A;
                break;
            case 2:
                spawnZone = Team.B;
                break;
            default:
                spawnZone = Team.NEUTRAL;
                break;
        }

        if (loc.isPassable() && loc.getTrapType().equals(TrapType.NONE) && !loc.isSpawnZone()){
            return 0; // nothing
        } else if(loc.isWall()){
            return 1; // wall
        } else if (loc.isWater()){
            return 2; // water
        } else if (spawnZone.equals( homeTeam )){
            return 3; // home spawn
        } else if (loc.getTrapType().equals(TrapType.WATER)){
            return 4; // water trap
        } else if (loc.getTrapType().equals(TrapType.EXPLOSIVE)){
            return 5; // Explosive! trap
        } else if (loc.getTrapType().equals(TrapType.STUN)){
            return 6; // stun trap
        } else if ((spawnZone.equals( homeTeam.opponent() ))){
            return 7; // opponent spawn
        } else {
            return 0;
        }
    }

    public  MapInfo deserializeType(MapLocation loc, int ser, Team hometeam){
        switch(ser){
            case 0: // nothing
                return new MapInfo(loc,true ,false, false ,0,false,0,TrapType.NONE, Team.NEUTRAL);
            case 1: // wall
                return new MapInfo(loc,false,true , false ,0,false,0,TrapType.NONE, Team.NEUTRAL);
            case 2: // water
                return new MapInfo(loc,false,false, false ,0,true,0,TrapType.NONE, Team.NEUTRAL);
            case 3: // home spawn
                return new MapInfo(loc,true ,false, false ,hometeam.equals(Team.A)?1:2,false,0,TrapType.NONE, Team.NEUTRAL);
            case 4:
                return new MapInfo(loc,true ,false,false ,0,false,0,TrapType.WATER, Team.NEUTRAL);
            case 5:
                return new MapInfo(loc,true ,false,false ,0,false,0,TrapType.EXPLOSIVE, Team.NEUTRAL);
            case 6:
                return new MapInfo(loc,true ,false,false ,0,false,0,TrapType.STUN, Team.NEUTRAL);
            case 7:
                return new MapInfo(loc,true ,false,false ,hometeam.equals(Team.A)?2:1,false,0,TrapType.NONE, Team.NEUTRAL);
            default:
                return new MapInfo(loc,true ,false,false ,0,false,0,TrapType.NONE, Team.NEUTRAL);

        }
    }



    //###############################################################################################
    //############# Communications Code
    //###############################################################################################
    /** We track this because WRITE operations are expensive, and it would be best to write this ONCE at the end*/
    int index ;
    
    /**How many messages you need to write this turn*/
    int writeCount = 0 ;
    /**How many messages were in the partition when you got there*/
    int writeOffset = 0;
    int[] arrayBuffer = new int[16];
    Queue<Integer> backLog  = new LinkedList<>();

    /**
     * We use a buffer because we only want to update the index a single time at the end of the turn.
     * @param value
     * @throws GameActionException
     */
    public void writeToArrayBuffer(int value) throws GameActionException {
        if(writeCount < PRE_200_PARTITION_SIZE) {
            arrayBuffer[writeCount] = value;
            writeCount++;
        } else {
            backLog.offer(value);
        }
    }

    public void flushArrayBuffer() throws GameActionException {
        // put some stuff on from the backlog.
        while( !backLog.isEmpty() && writeCount < PRE_200_PARTITION_SIZE ){
            writeToArrayBuffer( backLog.poll() );
        }
        for (int i = 0 ; i<writeCount ; i++){
            if(isEvenTurn()){ // Write to first partition on Even turns (read from 2nd partition)
                sharedArray.writeSharedArray(i+ INDEX_HEADER + writeOffset, arrayBuffer[i]);
            } else { // Write to 2nd partition on Odd turns (read from first partition)
                sharedArray.writeSharedArray(i+ INDEX_HEADER + writeOffset + PRE_200_PARTITION_SIZE, arrayBuffer[i]);
            }
        }
        sharedArray.writeSharedArray(INDEX_ADDR , updateIndex(index,writeCount + writeOffset));
    }

    /**
     * Convenience function
     * @param mapInfo
     * @throws ForgotToInitMapSize
     * @throws GameActionException
     */
    public void writeToArrayBuffer(MapInfo mapInfo) throws ForgotToInitMapSize, GameActionException {
        int serMapInfo = serializeMapInfo(mapInfo,homeTeam);
        writeToArrayBuffer(serMapInfo);
    }

    public boolean canWrite(){
        return writeCount < arrayBuffer.length;
    }

    /**
     * Let's be real, before turn 200, this is ALL we care about.
     * So let's make a convenience function
     * @return
     * @throws GameActionException
     * @throws ForgotToInitMapSize
     */
    public List<MapInfo> readAllAsMapInfo() throws GameActionException, ForgotToInitMapSize {
        int [] raw = readAll();
        List<MapInfo> mapInfos = new LinkedList<>();
        for(int i = 0 ; i<raw.length ; i++){
            mapInfos.add( deserializeMapInfo( raw[i] , homeTeam));
        }
        return mapInfos;
    }


    public int[] readAll() throws GameActionException {
        int count = isEvenTurn()? readOddBlock(index) : readEvenBlock(index)  ;
        int[] data = new int[count];
        for(int i =0 ; i<count ; i ++){
            if(isEvenTurn()){
                data[i] = sharedArray.readSharedArray(i+PRE_200_PARTITION_SIZE+INDEX_HEADER);
            } else {
                data[i] = sharedArray.readSharedArray(i+INDEX_HEADER);
            }
        }
        return data;
    }

    public void preTurnWarmup(){
        // the first 2 blocks of memory is an index.
        getLastModified(turnCount);
        // IF LAST_MODIFIED hash isn't right
        // congratulations, you're the first person to update this thing... Do the same thing as above.
        if( isSharedArrayUnconfigured() ){
            configureSharedArray();
            writeCount = 0 ;
            writeOffset = 0;
        }
        // You are the first person to see the shared array this turn...So clear the write index.
        else if( !lastModified.isCurrent(turnCount.get())){
            lastModified = LastModified.createBinary( turnCount.get() );
            try {
                // Setup Turn Count
                sharedArray.writeSharedArray(LAST_MODIFIED_ADDR, lastModified.rawBits);
                index = sharedArray.readSharedArray(INDEX_ADDR);
                // zero out the WRITE index.
                if(isEvenTurn()){
                    index = clearEvenBlock( index );
                } else {
                    index = clearOddBlock( index );
                }
                sharedArray.writeSharedArray(INDEX_ADDR, index);
            } catch (GameActionException e) {
                System.err.println(e); // Not really sure what to do if this fails, because I'm not sure how it would
            }
            writeOffset = getWriteIndex(index);
            writeCount = 0;
        } else { // The index is good! Just read it.
            try {
                index = sharedArray.readSharedArray(INDEX_ADDR);
                writeOffset = getWriteIndex(index);
                writeCount = 0;
            } catch (GameActionException e) {
                System.err.println(e); // again...no idea what to do here.
            }
        }
    }

    private void configureSharedArray() {
        lastModified = LastModified.createBinary( turnCount.get() );
        writeCount = 0;
        index = 0;
        try {
            // Setup Turn Count
            sharedArray.writeSharedArray(0, lastModified.rawBits);
            // zero out BOTH indexes
            sharedArray.writeSharedArray(1, 0);
        } catch (GameActionException e) {
            System.err.println(e); // Not really sure what to do if this fails, because I'm not sure how it would
        }
    }

    private boolean isSharedArrayUnconfigured() {
        return !lastModified.isValid();
    }

    static class LastModified{
        final int hash;
        final Integer last_mod_turn_count;

        final int rawBits;
        final boolean isValid;

        private LastModified(int bitsOrTurn , boolean isReadingBinaryBits) {
            if(isReadingBinaryBits) {
                rawBits = bitsOrTurn;
                // this first bits are the turn
                last_mod_turn_count = bitsOrTurn & TURN_COUNT_BIT_MASK;
                // the next bits are the hash. Make sure we're not reading garbage
                hash = (bitsOrTurn >> BITS_TO_REPRESENT_MAX_TURN_VALUE);
                isValid = last_mod_turn_count.hashCode() == hash;
            } else {
                last_mod_turn_count = bitsOrTurn;
                hash = last_mod_turn_count.hashCode();
                if (bitsOrTurn > GameConstants.GAME_MAX_NUMBER_OF_ROUNDS){
                    bitsOrTurn = GameConstants.GAME_MAX_NUMBER_OF_ROUNDS;
                }
                rawBits = (hash << BITS_TO_REPRESENT_MAX_TURN_VALUE) | (bitsOrTurn & TURN_COUNT_BIT_MASK);
                isValid = true; // Of course it's valid. You wrote it and you are always right.
            }
        }
        //###############################################
        //########### Factory Methods
        //###############################################
        static LastModified createBinary(int count){
            return new LastModified(count,false);
        }

        static LastModified readBinary(int rawBits){
            return new LastModified(rawBits,true);
        }

        boolean isValid(){
            return isValid;
        }

        boolean isCurrent(int currentTurn){
            return last_mod_turn_count == currentTurn;
        }

    }
    
    LastModified lastModified;
    LastModified getLastModified(TurnCount count){
        try {
            if ( hasLastModBeenCalcAlready(count) ){
                return lastModified;
            }
            lastModified = LastModified.readBinary( sharedArray.readSharedArray(0) ); // don't recalculate this if it can be helped.
            return lastModified;
        } catch (GameActionException e) {
            //I'll be damned if this throws an exception
            return lastModified;
        }
    }

    private boolean hasLastModBeenCalcAlready(TurnCount count) {
        return lastModified != null && lastModified.isCurrent(count.get());
    }


    /**
     * returns the last readable block for whichever partition of messages is in WRITE mode.
     * PRE-200 QUEUE
     * Evens Turns
     *  32-64
     * Odd Turns
     *  2-32
     * POST-200 QUEUE *TBD
     * EVEN
     *  17-31
     * Odd
     *  2-16
     */
    int getWriteIndex(int index){
        if(isEvenTurn()){ // The blocks are reversed
            return readEvenBlock(index);
        } else {
            return readOddBlock(index);
        }
    }

    int readEvenBlock(int index){
        return index & 0b0000_0000_1111_1111; //First 8 bits
    }
    int readOddBlock(int index){
        return (index >> INDEX_PARTITION_BITS) & 0b0000_0000_1111_1111; //Read last 8 bits
    }

    int clearEvenBlock(int index){
        return index & 0b1111_1111_0000_0000; //clear first 8 bits
    }
    int clearOddBlock(int index){
        return (index) & 0b0000_0000_1111_1111; //clear last 8 bits
    }

    int updateIndex(int fullIndex , int update){
        return isEvenTurn() ? updateEvenIndex(fullIndex,update)
                :updateOddIndex(fullIndex,update);
    }

    int updateEvenIndex(int fullIndex , int update){
        int cleared = clearEvenBlock(fullIndex);
        return cleared | update;
    }

    int updateOddIndex(int fullIndex , int update){
        int cleared = clearOddBlock(fullIndex);
        return (cleared | (update<<INDEX_PARTITION_BITS));
    }

    private boolean isEvenTurn() {
        return turnCount.get() % 2 == 0;
    }


}
