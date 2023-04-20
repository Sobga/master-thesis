package benchmarking;

import resizableArrays.ResizableArray;

import java.util.HashMap;
import java.util.Map;

public class RebuildMemoryBenchmark extends Benchmark {
    private final ResizableArray<Integer>[] arrays;
    private final Map<ResizableArray<Integer>, long[]> results;
    private final int N_GROWS;
    private final int START_SIZE;
    private final int N_SHRINK;

    public RebuildMemoryBenchmark(ResizableArray<Integer>[] arrays){
        this(arrays, 0, (int)1E6, 0);
    }

    public RebuildMemoryBenchmark(ResizableArray<Integer>[] arrays, int start, int grows, int shrinks){
        this.arrays = arrays;

        // Set variables
        START_SIZE = start;
        N_GROWS = grows;
        N_SHRINK = shrinks;

        // Prepare result storage
        results = new HashMap<>();

        // Export fields
        addField("START_SIZE", start);
        addField("N_GROW", grows);
        addField("N_SHRINK", shrinks);
    }

    @Override
    public String getName() {
        return "RebuildMemoryBenchmark-" + START_SIZE + "-" + N_GROWS+ "-" + N_SHRINK;
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
        int n_operations = N_GROWS + N_SHRINK;

        // Compute baseline size
        int[] size = new int[n_operations];
        addField("ACTUAL_SIZE", size);

        int n = START_SIZE;
        int j = 0;
        for (; j < N_GROWS; j++){
            n += 1;
            size[j] = n;
        }
        for (; j < N_SHRINK; j++){
            n -= 1;
            size[j] = n;
        }

        for (ResizableArray<Integer> array: arrays){
            System.out.println(" - " + getArrayName(array));
            long[] spaceUsed = new long[n_operations];
            results.put(array, spaceUsed);
            for (int i = 0; i < START_SIZE; i++)
                array.grow(i);

            j = 0;
            for (; j < N_GROWS; j++){
                spaceUsed[j] = array.countedGrow(j);
            }
            for (; j < N_SHRINK; j++){
                spaceUsed[j] = array.countedShrink();
            }

            array.clear();
        }
    }
}

