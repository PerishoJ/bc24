package tx.map.old;

import battlecode.common.*;
import junit.framework.TestCase;
import org.junit.Assert;
import tx.thinkin.BigPicture;
import tx.util.MockByteCodeLimiter;
import tx.util.OutOfTimeException;

import java.util.List;

public class AStarPathingTest extends TestCase {
    BigPicture bigPicture = new BigPicture(30,30);
    AStarPathing pathing = new AStarPathing(bigPicture);
    public void testAStarNeighborExpansion(){
        PathNode<MapLocation> testLoc = PathNode.root(bigPicture.getLocalInfo(new MapLocation(10,10)).getMapLocation());
        List<PathNode<MapLocation>> neighbors = pathing.getNeighbors(testLoc);
        assertEquals(8,neighbors.size());
    }

    public void testAStarPathing() throws OutOfTimeException, GameActionException {
        MapLocation start = bigPicture.getLocalInfo(new MapLocation(10,10)).getMapLocation();
        MapLocation target = bigPicture.getLocalInfo(new MapLocation(10,12)).getMapLocation();

        PathNode<MapLocation> path = pathing.findPath(start,target);
        Assert.assertEquals(path.getBase() , start);

        int i = 10 ; //target y location
        do{
            Assert.assertEquals(i,path.base.y);
            path = path.next;
            assertTrue("Expected final y value to be 10, but was " + i, i>=10 && i<=12);
            i++;
        }while(path.next != null);

        assertEquals(path.getBase(),target);
    }


    public void testAStarPathingWithObstacle() throws OutOfTimeException, GameActionException {
        MapLocation start = bigPicture.getLocalInfo(new MapLocation(10,10)).getMapLocation();
        MapLocation target = bigPicture.getLocalInfo(new MapLocation(10,12)).getMapLocation();
        bigPicture.updateLocalMap( addWall( 10, 11));

        PathNode<MapLocation> path = pathing.findPath(start,target);
        Assert.assertEquals(path.getBase() , start);
        System.out.println("Checked:" + pathing.checkedNodes.size());
        int i = 10 ; //target y location
        do{
            Assert.assertEquals(i,path.base.y);
            path = path.next;
            assertTrue("Expected final y value to be 10, but was " + i, i>=10 && i<=12);
            i++;
        }while(path.next != null);
        Assert.assertEquals(path.getBase() , target);

    }

    public void testAStarPathingWithObstaclePerformanceCheck() throws OutOfTimeException, GameActionException {
        MapLocation start = bigPicture.getLocalInfo(new MapLocation(10,10)).getMapLocation();
        MapLocation target = bigPicture.getLocalInfo(new MapLocation(10,12)).getMapLocation();
        bigPicture.updateLocalMap( addWall( 10, 11));
        MockByteCodeLimiter limiter = new MockByteCodeLimiter();
        pathing.setByteCodeLimiter(limiter);
        PathNode<MapLocation> path = pathing.findPath(start,target);
        Assert.assertEquals("Performance metric failed. Calculation complexity out of bounds", 5, limiter.ticks );
    }

    public void testAStarPathingWithBiggerObstaclePerformanceCheck() throws OutOfTimeException, GameActionException {
        MapLocation start = bigPicture.getLocalInfo(new MapLocation(10,10)).getMapLocation();
        MapLocation target = bigPicture.getLocalInfo(new MapLocation(10,12)).getMapLocation();
        bigPicture.updateLocalMap( addWall( 9, 11));
        bigPicture.updateLocalMap( addWall( 10, 11));
        bigPicture.updateLocalMap( addWall( 11, 11));
        MockByteCodeLimiter limiter = new MockByteCodeLimiter();
        pathing.setByteCodeLimiter(limiter);
        PathNode<MapLocation> path = pathing.findPath(start,target);
        printPath(path);
        Assert.assertEquals("Performance metric failed. Calculation complexity out of bounds", 13, limiter.ticks );
    }

    private void printPath(PathNode path){
        while(path != null){
            System.out.println(path.base);
            path = path.parent;
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
