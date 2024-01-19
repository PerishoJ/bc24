package tx.util;

public interface ByteCodeLimiterIF {

    void tick() throws OutOfTimeException;

    void resetClock();

    void setByteCodeLimit(int limit);

    /**
     * Not the same as bytecode, but the number of times the tick() func has been called.
     * @return number of ticks in a turn.
     */
    int getTicks();
}
