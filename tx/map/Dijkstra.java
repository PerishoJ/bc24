package tx.map;

import tx.util.ByteCodeLimiter;
import tx.util.OutOfTimeException;

import java.util.*;

public abstract class Dijkstra<T> {

    private final PriorityQueue<PathNode<T>> nodesToCheck = new PriorityQueue<>();
    private final Map<T,PathNode<T>> checkedNodes = new HashMap<>();
    T goal;
    T start;
    PathNode<T> current; // Keep track of this
    private final ByteCodeLimiter limiter = new ByteCodeLimiter();
    abstract protected boolean isGoal(PathNode<T> node);

    abstract protected int scoreNode(PathNode<T> parent, T node);

    boolean wasCalcInterrupted = false;

    public void setByteCodeLimit(int limit){
        limiter.setByteCodeLimit(limit);
    }

    /**
     * REM to stitch this path back up the OTHER way so that we can efficiently nav FWD,
     * instead of just backtracking from goal.
     *
     * @param start the location we are
     * @param goal where we are going
     * @return a node with linked list going back to start. List is most efficient found path.
     * @throws OutOfTimeException Calculation time is limited, so we throw an exception if we are out of it.
     */
    public PathNode<T> findPath(T start , T goal ) throws OutOfTimeException {
        limiter.resetClock();
        detectRestarts(start, goal);
        findPath();
        return current;
    }

    /**
     * this uses pa
     * - {@link #current}
     * - {@link #start}
     * - {@link #goal}
     * @throws OutOfTimeException
     */
    private void findPath() throws OutOfTimeException {
        try {
            while (!isGoal(current)) {
                expandNeighbors(current);
                current = nodesToCheck.poll();
                if (isUnsolvable(current)) break;
                limiter.tick();
            }
        } catch (OutOfTimeException e) {
            wasCalcInterrupted = true;
            throw e;
        }
    }

    /**
     * Even though the main loop automatically checks for resumed operations, this is
     * marginally cheaper if you know that an operation has a good chance of timing out...
     * ...we'll see if this ever gets used. Kinda doubt it.
     * @return
     * @throws OutOfTimeException
     */
    public PathNode<T> resume() throws OutOfTimeException {
        findPath();
        return current;
    }
    public boolean wasCalcInterrupted(){
        return wasCalcInterrupted;
    }

    private void detectRestarts(T start, T goal) {
        if(!isThisARestart(start, goal)){
            restartProcess(start, goal);
        }
    }

    private void restartProcess(T start, T goal) {
        this.goal = goal;
        this.start = start;
        current = PathNode.root(start);
        nodesToCheck.clear();
        checkedNodes.clear();
    }

    private boolean isThisARestart(T start, T goal) {
        return start == this.start && this.goal == goal;
    }

    private void expandNeighbors(PathNode<T> pathNode) {
        List<T> neighbors = pathNode.getNeighbors();
        for(T neighbor : neighbors){
            int score = scoreNode(pathNode, neighbor);
            if(checkedNodes.containsKey(neighbor)){
                PathNode<T> neighborNode = checkedNodes.get(neighbor);
                if(score < neighborNode.score){
                    neighborNode.parent = pathNode;
                    neighborNode.score = score;
                }
            } else {
                PathNode<T> neighborNode = new PathNode<T>(pathNode,neighbor);
                neighborNode.score = score;
                nodesToCheck.add(neighborNode);
                checkedNodes.put(neighbor,neighborNode);
            }
        }
    }

    private static <T> boolean isUnsolvable(PathNode<T> current) {
        return current == null;
    }




}
