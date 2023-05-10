package resizableArrays;

public class ResArrayList<T> implements ResizableArray<T>{
    private final ArrayListExtended<T> items;
    public ResArrayList(){
        items = new ArrayListExtended<>();

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
    public long countedGrow(T a) {
        items.add(a);
        return -1;
    }

    @Override
    public final T shrink() {
        return items.remove(items.size() - 1);
    }

    @Override
    public long countedShrink() {
        shrink();
        return -1;
    }

    @Override
    public final void clear() {
        items.clear();
        items.trimToSize();
    }

    @Override
    public final long wordCount() {
        return items.wordCount();
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
