import java.util.Arrays;

public class ConstantArray<T> implements ResizableArray<T>{
    T[] items;
    int n = 0;
    final float alpha;

    public ConstantArray(float alpha){
        this.alpha = alpha;
        items = createTypedArray(1);
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
            items = Arrays.copyOf(items, (int) Math.ceil((1+alpha) * items.length));
        items[n++] = a;
    }

    @Override
    public T shrink() {
        // Array is too empty
        if (n <= items.length / ((1+alpha) * (1+alpha)))
            items = Arrays.copyOf(items, (int) Math.ceil(items.length / (1+alpha)));

        // Find and delete last item
        T ret = items[n-1];
        items[n-1] = null;
        n--;

        return ret;
    }

    @Override
    public String toString() {
        return "ConstantArray{" +
                "n=" + n +
                ", items=" + Arrays.toString(items) +
                '}';
    }
}
