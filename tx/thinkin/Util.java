package tx.thinkin;

import battlecode.common.*;

import static java.lang.StrictMath.floor;

public class Util {

    public static  short mapWidth;
    public static short serializeMapLocation ( MapLocation loc){
        return (short)((loc.x * mapWidth) + loc.y);
    }

    public static MapLocation deserializeMapLoc( short loc){
        return new MapLocation((int)floor(loc/mapWidth) , loc%mapWidth);
    }

    public static short serializeType(MapInfo loc , Team homeTeam) {
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

    public static MapInfo deserializeType(MapLocation loc, short ser, Team hometeam){
        switch(ser){
            case 0: // nothing
                return new MapInfo(loc,true,false,0,false,0,TrapType.NONE);
            case 1: // wall
                return new MapInfo(loc,false,true,0,false,0,TrapType.NONE);
            case 2: // water
                return new MapInfo(loc,false,false,0,true,0,TrapType.NONE);
            case 3: // home spawn
                return new MapInfo(loc,true,false,hometeam.equals(Team.A)?1:2,false,0,TrapType.NONE);
            case 4:
                return new MapInfo(loc,true,false,0,false,0,TrapType.WATER);
            case 5:
                return new MapInfo(loc,true,false,0,false,0,TrapType.EXPLOSIVE);
            case 6:
                return new MapInfo(loc,true,false,0,false,0,TrapType.STUN);
            case 7:
                return new MapInfo(loc,true,false,hometeam.equals(Team.A)?2:1,false,0,TrapType.NONE);
            default:
                return new MapInfo(loc,true,false,0,false,0,TrapType.NONE);

        }
    }
}
