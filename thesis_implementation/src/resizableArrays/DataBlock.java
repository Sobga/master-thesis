package resizableArrays;

import memory.WordCountable;
import memory.MemoryLookup;

import java.util.Arrays;

import static utils.Utils.createTypedArray;

class DataBlock<T> implements WordCountable {
    final T[] items;
    int n;

    public DataBlock( int size) {
        items = createTypedArray(size);
    }
    public DataBlock(T[] items, int fillLevel){
        assert fillLevel >= 0 && fillLevel <= items.length;
        this.items = items;
        n = fillLevel;
    }

    public final void append(T a) {
        items[n++] = a;
    }

    public final T pop() {
        assert n > 0;
        n--;
        T ret = items[n];
        items[n] = null;
        return ret;
    }

    public final boolean isFull() {
        return n == items.length;
    }

    public final boolean isEmpty() {
        assert n >= 0;
        return n == 0;
    }

    public final int size() {
        return n;
    }

    @Override
    public String toString() {
        return Arrays.toString(items);
    }

    @Override
    public final long wordCount() {
        return items.length + 1;
    }
}