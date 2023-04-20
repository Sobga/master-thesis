package resizableArrays;

import memory.MemoryLookup;
import memory.WordCountable;
import utils.Utils;

public class CyclicArray<T> implements WordCountable {
    private final T[] items;
    private final int mask;
    private int start;
    private int n;
    private int lastPattern;

    // Assumes length is a power of two
    public CyclicArray(int length){
        items = Utils.createTypedArray(length);
        mask = length - 1;
        lastPattern = start - 1 + items.length;
    }

    public final int length(){
        return n;
    }

    public final void set(int i, T a){
        assert i >= 0;
        items[(i + start) & mask] = a;
    }

    public final T get(int i){
        assert i >= 0;
        return items[(i + start) & mask];
    }

    public final void append(T a){
        assert n < items.length;
        items[(start + n) & mask] = a;
        n++;
    }

    public final T removeFirst(){
        assert n > 0;
        T ret = items[start];
        items[start] = null;
        start = (start+1) & mask;
        lastPattern = start - 1 + items.length;
        n--;
        return ret;
    }

    public final T removeLast(){
        assert n > 0;
        int idx = (n+lastPattern) & mask;
        T ret = items[idx];
        items[idx] = null;
        n--;
        return ret;
    }

    public final T last(){
        return items[(n+lastPattern) & mask];
    }

    public final void clear(){
        for (int i = 0; i < n; i++)
            removeFirst();
        start = 0;
    }


    @Override
    public long wordCount() {
        return  MemoryLookup.wordSize(mask) +
                MemoryLookup.wordSize(start) +
                MemoryLookup.wordSize(n) +
                MemoryLookup.wordSize(items);
    }
}
