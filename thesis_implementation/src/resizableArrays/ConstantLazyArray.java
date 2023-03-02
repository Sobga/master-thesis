package resizableArrays;

import memory.MemoryLookup;

import static utils.Utils.createTypedArray;

public class ConstantLazyArray<T> extends ConstantArray<T>{
    private T[] oldItems;
    private int oldN;
    private int moveIdx;

    public ConstantLazyArray(float alpha) {
        super(alpha);
        oldItems = items;
    }

    @Override
    public String getName() {
        return "ConstantLazyArray-" + (scale-1);
    }

    @Override
    public T get(int i) {
        if (oldItems != null && i < oldItems.length)
//            return items[i];
            items[i] = oldItems[i];
        return items[i];
    }

    @Override
    public void set(int i, T a){
        if (oldItems != null && i < oldItems.length)
            oldItems[i] = a;
        items[i] = a;
    }

    @Override
    public void grow(T a){
        // Are we re-sizing?
        if (n >= items.length){
//            assert (moveIdx == oldItems.length || moveIdx == items.length); // Have all items been moved over?
            oldItems = items;
            items = createTypedArray((int)Math.ceil(scale * items.length));
            moveIdx = 0;
            oldN = n;
        }

        moveItem();

        // Insert item
        set(n++, a);
    }

    @Override
    public T shrink(){
        // Are we re-sizing?
        if (n <= items.length / (scale*scale)){
//            assert (moveIdx == oldItems.length || moveIdx == items.length); // Have all items been moved over?
            oldItems = items;
            items = createTypedArray((int) Math.ceil(items.length / scale));
            moveIdx = 0;
            oldN = n;
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
        if (moveIdx < oldN && moveIdx < items.length){
            items[moveIdx] = oldItems[moveIdx];
            moveIdx++;
        } else {
            // All items have been moved over
            oldItems = null;
        }
    }

    @Override
    public void clear() {
        super.clear();
        oldItems = items;
        moveIdx = 0;
    }

    @Override
    public long byteCount() {
        return super.byteCount() +
                MemoryLookup.wordSize(oldItems) +
                MemoryLookup.wordSize(moveIdx) +
                MemoryLookup.wordSize(oldN);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ConstantLazyArray{");
        for (int i = 0; i < n; i++){
            if (i != 0)
                sb.append(", ");
            sb.append(get(i));
        }
        sb.append("}");
        return sb.toString();
    }
}
