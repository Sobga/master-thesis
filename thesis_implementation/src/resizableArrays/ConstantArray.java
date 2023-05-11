package resizableArrays;

import memory.MemoryLookup;
import memory.WordCountable;

import java.util.Arrays;

import static utils.Utils.createTypedArray;

public class ConstantArray<T> implements ResizableArray<T>{
    T[] items;
    int n = 0;
    int shrinkSize;
    final float scale;

    public ConstantArray(float alpha){
        this.scale = 1 + alpha;
        clear();
    }

    @Override
    public void clear() {
        items = createTypedArray(1);
        shrinkSize = (int) (items.length / (scale * scale));
        n = 0;
    }

    @Override
    public String getName() {
        return "ConstantArray-" + (scale-1);
    }

    @Override
    public int length() {
        return n;
    }

    @Override
    public T get(int i) {
        return items[i];
    }

    @Override
    public void set(int i, T a) {
        items[i] = a;
    }

    @Override
    public T last(){
        return items[n-1];
    }

    @Override
    public void grow(T a) {
        // Grow array, as it is full
        if (n >= items.length) {
            items = Arrays.copyOf(items, (int) Math.ceil(scale * items.length));
            shrinkSize = (int) (items.length / (scale * scale));
        }
        items[n++] = a;
    }

    @Override
    public T shrink() {
        // Array is too empty
        if (n <= shrinkSize) {
            items = Arrays.copyOf(items, (int) Math.ceil(items.length / scale));
            shrinkSize = (int) (items.length / (scale * scale));
        }

        // Find and delete last item
        T ret = items[n-1];
        items[n-1] = null;
        n--;

        return ret;
    }

    @Override
    public long countedGrow(T a) {
        long size = wordCount();
        if (n >= items.length) {
            items = Arrays.copyOf(items, (int) Math.ceil(scale * items.length));
            shrinkSize = (int) (items.length / (scale * scale));
            size += items.length;
        }
        items[n++] = a;

        return size;
    }

    @Override
    public long countedShrink() {
        long size = wordCount();

        // Array is too empty
        if (n <= shrinkSize) {
            items = Arrays.copyOf(items, (int) Math.ceil(items.length / scale));
            shrinkSize = (int) (items.length / (scale * scale));
            size += items.length;
        }

        // Find and delete last item
        T ret = items[n-1];
        items[n-1] = null;
        n--;

        return size;
    }

    @Override
    public long wordCount() {
        long res = MemoryLookup.wordSize(n) + MemoryLookup.wordSize(shrinkSize) + items.length;
        if (n > 0 && !MemoryLookup.isPrimitive(items[0])){
            if (items[0] instanceof WordCountable)
                for (int i = 0; i < n; i++)
                    res += ((WordCountable) items[i]).wordCount();
            else
                for (int i = 0; i < n; i++)
                    res += MemoryLookup.wordSize(items[i]);
        }

        assert MemoryLookup.wordSize(n) + MemoryLookup.wordSize(items) == res;
        return res;
    }

    @Override
    public String toString() {
        return "ConstantArray{" +
                "n=" + n +
                ", items=" + Arrays.toString(items) +
                '}';
    }
}
