package tx.map.old;

import battlecode.common.GameActionException;
import tx.util.ByteCodeLimiter;
import tx.util.ByteCodeLimiterIF;
import tx.util.OutOfTimeException;

import java.util.*;

@Deprecated
public abstract class Dijkstra<T> {

    protected PriorityQueue<PathNode<T>> nodesToCheck = new PriorityQueue<>();
    protected Map<T,PathNode<T>> checkedNodes = new HashMap<>();
    T goal;
    T start;
    PathNode<T> current; // Keep track of this
    private ByteCodeLimiterIF limiter = new ByteCodeLimiter();
    abstract protected boolean isGoal(PathNode<T> node);

    /**
     * Scores the value between this node and its {@link PathNode#parent}
     * @param node
     * @return
     */
    abstract protected int scoreNode(PathNode<T> node);

    boolean wasCalcInterrupted = false;

    public void setByteCodeLimit(int limit){
        limiter.setByteCodeLimit(limit);
    }
    public void setByteCodeLimiter(ByteCodeLimiterIF limiterImpl){
        limiter = limiterImpl;
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
    public PathNode<T> findPath(T start , T goal ) throws OutOfTimeException, GameActionException {
        limiter.resetClock();
        detectRestarts(start, goal);
        findPath();
        return linkPath(current); // the returned path is backwards. Straighten it out.
    }

    /**
     * When finding a path, we only link one direction.
     * Now we link both directions.
     * @param node
     */
    private PathNode linkPath(PathNode node){
        while(node !=null && node.parent != null){
            node.parent.next = node;
            node = node.parent;
        }
        return node;
    }

    /**
     * this uses pa
     * - {@link #current}
     * - {@link #start}
     * - {@link #goal}
     * @throws OutOfTimeException
     * @throws GameActionException If you are trying to use raw PathNode as the base node. They are basically abstract.
     */
    private void findPath() throws OutOfTimeException, GameActionException {
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
    public PathNode<T> resume() throws OutOfTimeException, GameActionException {
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

    private void expandNeighbors(PathNode<T> pathNode) throws GameActionException {
        List<PathNode<T>> neighbors = getNeighbors(pathNode);
        for ( Iterator<PathNode<T>> i = neighbors.iterator(); i.hasNext() ; ){
            PathNode<T> neighbor =  i.next();
            int score = scoreNode(neighbor);
            if(checkedNodes.containsKey(neighbor.base)){
                PathNode<T> neighborNode = checkedNodes.get(neighbor.base); // We key off the BASE, not the node. Not a set.
                if(score < neighborNode.score){
                    nodesToCheck.remove(neighborNode);
                } else {
                    i.remove();
                }
            } else {
                neighbor.score = score;
                nodesToCheck.add(neighbor);
                checkedNodes.put(neighbor.base,neighbor);
            }
        }

    }

    protected abstract List<PathNode<T>> getNeighbors(PathNode<T> node);

    private static <T> boolean isUnsolvable(PathNode<T> current) {
        return current == null;
    }




}
