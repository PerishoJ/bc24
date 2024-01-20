package tx.util;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ByteCodeMonitorIF {
    void startClock(String clock);
    void stopClock(String clock);
    int getClockDelta(String clock);
    Set<Map.Entry<String,Integer>> getClocks();
    void putMetric(String metric, Integer value);
    int getMetric(String metric);
    Set<Map.Entry<String,Integer>> getMetrics();
}
