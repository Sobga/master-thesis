package benchmarking.measurers;

import resizableArrays.ResizableArray;

public class MemoryMeasurer extends Measurer {
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
        resultStorage[i++] = array.wordCount();
    }
}