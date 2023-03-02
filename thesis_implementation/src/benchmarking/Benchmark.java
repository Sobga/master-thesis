package benchmarking;

import resizableArrays.ResizableArray;

import java.util.Map;

public abstract class Benchmark {
    public abstract String getName();
    public abstract ResizableArray<Integer>[] getArrays(); // Recorded data for each datastructure
    public abstract Object getRecordedData(ResizableArray<Integer> array); // Recorded data for each datastructure
    public abstract Map<String, Object> getJSONFields(); // Auxiliary fields
    public abstract void run();


    public String getArrayName(ResizableArray<Integer> array){
        return array.getName();
    }

    public void measureIndividualOperations(ResizableArray<Integer> array, Operation[] operations, Measurer measurer){
        for (Operation operation : operations) {
            Object output = null;
            switch (operation.op) {
                case GET -> {
                    measurer.start();
                    array.get(operation.idx);
                    output = measurer.end();
                }
                case SET -> {
                    measurer.start();
                    array.set(operation.idx, operation.data);
                    output = measurer.end();
                }
                case GROW -> {
                    measurer.start();
                    array.grow(operation.data);
                    output = measurer.end();
                }
                case SHRINK -> {
                    measurer.start();
                    array.shrink();
                    output = measurer.end();
                }
            }
            measurer.store(array, output);
        }
    }
    public Object measureOverallOperations(ResizableArray<Integer> array, Operation[] operations, Measurer measurer){
        measurer.start();
        for (Operation operation : operations)
            operation.applyOperation(array);
        return measurer.end();
    }
}

abstract class Measurer {
    public abstract void start();
    public abstract Object end();
    public abstract void store(ResizableArray<Integer> array, Object result);
}



