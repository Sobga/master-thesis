package resizableArrays;

import java.util.ArrayList;

public class ResArrayList<T> implements ResizableArray<T>{
    private final ArrayList<T> items;
    public ResArrayList(){
        items = new ArrayList<>();

    }

    @Override
    public final int length() {
        return items.size();
    }

    @Override
    public final T get(int i) {
        return items.get(i);
    }


    @Override
    public final void set(int i, T a) {
        items.set(i, a);
    }

    @Override
    public final void grow(T a) {
        items.add(a);
    }

    @Override
    public final T shrink() {
        return items.remove(items.size() - 1);
    }

    @Override
    public final void clear() {
        items.clear();
        items.trimToSize();
    }

    @Override
    public final long byteCount() {
        return -1;
    }

    @Override
    public final String getName() {
        return "ArrayList";
    }

    @Override
    public final String toString() {
        return "ResArrayList{" + items + '}';
    }
}
