package benchmarking;

import resizableArrays.ResizableArray;
import utils.Utils;

import java.util.HashMap;
import java.util.Map;

import static utils.Utils.generateOperations;

public class TotalTimeBenchmark extends Benchmark{
    private final ResizableArray<Integer>[] arrays;

    private final int N_WARMUP = 10;
    private final int[] intervals;
    private final Map<String, Object> fields;
    private final Map<ResizableArray<Integer>, long[]> results;

    public TotalTimeBenchmark(ResizableArray<Integer>[] arrays, boolean randomizeOperation){
        this.arrays = arrays;

        // Prepare meta fields
        fields = new HashMap<>();
        fields.put("RANDOM_OPERATION", randomizeOperation);

        int base =(int) 1E5;
        intervals = new int[10];
        for (int i = 0; i < intervals.length; i++)
            intervals[i] = (i+1) * base;
        fields.put("INTERVALS", intervals);
        fields.put("N_WARMUP", N_WARMUP);

        // Prepare result storage
        results = new HashMap<>();
        for (ResizableArray<Integer> array : arrays)
            results.put(array, new long[intervals.length]);

    }

    @Override
    public String getName() {
        return "TotalTimeBenchmark";
    }

    @Override
    public ResizableArray<Integer>[] getArrays() {
        return arrays;
    }

    @Override
    public Object getRecordedData(ResizableArray<Integer> array) {
        return results.get(array);
    }

    @Override
    public Map<String, Object> getJSONFields() {
        return fields;
    }

    @Override
    public void run() {
        TotalTimeMeasurer measurer = new TotalTimeMeasurer();
        Operation[] operations = generateOperations(intervals[intervals.length-1], (boolean) fields.get("RANDOM_OPERATION"));

        for (ResizableArray<Integer> array : arrays) {
            // Warmup
            for (int i = 0; i < N_WARMUP; i++) {
                for (Operation operation : operations) {
                    operation.applyOperation(array);
                    array.clear();
                }
            }

            // Store results
            measurer.setArray(results.get(array));
            measureIntervalOperations(array, operations, measurer, intervals);
            array.clear();
        }
    }

    static class TotalTimeMeasurer extends Measurer{
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
