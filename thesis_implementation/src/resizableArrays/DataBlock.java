package resizableArrays;

import memory.WordCountable;
import memory.MemoryLookup;

import java.util.Arrays;

import static utils.Utils.createTypedArray;

class DataBlock<T> implements WordCountable {
    final T[] items;
    private int n;

    public DataBlock( int size) {
        items = createTypedArray(size);
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

    @Override
    public long byteCount() {
        return MemoryLookup.wordSize(n) + MemoryLookup.wordSize(items);
    }
}
