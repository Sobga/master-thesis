package benchmarking;

import resizableArrays.ResizableArray;
import utils.Utils;

import java.util.HashMap;
import java.util.Map;

public class IndexingBenchmark extends Benchmark{
    private final int PROBLEM_SIZE = (int) 1E7;
    private final int N_WARMUP = 5;
    private final int N_ATTEMPTS = 20;
    private final int N_MEASUREMENTS = 128;
    private int[] intervals;
    Map<ResizableArray<Integer>, long[]> results;


    private final ResizableArray<Integer>[] arrays;

    public IndexingBenchmark(ResizableArray<Integer>[] arrays){
        super();
        this.arrays = arrays;
    }

    private void init(){
        addField("N_WARMUP", N_WARMUP);
        addField("N_MEASUREMENTS", N_WARMUP);
        addField("N_ATTEMPTS", N_ATTEMPTS);

        // When are we measuring queries?
        intervals = new int[N_MEASUREMENTS];
        int delta = PROBLEM_SIZE / N_MEASUREMENTS;
        for (int i = 0; i < N_MEASUREMENTS; i++)
            intervals[i] = (i+1) * delta;
        addField("INTERVALS", intervals);


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
        return "IndexingBenchmark";
    }

    @Override
    public ResizableArray<Integer>[] getArrays() {
        return arrays;
    }

    @Override
    public Object getRecordedData(ResizableArray<Integer> array) {
        return results.get(array);
    }

    public void fillToSize(ResizableArray<Integer> array, int size){
        array.clear();
        for (int i = 0; i < size; i++)
            array.grow(0);
    }

    public void indexingOperations(ResizableArray<Integer> array, int[] indices){
        for (int j = 0; j < indices.length; j++)
            array.set(indices[j], j);
    }
    @Override
    public void run() {
        init();

        //TotalTimeMeasurer measurer = new TotalTimeMeasurer();
        int[] indices = Utils.indexingPermutation(PROBLEM_SIZE);
        long[][] outputArr = new long[N_ATTEMPTS][N_MEASUREMENTS];


        for (ResizableArray<Integer> array : arrays) {
            System.gc();

            // Fill array to size
            fillToSize(array, PROBLEM_SIZE);

            // Warmup on permutation
            for (int i = 0; i < N_WARMUP; i++)
                indexingOperations(array, indices);

            for (int i = 0; i < N_ATTEMPTS; i++){
                long[] outputLoc = outputArr[i];
                int measureProgress = 0;
                // Run test
                long tStart = System.nanoTime();
                for (int j = 0; j < PROBLEM_SIZE; j++) {
                    array.set(indices[j], i);
                    if (intervals[measureProgress] <= j)
                        outputLoc[measureProgress++] = System.nanoTime() - tStart;
                }
                // Add final result
                outputLoc[measureProgress] = System.nanoTime() - tStart;
            }

            // Cleanup
            array.clear();

            // Store results - keep best run
            long[] lastResults = new long[N_ATTEMPTS];
            for (int i = 0; i < N_ATTEMPTS; i++)
                lastResults[i] = outputArr[i][N_MEASUREMENTS - 1 ];
            int idx = Utils.slowMedianIndex(lastResults);
            long[] resultArr = results.get(array);
            System.arraycopy(outputArr[idx], 0, resultArr, 0, N_MEASUREMENTS);
        }
    }
}
