public class ConstantLazyArray<T> extends ConstantArray<T>{
    private T[] oldItems;
    private int moveIdx;

    public ConstantLazyArray(float alpha) {
        super(alpha);
        oldItems = items;
    }

    @Override
    public T get(int i) {
        if (i < oldItems.length)
            items[i] = oldItems[i];
        return items[i];
    }

    @Override
    public void set(int i, T a){
        if (i < oldItems.length)
            oldItems[i] = a;
        items[i] = a;
    }

    @Override
    public void grow(T a){
        // Are we re-sizing?
        if (n >= items.length){
           oldItems = items;
           items = createTypedArray((int)Math.ceil((1+alpha) * items.length));
           moveIdx = 0;
        }

        moveItem();

        // Insert item
        items[n++] = a;
    }

    @Override
    public T shrink(){
        // Are we re-sizing?
        if (n <= items.length / ((1+alpha) * (1+alpha))){
            oldItems = items;
            items = createTypedArray((int) Math.ceil(items.length / (1+alpha)));
            moveIdx = 0;
        }
        n--;

        moveItem();

        // Find and remove last item
        T ret = get(n);
        set(n, null);
        return ret;
    }

    private void moveItem(){
        // Move one item over, if need be
        if (moveIdx < oldItems.length && moveIdx < items.length){
            items[moveIdx] = oldItems[moveIdx];
            moveIdx++;
        }
    }
}
