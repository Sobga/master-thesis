package benchmarking;

import resizableArrays.ResizableArray;
import utils.Utils;

import java.util.HashMap;
import java.util.Map;

public class TotalTimeBenchmark extends Benchmark{
    private final ResizableArray<Integer>[] arrays;

    private final int N_WARMUP = 5;
    private final int N_ATTEMPTS = 45;
    private final int N_MEASUREMENTS = 128;
    private final int[] intervals;
    private final Map<ResizableArray<Integer>, long[]> results;

    public TotalTimeBenchmark(ResizableArray<Integer>[] arrays, boolean randomizeOperation){
        super();
        this.arrays = arrays;
        // Prepare meta fields
        addField("RANDOM_OPERATION", randomizeOperation);


        int min =(int) 1E5;
        int max =(int) 1E7;
        // When are we measuring queries?
        intervals = new int[N_MEASUREMENTS];
        int delta = (max - min)/(N_MEASUREMENTS -1);
        for (int i = 0; i < N_MEASUREMENTS; i++)
            intervals[i] = delta*i + min;

        addField("INTERVALS", intervals);
        addField("N_WARMUP", N_WARMUP);
        addField("N_ATTEMPTS", N_ATTEMPTS);

        // Prepare result storage
        results = new HashMap<>();
        for (ResizableArray<Integer> array : arrays) {
            long[] arr = new long[N_MEASUREMENTS];
            arr[N_MEASUREMENTS - 1] = Long.MAX_VALUE;
            results.put(array, arr);
        }
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
    public void run() {
        long[][] outputArr = new long[N_ATTEMPTS][N_MEASUREMENTS];
        long end = intervals[intervals.length-1];
        for (ResizableArray<Integer> array : arrays) {
            System.gc();

            // Warmup
            for (int i = 0; i < N_WARMUP; i++) {
                for (int j = 0; j < end; j++)
                    array.grow(j);
                array.clear();
            }

            // Run benchmark
            for (int i = 0; i < N_ATTEMPTS; i++){
                long[] outputLoc = outputArr[i];
                int measureProgress = 0;

                // Run test
                long tStart = System.nanoTime();
                for (int j = 0; j <= end; j++) {
                    array.grow(j);
                    if (intervals[measureProgress] <= j)
                        outputLoc[measureProgress++] = System.nanoTime() - tStart;
                }
                // Add final result
                //outputLoc[measureProgress] = System.nanoTime() - tStart;

                // Cleanup
                array.clear();
            }

            // Store results - keep best run
            long[] lastResults = new long[N_ATTEMPTS];
            for (int i = 0; i < N_ATTEMPTS; i++)
                lastResults[i] = outputArr[i][N_MEASUREMENTS - 1];
            int idx = Utils.slowMedianIndex(lastResults);

            assert Utils.slowMedian(lastResults) - lastResults[idx] == 0;

            long[] resultArr = results.get(array);
            System.arraycopy(outputArr[idx], 0, resultArr, 0, N_MEASUREMENTS);
        }
    }

}
