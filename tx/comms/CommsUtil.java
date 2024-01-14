package tx.comms;

import battlecode.common.*;

import java.util.LinkedList;
import java.util.List;

public class CommsUtil {

    /** This is hardcoded!! YAY! :D */
    public static final int BITS_TO_REPRESENT_MAX_TURN_VALUE = 11;

    /**essentially a 5 bit mask...uses {@link #BITS_TO_REPRESENT_MAX_TURN_VALUE} to account for when teh devs change things, we can change quickly*/
    public static final int HASH_MASK = (1 << (16 - BITS_TO_REPRESENT_MAX_TURN_VALUE)) - 1; 
    public static final int TURN_COUNT_BIT_MASK = 1<<BITS_TO_REPRESENT_MAX_TURN_VALUE -1;
    public static final int NUMBER_OF_EVEN_BLOCKS = 30;
    public static final int NUMBER_OF_ODD_BLOCKS = 29;
    public static final int TURN_COUNT_OFFSET = 1;
    public static final int INDEX_OFFSET = 5;
    private final int mapHeight;

    private final SharedArrayWrapper sharedArray;

    private final TurnCount turnCount;

    
    public CommsUtil(int mapHeight, SharedArrayWrapper sharedArray, TurnCount turnCount){
        this.mapHeight = mapHeight;
        this.sharedArray =sharedArray;
        this.turnCount = turnCount;
    }

    public CommsUtil(int mapHeight , RobotController rc , TurnCount turnCount){
        this.mapHeight = mapHeight;
        sharedArray = new RobotSharedArray(rc);
        this.turnCount = turnCount;

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

    public void setupIndex(){

        // the first block of memory is an index...because we aren't stupid and that's what we use.
        // doing the evens/odds memory split.  Read/write to half the buffer on each turn.
        // First block is LAST_MODIFIED tells the last turn that the memory was updated, and gives a hash of that.

            // IF LAST_MODIFIED was the last turn
                    // -> Update LAST_MODIFIED and HASH
                    // -> Clear index of the active Buffer half
            // IF LAST_MODIFIED hash isn't right
                    // congratulations, you're the first person to update this thing... Do the same thing as above.
    }

    
    //###############################################################################################
    //############# Communications Code
    //###############################################################################################

    static class LastModified{
        final int hash;
        final Integer last_mod_turn_count;

        final int rawBits;
        final boolean isValid;

        private LastModified(int bitsOrTurn , boolean isReading) {
            if(isReading) {
                rawBits = bitsOrTurn;
                // this first bits are the turn
                last_mod_turn_count = bitsOrTurn & TURN_COUNT_BIT_MASK;
                // the next bits are the hash. Make sure we're not reading garbage
                hash = (bitsOrTurn >> BITS_TO_REPRESENT_MAX_TURN_VALUE) & HASH_MASK;
                isValid = last_mod_turn_count.hashCode() == hash;
            } else {
                last_mod_turn_count = bitsOrTurn;
                hash = last_mod_turn_count.hashCode();
                if (bitsOrTurn > GameConstants.GAME_MAX_NUMBER_OF_ROUNDS){
                    bitsOrTurn = GameConstants.GAME_MAX_NUMBER_OF_ROUNDS;
                }
                rawBits = (bitsOrTurn << BITS_TO_REPRESENT_MAX_TURN_VALUE) | (bitsOrTurn & TURN_COUNT_BIT_MASK);
                isValid = true; // Of course it's valid. You wrote it and you are always right.
            }
        }
        //###############################################
        //########### Factory Methods
        //###############################################
        public static LastModified createBinary(int count){
            return new LastModified(count,false);
        }

        public static LastModified readBinary(int rawBits){
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

    public int[] getIndexBlock(){
        int[] indices = new int[4];
        try {
            for(int i = 0; i < 4 ; i++){
                indices[i] = sharedArray.readSharedArray(i + TURN_COUNT_OFFSET);
            }
        } catch (GameActionException e) {
            System.err.println(e); // I really don't know what to do with this one
        }
        return indices;
    }

    /**
     * Use a split queue system
     * first byte for turn count and hash
     * next 4 bytes for index
     * next 59 blocks are split
     * Half the array on even turns (30)
     * Half the array on odd turns (29)
     * 
     * @return
     */
    public List getReadable(int[] indices){
        // bytes 1,2 for blocks (64-5(index blocks)) = 30 respectively
        // Check for values at index by bit-shifting until the index is all zeroes
        // Values come in order
        // So
        // 0b0000_0000_0000_0011 (1)
        // >>
        // 0b0000_0000_0000_001 (2)
        // >>
        // 0b0000_0000_0000_00 (X)
        // So the first 2 blocks have values.
        List<Integer> validBlocks = new LinkedList<>();
        if(isEvenTurn()){
            // use blocks 1 and 2 for index
            for(int i = 0; i< NUMBER_OF_EVEN_BLOCKS; i++){
                boolean hasData = checkEvenIndexForData(i, indices);
                if(!hasData){
                    break;
                } else {
                    validBlocks.add(i+ INDEX_OFFSET);
                }
            }
        } else {
            // use blocks 3 and 4 for index
            for(int i = 0; i< NUMBER_OF_ODD_BLOCKS; i++){
                boolean hasData = checkOddIndexForData(i, indices);
                if(!hasData){
                    break;
                } else {
                    validBlocks.add(i+ INDEX_OFFSET + NUMBER_OF_EVEN_BLOCKS);
                }
            }
        }
        return validBlocks;
    }

    /** Use blocks 3 & 4 as index for EVENS memory blocks*/
    private boolean checkEvenIndexForData(int i, int[] indices) {
        boolean hasData;
        if(i <16){
            hasData = indices[0] > 0;
            indices[0] = indices[0]>>1;

        } else {
            hasData = indices[1] > 0;
            indices[0] = indices[0]>>1;
        }
        return hasData;
    }

    /** Use blocks 3 & 4 as index for ODDS memory blocks*/
    private boolean checkOddIndexForData(int i, int[] indices) {
        boolean hasData;
        if(i <16){
            hasData = indices[2] > 0;
            indices[0] = indices[0]>>1;

        } else {
            hasData = indices[3] > 0;
            indices[0] = indices[0]>>1;
        }
        return hasData;
    }

    private boolean isEvenTurn() {
        return turnCount.get() % 2 == 1;
    }

    public int[] getWriteable(int[] indices){
        return new int[]{};
    }

}
