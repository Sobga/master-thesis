package benchmarking.measurers;

import resizableArrays.ResizableArray;

public abstract class Measurer {
    public abstract void start();

    public abstract Object end();

    public abstract void store(ResizableArray<Integer> array, Object result);
}
