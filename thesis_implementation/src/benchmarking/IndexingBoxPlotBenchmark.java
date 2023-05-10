package benchmarking;

import resizableArrays.ResizableArray;
import utils.Utils;

import java.util.HashMap;
import java.util.Map;

public class IndexingBoxPlotBenchmark extends Benchmark{
    private final int PROBLEM_SIZE = (int) 1E7;
    private final int N_WARMUP = 10;
    private final int N_ATTEMPTS = 30;
    Map<ResizableArray<Integer>, long[]> results;


    private final ResizableArray<Integer>[] arrays;

    public IndexingBoxPlotBenchmark(ResizableArray<Integer>[] arrays){
        super();
        this.arrays = arrays;
    }

    private void init(){
        addField("N_WARMUP", N_WARMUP);
        addField("N_MEASUREMENTS", N_WARMUP);
        addField("N_ATTEMPTS", N_ATTEMPTS);
        addField("PROBLEM_SIZE", PROBLEM_SIZE);

        // Prepare result storage
        results = new HashMap<>();
        for (ResizableArray<Integer> array : arrays) {
            long[] arr = new long[N_ATTEMPTS];
            results.put(array, arr);
        }
    }

    @Override
    public String getName() {
        return "IndexingBoxPlotBenchmark";
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
        int[] indices = Utils.indexingPermutation(PROBLEM_SIZE);

        for (ResizableArray<Integer> array : arrays) {
            System.gc();
            long[] outputArr = results.get(array);

            // Fill array to size
            fillToSize(array, PROBLEM_SIZE);

            // Warmup on permutation
            for (int i = 0; i < N_WARMUP; i++)
                indexingOperations(array, indices);

            for (int i = 0; i < N_ATTEMPTS; i++){

                long tStart = System.nanoTime();
                for (int j = 0; j < PROBLEM_SIZE; j++) {
                    array.set(indices[j], i);
                }
                outputArr[i] = System.nanoTime() - tStart;
            }
            // Cleanup
            array.clear();
        }
    }
}
