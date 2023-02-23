package Benchmarking;

import ResizableArrays.ResizableArray;

import java.util.Map;

public abstract class Benchmark {
    public abstract String getName();
    public abstract ResizableArray<Integer>[] getArrays(); // Recorded data for each datastructure
    public abstract Object getRecordedData(ResizableArray<Integer> array); // Recorded data for each datastructure
    public abstract Map<String, Object> getJSONFields(); // Auxiliary fields
    public abstract void run();
}

class Operation{
    OperationType op;
    int idx;
    int data;

    public Operation(OperationType op, int idx, int data){
        this.op = op;
        this.idx = idx;
        this.data = data;
    }
}