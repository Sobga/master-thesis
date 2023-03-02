package benchmarking;

import resizableArrays.ResizableArray;
import utils.Utils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static utils.Utils.generateOperations;

public class TotalTimeBenchmark extends Benchmark{
    private final ResizableArray<Integer>[] arrays;

    private final int N_TRIALS = 20;
    private final int[] testSizes;
    private final Map<String, Object> fields;
    private final Map<ResizableArray<Integer>, long[]> results;

    public TotalTimeBenchmark(ResizableArray<Integer>[] arrays, boolean randomizeOperation){
        this.arrays = arrays;

        // Prepare meta fields
        fields = new HashMap<>();
        fields.put("RANDOM_OPERATION", randomizeOperation);

        int base =(int) 1E5;
        testSizes = new int[10];
        for (int i = 0; i < testSizes.length; i++)
            testSizes[i] = (i+1) * base;
        fields.put("SIZES", testSizes);


        // Prepare result storage
        results = new HashMap<>();
        for (ResizableArray<Integer> array : arrays)
            results.put(array, new long[testSizes.length]);

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
        Operation[] allOperations = generateOperations(testSizes[testSizes.length-1], (boolean) fields.get("RANDOM_OPERATION"));

        long[] times = new long[N_TRIALS];
        for (int j = 0, testSizesLength = testSizes.length; j < testSizesLength; j++) {
            int problemSize = testSizes[j];
            Operation[] operations = Arrays.copyOf(allOperations, problemSize);
            for (ResizableArray<Integer> array : arrays) {
                // Warmup
                for (Operation operation : operations)
                    operation.applyOperation(array);
                array.clear();

                // Repeat experiment
                for (int i = 0; i < N_TRIALS; i++){
                    times[i] = (long) measureOverallOperations(array, operations, measurer);
                    array.clear();
                }

                // Store results
                results.get(array)[j] = Utils.slowMedian(times);
            }
        }
    }

    static class TotalTimeMeasurer extends Measurer{
        private long tStart;

        @Override
        public void start() {
            tStart = System.nanoTime();
        }

        @Override
        public Object end() {
            long tEnd = System.nanoTime();
            return tEnd - tStart;
        }

        @Override
        public void store(ResizableArray<Integer> array, Object result) {}
    }
}
