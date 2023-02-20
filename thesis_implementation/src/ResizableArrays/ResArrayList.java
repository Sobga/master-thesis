package ResizableArrays;

import java.util.ArrayList;

public class ResArrayList<T> implements ResizableArray<T>{
    private final ArrayList<T> items;
    public ResArrayList(){
        items = new ArrayList<>();

    }

    @Override
    public int length() {
        return items.size();
    }

    @Override
    public T get(int i) {
        return items.get(i);
    }


    @Override
    public void set(int i, T a) {
        items.set(i, a);
    }

    @Override
    public void grow(T a) {
        items.add(a);
    }

    @Override
    public T shrink() {
        return items.remove(items.size() - 1);
    }

    @Override
    public String toString() {
        return "ResArrayList{" + items + '}';
    }
}
