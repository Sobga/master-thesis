package resizableArrays;

import memory.MemoryLookup;
import memory.WordCountable;
import utils.Utils;

public class CyclicArray<T> implements WordCountable {
    T[] items;
    int mask;
    int start;
    int n;

    // Assumes length is a power of two
    public CyclicArray(int length){
        items = Utils.createTypedArray(length);
        mask = length - 1;
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
        items[(start + n) & mask] = a;
        n++;
    }

    public final T removeFirst(){
        T ret = items[start];
        items[start] = null;
        start = (start+1) & mask;
        n--;
        return ret;
    }

    public final T removeLast(){
        int idx = (start + n - 1 + items.length) & mask;
        T ret = items[idx];
        items[idx] = null;
        n--;
        return ret;
    }

    public final T last(){
        return items[(start + n - 1 + items.length) & mask];
    }

    public final void clear(){
        for (int i = 0; i < n; i++)
            removeFirst();
        start = 0;
    }


    @Override
    public long byteCount() {
        return  MemoryLookup.wordSize(mask) +
                MemoryLookup.wordSize(start) +
                MemoryLookup.wordSize(n) +
                MemoryLookup.wordSize(items);
    }
}
