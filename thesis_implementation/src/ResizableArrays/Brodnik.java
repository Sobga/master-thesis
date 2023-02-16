package ResizableArrays;

class DataBlock<T>{
    final T[] items;
    private int n;
    public DataBlock(ResizableArray<T> parent, int size){
        items = parent.createTypedArray(size);
    }

    public void append(T a){
        items[n++] = a;
    }

    public T pop(){
        return items[--n];
    }

    public boolean isFull(){
        return n == items.length;
    }

    public boolean isEmpty(){
        return n == 0;
    }

    public int size(){
        return items.length;
    }
}

public class Brodnik<T> implements ResizableArray<T>{
    ResizableArray<DataBlock<T>> blocks;
    int n;

    public Brodnik(){
        blocks = new ConstantLazyArray<>(1);
        blocks.grow(new DataBlock<>(this,1));
    }

    @Override
    public int length() {
        return n;
    }

    @Override
    public T get(int i) {
        int ell = (int) Math.ceil((Math.sqrt(8 * (i+1) + 1) - 1) / 2.0f);
        int idx = (i+1) - ell*(ell - 1) / 2;

        return blocks.get(ell - 1).items[idx - 1];
    }

    @Override
    public void set(int i, T a) {
        int ell = (int) Math.ceil((Math.sqrt(8 * (i+1) + 1) - 1) / 2.0f);
        int idx = (i+1) - ell*(ell - 1) / 2;

        blocks.get(ell - 1).items[idx - 1] = a;
    }

    @Override
    public void grow(T a) {
        n++;

        // Find last-block
        DataBlock<T> lastBlock = blocks.last();

        if (lastBlock.isFull()){
            // If last block is full, create new and add the item there
            DataBlock<T> newBlock = new DataBlock<T>(this, lastBlock.size() + 1);
            blocks.grow(newBlock);

            newBlock.append(a);
        } else
            lastBlock.append(a);
    }

    @Override
    public T shrink() {
        n--;

        // Find last-block
        DataBlock<T> lastBlock = blocks.last();

        if (lastBlock.isEmpty()){
            blocks.shrink(); // Remove empty block
            lastBlock = blocks.last();
        }
        return lastBlock.pop();
    }
}
