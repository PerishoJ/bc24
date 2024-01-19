package tx.util;

public interface ByteCodeLimiterIF {

    void tick() throws OutOfTimeException;

    void resetClock();

    void setByteCodeLimit(int limit);
}
