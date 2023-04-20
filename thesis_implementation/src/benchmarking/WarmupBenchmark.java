package benchmarking;

import benchmarking.measurers.Measurer;
import resizableArrays.ResizableArray;

import java.util.HashMap;
import java.util.Map;

import static utils.Utils.generateOperations;

public class WarmupBenchmark extends Benchmark{
    private final static int N_WARMUPS = 40;
    private final static int N_INTERVALS = 50;

    private final int[] intervals;

    private final ResizableArray<Integer>[] arrays;
    private final long[][] results;
    private int resultIdx = 0;

    private final Map<String, Object> fields;

    public WarmupBenchmark(ResizableArray<Integer> array){
        fields = new HashMap<>();
        arrays = new ResizableArray[N_WARMUPS];
        results = new long[N_WARMUPS][N_INTERVALS];

        intervals = new int[N_INTERVALS];
        int min =(int) 1E5;
        int max =(int) 1E7;
        int delta = (max - min)/(N_INTERVALS-1);
        for (int i = 0; i < N_INTERVALS; i++)
            intervals[i] = delta*i + min;

        fields.put("INTERVALS", intervals);
        fields.put("N_WARMUP", N_WARMUPS);


        for (int i = 0; i < N_WARMUPS; i++)
            arrays[i] = array;
    }

    @Override
    public String getName() {
        return "WarmupBenchmark";
    }

    @Override
    public ResizableArray<Integer>[] getArrays() {
        return arrays;
    }

    @Override
    public Object getRecordedData(ResizableArray<Integer> array) {
        return results[resultIdx++];
    }

    @Override
    public Map<String, Object> getJSONFields() {
        return fields;
    }

    @Override
    public String getArrayName(ResizableArray<Integer> array) {
        return array.getName() +"("+ resultIdx + ")";
    }

    @Override
    public void run() {
        TotalTimeMeasurer measurer = new TotalTimeMeasurer();
        Operation[] operations = generateOperations(intervals[intervals.length-1], false);

        for (int i = 0; i < N_WARMUPS; i++){
            arrays[0].clear();
            measurer.setArray(results[i]);
            measureIntervalOperations(arrays[0], operations, measurer, intervals);
        }
    }

    static class TotalTimeMeasurer extends Measurer {
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
        private void setArray(long[] arr){
            measurements = arr;
            idx = 0;
        }
    }
}
