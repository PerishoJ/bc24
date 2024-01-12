package tx;

import tx.thinkin.idears.BrightIdea;
import battlecode.common.*;

import java.util.Random;

/**
 * RobotPlayer is the class that describes your main robot strategy.
 * The run() method inside this class is like your main function: this is what we'll call once your robot
 * is created!
 */
public strictfp class RobotPlayer {

    /**
     * We will use this variable to count the number of turns this robot has been alive.
     * You can use static variables like this to save any information you want. Keep in mind that even though
     * these variables are static, in Battlecode they aren't actually shared between your robots.
     */
    static int turnCount = 0;

    /**
     * A random number generator.
     * We will use this RNG to make some random moves. The Random class is provided by the java.util.Random
     * import at the top of this file. Here, we *seed* the RNG with a constant number (6147); this makes sure
     * we get the same sequence of numbers every time this code is run. This is very useful for debugging!
     */
    public static Random rng ;
    public static final Direction[] directions = {
            Direction.NORTH,
            Direction.NORTHEAST,
            Direction.EAST,
            Direction.SOUTHEAST,
            Direction.SOUTH,
            Direction.SOUTHWEST,
            Direction.WEST,
            Direction.NORTHWEST,
    };
    /** Array containing all the possible movement directions. */


    /**
     * run() is the method that is called when a robot is instantiated in the Battlecode world.
     * It is like the main function for your robot. If this method returns, the robot dies!
     *
     * @param me  The RobotController object. You use it to perform actions from this robot, and to get
     *            information on its current status. Essentially your portal to interacting with the world.
     **/
    @SuppressWarnings("unused")
    public static void run(RobotController me) throws GameActionException {

        Cowboy yoursTruly = new Cowboy(me);
        rng = new Random(me.getID());
        me.setIndicatorString("Yeehaw!");
        System.out.println("Yeehaw!");
        while (true) {
            // This code runs during the entire lifespan of the robot, which is why it is in an infinite
            // loop. If we ever leave this loop and return from run(), the robot dies! At the end of the
            // loop, we call Clock.yield(), signifying that we've done everything we want to do.

            turnCount += 1;  // We have now been alive for one more turn!

            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode.
            try {
                // Make sure you spawn your robot in before you attempt to take any actions!
                // Robots not spawned in do not have vision of any tiles and cannot perform any actions.
                if (!me.isSpawned()){
                    comeOnIn(me);
                }
                else{
                    BrightIdea resolution = yoursTruly.takeStock();
                    resolution.getErDone( yoursTruly );
                    //snatchUpThemFlags(me);
                    // Move and attack randomly if no objective.
                    //bushwackThemHogs(rc, dir);
                    //updateEnemyRobots(me);
                }

            } catch (GameActionException e) {
                System.out.println("GameActionException");
                e.printStackTrace();

            } catch (Exception e) {
                System.out.println("Exception");
                e.printStackTrace();

            } finally {
                Clock.yield();
            }
        }
    }

    private static void bushwackThemHogs(RobotController rc, Direction dir) throws GameActionException {
        // Rarely attempt placing traps behind the robot.
        MapLocation prevLoc = rc.getLocation().subtract(dir);
        if (rc.canBuild(TrapType.EXPLOSIVE, prevLoc) && rng.nextInt() % 37 == 1)
            rc.build(TrapType.EXPLOSIVE, prevLoc);
        // We can also move our code into different methods or classes to better organize it!
    }

    private static void snatchUpThemFlags(RobotController rc) throws GameActionException {
        if (rc.canPickupFlag(rc.getLocation())){
            rc.pickupFlag(rc.getLocation());
            rc.setIndicatorString("Holding a flag!");
        }
        // If we are holding an enemy flag, singularly focus on moving towards
        // an ally spawn zone to capture it! We use the check roundNum >= SETUP_ROUNDS
        // to make sure setup phase has ended.
        if (rc.hasFlag() && rc.getRoundNum() >= GameConstants.SETUP_ROUNDS){
            MapLocation[] spawnLocs = rc.getAllySpawnLocations();
            MapLocation firstLoc = spawnLocs[0];
            Direction dir = rc.getLocation().directionTo(firstLoc);
            if (rc.canMove(dir)) rc.move(dir);
        }
    }

    private static void comeOnIn(RobotController rc) throws GameActionException {
        // Need to spawn efficiently.
        // Just try a bunch of places until you get something
        if (!rc.isSpawned()) {
            MapLocation[] spawnLocs = rc.getAllySpawnLocations();
            for (int i = 0; i < spawnLocs.length * 2; i++) {
                // Pick a random spawn location to attempt spawning in.
                MapLocation randomLoc = spawnLocs[rng.nextInt(spawnLocs.length)];
                if (rc.canSpawn(randomLoc)) rc.spawn(randomLoc);
            }
        }

    }


    private static boolean getRowdy(RobotController rc) throws GameActionException {
        boolean isABadGuyNearby = false;
        if(rc.getActionCooldownTurns()<GameConstants.ATTACK_COOLDOWN) {
            RobotInfo[] nearbyBots = rc.senseNearbyRobots(GameConstants.ATTACK_RADIUS_SQUARED);
            for (RobotInfo bot : nearbyBots) {
                if (bot.getTeam() != rc.getTeam()){
                    isABadGuyNearby = true;
                    if(rc.canAttack(bot.location)) {
                        rc.attack(bot.location);
                        break;
                    }
                }
            }
        }
        return isABadGuyNearby;
    }

    public static void updateEnemyRobots(RobotController rc) throws GameActionException{
        // Sensing methods can be passed in a radius of -1 to automatically 
        // use the largest possible value.
        RobotInfo[] enemyRobots = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
        if (enemyRobots.length != 0){
            rc.setIndicatorString("There are nearby enemy robots! Scary!");
            // Save an array of locations with enemy robots in them for future use.
            MapLocation[] enemyLocations = new MapLocation[enemyRobots.length];
            for (int i = 0; i < enemyRobots.length; i++){
                enemyLocations[i] = enemyRobots[i].getLocation();
            }
            // Let the rest of our team know how many enemy robots we see!
            if (rc.canWriteSharedArray(0, enemyRobots.length)){
                rc.writeSharedArray(0, enemyRobots.length);
                int numEnemies = rc.readSharedArray(0);
            }
        }
    }
}
