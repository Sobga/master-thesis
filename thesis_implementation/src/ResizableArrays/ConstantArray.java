package ResizableArrays;

import java.util.Arrays;

public class ConstantArray<T> implements ResizableArray<T>{
    T[] items;
    int n = 0;
    final float scale;

    public ConstantArray(float alpha){
        this.scale = 1 + alpha;
        items = createTypedArray(1);
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
    public void grow(T a) {
        // Grow array, as it is full
        if (n >= items.length)
            items = Arrays.copyOf(items, (int) Math.ceil(scale * items.length));
        items[n++] = a;
    }

    @Override
    public T shrink() {
        // Array is too empty
        if (n <= items.length / (scale * scale))
            items = Arrays.copyOf(items, (int) Math.ceil(items.length / scale));

        // Find and delete last item
        T ret = items[n-1];
        items[n-1] = null;
        n--;

        return ret;
    }

    @Override
    public void clear() {
        items = createTypedArray(1);
        n = 0;
    }

    @Override
    public String toString() {
        return "ResizableArrays.ConstantArray{" +
                "n=" + n +
                ", items=" + Arrays.toString(items) +
                '}';
    }
}
