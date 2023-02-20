package ResizableArrays;

import java.util.Arrays;

class DataBlock<T> {
    final T[] items;
    private int n;

    public DataBlock(ResizableArray<T> parent, int size) {
        items = parent.createTypedArray(size);
    }
    public DataBlock(T[] items, int fillLevel){
        assert fillLevel >= 0 && fillLevel <= items.length;
        this.items = items;
        n = fillLevel;
    }

    public void append(T a) {
        items[n++] = a;
    }

    public T pop() {
        n--;
        T ret = items[n];
        items[n] = null;
        return ret;
    }

    public boolean isFull() {
        return n == items.length;
    }

    public boolean isEmpty() {
        return n == 0;
    }

    public int size() {
        return n;
    }

    @Override
    public String toString() {
        return Arrays.toString(items);
    }
}
