package benchmarking.measurers;

import resizableArrays.ResizableArray;

public class TotalTimeMeasurer extends Measurer {
    private long tStart;
    private long[] measurements;
    private int idx;

    @Override
    public void start() {
        tStart = System.nanoTime();
    }

    @Override
    public Object end() {
        measurements[idx++] = System.nanoTime() - tStart;
        return null;
    }

    @Override
    public final void store(ResizableArray<Integer> array, Object result) {
        measurements[idx++] = System.nanoTime() - tStart;
    }

    public void setArray(long[] arr) {
        measurements = arr;
        idx = 0;
    }
}
