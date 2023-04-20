package resizableArrays;

import memory.MemoryLookup;

public class Brodnik<T> implements ResizableArray<T>{
    private final ResizableArray<DataBlock<T>> blocks;
    private int n;

    public Brodnik(){
        blocks = new ConstantArray<>(1);
//        blocks = new ResArrayList<>();
        blocks.grow(new DataBlock<>(1));
    }

    @Override
    public String getName() {
        return "Brodnik";
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
            lastBlock = new DataBlock<>(lastBlock.size() + 1);
            blocks.grow(lastBlock);
        }
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

    @Override
    public long countedGrow(T a) {
        long size = MemoryLookup.wordSize(n);

        n++;
        // Find last-block
        DataBlock<T> lastBlock = blocks.last();

        if (lastBlock.isFull()){
            // If last block is full, create new and add the item there
            lastBlock = new DataBlock<>(lastBlock.size() + 1);
            size += blocks.countedGrow(lastBlock);
        } else {
            size += blocks.wordCount();
        }
        lastBlock.append(a);
        return size;
    }

    @Override
    public long countedShrink() {
        long size = MemoryLookup.wordSize(n);
        n--;
        // Find last-block
        DataBlock<T> lastBlock = blocks.last();

        if (lastBlock.isEmpty()){
            size += blocks.countedShrink(); // Remove empty block
            lastBlock = blocks.last();
        } else
            size += MemoryLookup.wordSize(blocks);
        lastBlock.pop();

        return size;
    }

    @Override
    public void clear() {
        n = 0;
        blocks.clear();
        blocks.grow(new DataBlock<>(1));
    }

    @Override
    public long wordCount() {
        return MemoryLookup.wordSize(n) + blocks.wordCount();
    }
}
