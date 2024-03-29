package tx.map.old;

import java.util.Objects;

/**
 * Just a utility class for a graph Node.
 * Generic for reusibility because...it's a graph node. Kinda basic and abstract and proven generic.
 * @param <T>
 */

@Deprecated
public class PathNode <T>{

    int score = Integer.MAX_VALUE; // left kinda generic. Override the getters and settings to give it meaning.
    public PathNode<T> parent;
    T base;
    public PathNode<T> next;

    public T getBase(){
        return base;
    }

    public boolean isScored(){
        return score != Integer.MAX_VALUE;
    }
    /**
     * This is for initing nodes that haven't been scored yet.
     * @param parent
     */
     public PathNode(PathNode<T> parent , T base) {
        this.parent = parent;
        this.base = base;
    }

    /**
     *
     * @return
     */
    public static <T> PathNode<T> root(T base){
        return new PathNode<T>(base);
    }


    /**
     * Make this private so that IT'S VERY obvious that this specific node should be the ROOT node of the Path tree.
     * Use {@link #root(T base)} static method instead.
     */
    private PathNode(T base){
         next = null;
         parent = null;
         score = 0;
         this.base = base;
    }


    /**
     * @param next the next node in the graph
     * @return next node
     */
    public PathNode<T> setNext(PathNode<T> next){
         this.next = next;
         next.parent = this;
         return next;
    }

    /**
     * creates a next PathNode with {@param base} as the wrapped value and 'this' as the parent node.
     * @param base
     * @return
     */
    public PathNode<T> setNext(T base){
        next = new PathNode<T>(this, base); // some unchecked type ... check ain't worth the time. IGNORE THIS
        return next;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PathNode<?> pathNode = (PathNode<?>) o;
        return Objects.equals(base, pathNode.base);
    }

    @Override
    public int hashCode() {
        return Objects.hash(base);
    }
}
