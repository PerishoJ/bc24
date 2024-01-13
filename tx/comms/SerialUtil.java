package tx.comms;

import battlecode.common.*;

import static java.lang.Math.floor;

public class SerialUtil {

    private final int mapHeight;

    public SerialUtil(int mapHeight){
        this.mapHeight = mapHeight;
    }
    public  class ForgotToInitMapSize extends Exception{}

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

    /**
     * The first 9 value of the shared array are the index, which shows which values are valid and which are not
     * Setup a consistent time between bots. Use hash code to validate.
     *  1 - game clock [ 11b - value] [5b - hash]
     *  [evens turn #][][][]
     *  [odds turn #][][][]
     *
     *  if the turn # does not matchhh.....ahhh fuck.
     *
     */
    public  void updateNetworkTime(RobotController rc){
        // Is network synced?
            //Yes
                // Done network time match my time?
                    //Yes
                        //awesome
                    //No
                        // I am the first one to see the time. It needs to be updated
            //No
                //Check the time
                    // Does the time match the hash?
                        //Yes
                            // Update my time. Go along my merry way.
                            // Mark yourself as semi-updated. Wait for 10 turns before assuming yourself a "time" authority.
                                // Make array of possible times.
                                    // update each unique time, and add a score for each that "matches" the global time.
                        //No
                            // I am the first player...on no. time to update the hash.
    }
}
