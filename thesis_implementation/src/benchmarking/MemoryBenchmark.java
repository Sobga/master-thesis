package benchmarking;

import resizableArrays.ResizableArray;

import java.util.HashMap;
import java.util.Map;

import static utils.Utils.generateOperations;
import static utils.Utils.growShrink;

public class MemoryBenchmark extends Benchmark {
    private final ResizableArray<Integer>[] arrays;
    private final Map<ResizableArray<Integer>, long[]> results;
    private final Map<String, Object> fields;

    private final int TEST_SIZE = (int) 1E4;


    public MemoryBenchmark(ResizableArray<Integer>[] arrays){
        this.arrays = arrays;

        // Prepare result storage
        results = new HashMap<>();
        for (ResizableArray<Integer> array : arrays)
            results.put(array, new long[TEST_SIZE]);

        // Register relevant fields
        fields = new HashMap<>();
    }

    @Override
    public String getName() {
        return "MemoryBenchmark";
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
//        Operation[] operations = generateOperations(TEST_SIZE, 1, 0, 0, 1);
        int SHRINK_COUNT = 4000;
        Operation[] operations = growShrink(TEST_SIZE - SHRINK_COUNT, SHRINK_COUNT);
        MemoryMeasurer measurer = new MemoryMeasurer();
        // Compute baseline size
        int[] size = new int[TEST_SIZE];
        fields.put("ACTUAL_SIZE", size);
        int n = 0;
        for (int i = 0; i < TEST_SIZE; i++){
            n += operations[i].op == OperationType.GROW ? 1 : -1;
            size[i] = n;
        }

        for (ResizableArray<Integer> array: arrays){
            long[] spaceUsed = new long[TEST_SIZE];
            results.put(array, spaceUsed);
            measurer.setResultStorage(spaceUsed);
            measureIndividualOperations(array, operations, measurer);
        }
    }

    static class MemoryMeasurer extends Measurer{
        private long[] resultStorage;
        private int i = 0;

        public void setResultStorage(long[] array){
            resultStorage = array;
            i = 0;
        }
        @Override
        public void start() {}

        @Override
        public Object end() {
            return null;
        }

        @Override
        public void store(ResizableArray<Integer> array, Object result) {
            resultStorage[i++] = array.byteCount();
        }
    }
}
