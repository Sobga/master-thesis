package Benchmarking;

import ResizableArrays.ResizableArray;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class TimeBenchmark extends Benchmark{
    private final int N_TRIALS = 10;
    private final int TEST_SIZE = (int) 1E4;

    private final ResizableArray<Integer>[] arrays;
    private final Map<ResizableArray<Integer>, long[]> results;
    private final Map<String, Object> fields;

    public TimeBenchmark(ResizableArray<Integer>[] arrays, boolean randomOperation){
        this.arrays = arrays;

        // Prepare result storage
        results = new HashMap<>();
        for (ResizableArray<Integer> array : arrays)
            results.put(array, new long[TEST_SIZE]);

        // Register relevant fields
        fields = new HashMap<>();
        fields.put("N_TRIALS", N_TRIALS);
        fields.put("RANDOM_OPERATION", randomOperation);
    }

    @Override
    public String getName() {
        return "TimeBenchmark";
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
        Operation[] operations = generateOperations();
        long[][] timeArr = new long[TEST_SIZE][N_TRIALS];

        for (ResizableArray<Integer> array : arrays){
            long[] resultArr = results.get(array);

            // Warmup
            for (int i = 0; i < TEST_SIZE; i++)
                timeOperation(array, operations[i]);
            array.clear();

            for (int i = 0; i < N_TRIALS; i++){
                // Run through all the operations once
                for (int j = 0; j < TEST_SIZE; j++)
                    timeArr[j][i] = timeOperation(array, operations[j]);
                array.clear();
            }

            // Rescale results
            for (int i = 0; i < TEST_SIZE; i++){
                resultArr[i] = Utils.slowMedian(timeArr[i]);
            }
        }
    }

    // Run a single operation on the given array. Returns elapsed time in ns
    private long timeOperation(ResizableArray<Integer> array, Operation operation){
        long start = 0;
        long end = 0;

        switch (operation.op){
            case GET -> {
                start = System.nanoTime();
                array.get(operation.idx);
                end = System.nanoTime();
            }
            case SET -> {
                start = System.nanoTime();
                array.set(operation.idx, operation.data);
                end = System.nanoTime();
            }
            case GROW -> {
                start = System.nanoTime();
                array.grow(operation.data);
                end = System.nanoTime();
            }
            case SHRINK -> {
                start = System.nanoTime();
                array.shrink();
                end = System.nanoTime();
            }
        }
        return end - start;
    }

    private Operation[] generateOperations(){
        Operation[] operations = new Operation[TEST_SIZE];
        Random random = new Random();

        int n = 0;
        for (int i = 0; i < TEST_SIZE; i++){
            if (n == 0){
                operations[i] = new Operation(OperationType.GROW, i, i);
                n++;
                continue;
            }

            // Generate random operation
//            OperationType op =  ;
            OperationType op = (boolean) fields.get("RANDOM_OPERATION") ?
                    OperationType.fromInt(random.nextInt(4)) : OperationType.fromInt(2);
            int idx = random.nextInt(n);
            operations[i] = new Operation(op, idx, i);

            // Update n
            if (op == OperationType.GROW)
                n++;
            else if (op == OperationType.SHRINK)
                n--;

        }
        return operations;
    }
}
