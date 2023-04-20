package benchmarking;

import resizableArrays.ResizableArray;
import utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;

public class IncreasingIndexingBenchmark extends IndexingBenchmark{
    private final int[] SIZES;
    private final int N_WARMUP = 15;
    private final int N_ATTEMPTS = 5;
    public IncreasingIndexingBenchmark(ResizableArray<Integer>[] arrays){
        super(arrays);
        SIZES = new int[10];
        for (int i = 0; i < SIZES.length; i++){
            SIZES[i] = (i+1)*(int) 1E6;
        }
        addField("SIZES", SIZES);
        addField("N_WARMUP", N_WARMUP);
        addField("N_ATTEMPTS", N_ATTEMPTS);

        results = new HashMap<>();
        for (ResizableArray<Integer> array : arrays) {
            long[] arr = new long[SIZES.length];
            results.put(array, arr);
        }
    }

    @Override
    public String getName() {
        return "IncreasingIndexingBenchmark";
    }

    @Override
    public void run() {
        ArrayList<int[]> permutations = new ArrayList<>();
        for (int size : SIZES)
            permutations.add(Utils.indexingPermutation(size));

        for (ResizableArray<Integer> array : getArrays()){
            System.gc();
            long[] resultArr = results.get(array);
            for (int i = 0; i < permutations.size(); i++){
                int[] permutation = permutations.get(i);
                for (int j = array.length(), stop = permutation.length; j < stop; j++)
                    array.grow(j);

                // Warmup
                for (int j = 0; j < N_WARMUP; j++)
                    indexingOperations(array, permutation);

                // Find best result
                long tBest = Long.MAX_VALUE;
                for (int j = 0; j < N_ATTEMPTS; j++){
                    long tStart = System.nanoTime();
                    indexingOperations(array, permutation);
                    long tEnd = System.nanoTime();
                    tBest = Math.min(tBest,  tEnd- tStart);
                }

                resultArr[i] = tBest;
            }
            array.clear();
        }
    }
}
