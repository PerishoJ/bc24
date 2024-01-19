package tx.map;

import battlecode.common.*;
import junit.framework.TestCase;
import tx.thinkin.BigPicture;

public class TrailBlazerTest extends TestCase {
    BigPicture bigPicture = new BigPicture(30,30);
    TrailBlazer pathing = new TrailBlazer(bigPicture);

    public void test(){
        assertTrue(true);//some bullshit here
    }
    private void printPath(MapScribbles path){
        while(path != null){
            System.out.println(path.loc());
            path = path.prev;
        }
    }

    private MapInfo addWall(int x , int y){
        return new MapInfo(
                new MapLocation(x,y),
                false,
                true,
                false,
                3,
                false,
                0,
                TrapType.NONE,
                Team.A
        );
    }

    private MapInfo addWater(int x , int y){
        return new MapInfo(
                new MapLocation(10,11),
                false,
                false,
                false,
                3,
                true,
                0,
                TrapType.NONE,
                Team.A
        );
    }

    private MapInfo addAllySpawn(int x , int y){
        return new MapInfo(
                new MapLocation(10,11),
                true,
                false,
                false,
                1,
                true,
                0,
                TrapType.NONE,
                Team.A
        );
    }
}