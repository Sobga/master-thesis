package benchmarking;

import benchmarking.measurers.Measurer;
import resizableArrays.ResizableArray;
import utils.Utils;

import java.util.HashMap;
import java.util.Map;

import static utils.Utils.generateOperations;

public class TimeBenchmark extends Benchmark{
    private final int N_TRIALS = 10;
    private final int TEST_SIZE = (int) 1E5;

    private final ResizableArray<Integer>[] arrays;
    private final Map<ResizableArray<Integer>, long[]> results;

    public TimeBenchmark(ResizableArray<Integer>[] arrays, boolean randomOperation){
        this.arrays = arrays;

        // Prepare result storage
        results = new HashMap<>();
        for (ResizableArray<Integer> array : arrays)
            results.put(array, new long[TEST_SIZE]);

        // Register relevant fields
        addField("N_TRIALS", N_TRIALS);
        addField("RANDOM_OPERATION", randomOperation);
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
    public void run() {
        long[][] timeArr = new long[TEST_SIZE][N_TRIALS];
        TimeMeasurer measurer = new TimeMeasurer(timeArr);

        Operation[] operations = generateOperations(TEST_SIZE, (boolean) getField("RANDOM_OPERATION"));

        for (ResizableArray<Integer> array: arrays){
            // Warmup
            for (int i = 0; i < TEST_SIZE; i++)
                operations[i].applyOperation(array);
            array.clear();

            for (int i = 0; i < N_TRIALS; i++){
                measurer.setOutputRow(i);
                measureIndividualOperations(array, operations, measurer);
                array.clear();
            }

            // Rescale results
            long[] resultArr = results.get(array);
            for (int i = 0; i < TEST_SIZE; i++){
                resultArr[i] = Utils.slowMedian(timeArr[i]);
            }
        }
    }

    static class TimeMeasurer extends Measurer {
        private final long[][] timeArr;
        private long tStart;
        private int i;
        private int row;
        public TimeMeasurer(long[][] timeArr){
            this.timeArr = timeArr;
        }

        public void setOutputRow(int row){
            this.row = row;
            i = 0;
        }
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
        public void store(ResizableArray<Integer> array, Object result) {
            timeArr[i++][row] = (long) result;
        }
    }
}
