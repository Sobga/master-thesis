package benchmarking;

import resizableArrays.ResizableArray;
import utils.Utils;

import java.util.HashMap;
import java.util.Map;

public class TimeBoxPlotBenchmark extends Benchmark{
    private final ResizableArray<Integer>[] arrays;

    private final int N_WARMUP = 10;
    private final int N_ATTEMPTS = 30;
    private final int N = (int) 1E7;
    private final Map<ResizableArray<Integer>, long[]> results;

    public TimeBoxPlotBenchmark(ResizableArray<Integer>[] arrays, boolean randomizeOperation){
        super();
        this.arrays = arrays;
        // Prepare meta fields
        addField("RANDOM_OPERATION", randomizeOperation);
        addField("N_WARMUP", N_WARMUP);
        addField("N_ATTEMPTS", N_ATTEMPTS);
        addField("N", N);

        // Prepare result storage
        results = new HashMap<>();
        for (ResizableArray<Integer> array : arrays) {
            long[] arr = new long[N_ATTEMPTS];
            results.put(array, arr);
        }
    }

    @Override
    public String getName() {
        return "TimeBoxPlotBenchmark";
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
    public void run() {
        for (ResizableArray<Integer> array : arrays) {
            System.gc();
            long[] outputArr = results.get(array);

            // Warmup
            for (int i = 0; i < N_WARMUP; i++) {
                for (int j = 0; j < N; j++)
                    array.grow(j);
                array.clear();
            }

            // Run benchmark
            for (int i = 0; i < N_ATTEMPTS; i++){
                // Run test
                long tStart = System.nanoTime();
                for (int j = 0; j < N; j++)
                    array.grow(j);
                outputArr[i] = System.nanoTime() - tStart;
                // Cleanup
                array.clear();
            }
        }
    }

}
