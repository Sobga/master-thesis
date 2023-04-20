package benchmarking;

import benchmarking.measurers.Measurer;
import resizableArrays.ResizableArray;

import java.util.HashMap;
import java.util.Map;

public abstract class Benchmark {
    private Map<String, Object> fields;

    public Benchmark(){
        fields = new HashMap<>();
    }
    void addField(String name, Object object){ fields.put(name, object);}
    Object getField(String name){return fields.get(name);}

    public abstract String getName();
    public abstract ResizableArray<Integer>[] getArrays(); // Recorded data for each datastructure
    public abstract Object getRecordedData(ResizableArray<Integer> array); // Recorded data for each datastructure
    public Map<String, Object> getJSONFields(){return fields;}; // Auxiliary fields
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

    public void measureIntervalOperations(ResizableArray<Integer> array, Operation[] operations, Measurer measurer, int[] intervals){
        int intervalProgress = 0;
        measurer.start();
        for (int i = 0; i < operations.length; i++){
            operations[i].applyOperation(array);
            if (i >= intervals[intervalProgress]){
                measurer.store(array, null);
                intervalProgress++;
            }
        }
        measurer.end();
    }
}



