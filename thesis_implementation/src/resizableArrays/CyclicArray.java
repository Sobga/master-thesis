package resizableArrays;

import memory.MemoryLookup;
import memory.WordCountable;
import utils.Utils;

public class CyclicArray<T> implements WordCountable {
    private final T[] items;
    final int mask;
    private int start;
    private int n;
    private int lastPattern;

    // Assumes length is a power of two
    public CyclicArray(int length){
        items = Utils.createTypedArray(length);
        mask = length - 1;
        lastPattern = start - 1 + items.length;
    }

    public int length(){
        return n;
    }

    public final void set(int i, T a){
        assert i >= 0;
        items[(i + start) & mask] = a;
    }

    public T get(int i){
        assert i >= 0;
        return items[(i + start) & mask];
    }

    public void append(T a){
        assert n < items.length;
        items[(start + n) & mask] = a;
        n++;
    }

    public T removeFirst(){
        assert n > 0;
        T ret = items[start];
        items[start] = null;
        start = (start+1) & mask;
        lastPattern = start - 1 + items.length;
        n--;
        return ret;
    }

    public T removeLast(){
        assert n > 0;
        int idx = (n+lastPattern) & mask;
        T ret = items[idx];
        items[idx] = null;
        n--;
        return ret;
    }

    public T last(){
        return items[(n+lastPattern) & mask];
    }

    public void clear(){
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
