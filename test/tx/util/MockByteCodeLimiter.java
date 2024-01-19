package tx.util;

public class MockByteCodeLimiter implements ByteCodeLimiterIF{
    public int ticks;
    @Override
    public void tick() throws OutOfTimeException {
        ticks ++;
    }

    @Override
    public void resetClock() {
        ticks = 0;
    }

    @Override
    public void setByteCodeLimit(int limit) {
        //pass
    }

}
