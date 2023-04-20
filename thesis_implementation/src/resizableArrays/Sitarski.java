package resizableArrays;

import memory.MemoryLookup;
import utils.Utils;

import java.util.Arrays;

import static utils.Utils.log2nlz;

public class Sitarski<T> implements ResizableArray<T>{
    private DataBlock<T>[] indexBlock;
    private int b = 1; // Assume b is kept as a power of 2
    private int bExp = 0;
    private int n;

    public Sitarski(){
        clear();
    }

    @Override
    public void clear() {
        b = 1;
        bExp = 0;
        n = 0;
        indexBlock = allocateDatablocks(b);
    }

    @Override
    public String getName() {
        return "Sitarski";
    }

    @Override
    public int length() {
        return n;
    }

    @Override
    public T get(int i) {
        DataBlock<T> block = indexBlock[i >> bExp];
        return block.items[i & (b-1)];
    }

    @Override
    public void set(int i, T a) {
        DataBlock<T> block = indexBlock[i >> bExp];
        block.items[i & (b-1)] = a;
    }

    @Override
    public void grow(T a) {
        // Is the datastructure too full?
        if (n == b*b){
            // Rebuild
            rebuildGrow();
        }

        int blockIdx = n >> bExp;

        // We need a new block
        if (indexBlock[blockIdx] == null)
            indexBlock[blockIdx] = new DataBlock<>(b);

        // Add item
        indexBlock[blockIdx].append(a);
        n++;
    }

    private void rebuildGrow(){
        int newB = b << 1;
        DataBlock<T>[] newIndex = allocateDatablocks(newB);

        // Block size is increasing; each new block holds 2 of the old blocks
        for (int i = 0; i < Math.max(b >> 1, 1); i++){
            T[] items = Utils.createTypedArray(newB);
            int fillLevel = 0;
            // Copy and deallocate first block
            System.arraycopy(indexBlock[2*i].items, 0, items, 0, b);
            fillLevel += indexBlock[2*i].size();
            indexBlock[2*i] = null;


            // Copy and deallocate second block
            if (2*i+1 < b) {
                System.arraycopy(indexBlock[2*i + 1].items, 0, items, b, b);
                fillLevel += indexBlock[2*i+1].size();
                indexBlock[2*i + 1] = null;
            }

            DataBlock<T> block = new DataBlock<>(items, fillLevel);
            newIndex[i] = block;
        }
        b = newB;
        bExp = log2nlz(b);
        indexBlock = newIndex;
    }

    @Override
    public T shrink() {
        n--;

        if (n == b*b / 16)
            rebuildShrink();

        int blockIdx = n >> bExp;
        T ret = indexBlock[blockIdx].pop();

        if (indexBlock[blockIdx].isEmpty() && blockIdx+1 < b)
            indexBlock[blockIdx+1] = null;
        return ret;
    }

    private void rebuildShrink(){
        int newB = Math.max(b >> 1, 1);

        DataBlock<T>[] newIndex = allocateDatablocks(newB);

        for (int i = 0; i < b; i++){

            if (indexBlock[i] == null || indexBlock[i].size() == 0 || 2*i >= newB)
                break;

            T[] firstItems = Arrays.copyOf(indexBlock[i].items, newB);
            newIndex[2*i] = new DataBlock<>(firstItems, Math.min(indexBlock[i].size(), newB));


            if (2*i + 1 < newIndex.length && indexBlock[i].size() > newB){
                T[] secondItems = Arrays.copyOfRange(indexBlock[i].items, newB, b);
                newIndex[2*i + 1] = new DataBlock<>(secondItems, indexBlock[i].size() - newB);
            }
            indexBlock[i] = null;
        }
        b = newB;
        bExp = log2nlz(b);
        indexBlock = newIndex;
    }

    @Override
    public long countedGrow(T a) {
        // Is the datastructure too full?
        long size = 0;
        if (n == b*b){
            // Rebuild
            size = countedRebuildGrow();
        }

        int blockIdx = n >> bExp;

        // We need a new block
        if (indexBlock[blockIdx] == null)
            indexBlock[blockIdx] = new DataBlock<>(b);

        // Add item
        indexBlock[blockIdx].append(a);
        n++;

        if (size == 0)
            return wordCount();
        return size;
    }

    private long countedRebuildGrow(){
        int newB = b << 1;
        DataBlock<T>[] newIndex = allocateDatablocks(newB);

        // Block size is increasing; each new block holds 2 of the old blocks
        for (int i = 0; i < Math.max(b >> 1, 1); i++){
            T[] items = Utils.createTypedArray(newB);
            int fillLevel = 0;
            // Copy and deallocate first block
            System.arraycopy(indexBlock[2*i].items, 0, items, 0, b);
            fillLevel += indexBlock[2*i].size();
            indexBlock[2*i] = null;


            // Copy and deallocate second block
            if (2*i+1 < b) {
                System.arraycopy(indexBlock[2*i + 1].items, 0, items, b, b);
                fillLevel += indexBlock[2*i+1].size();
                indexBlock[2*i + 1] = null;
            }

            DataBlock<T> block = new DataBlock<>(items, fillLevel);
            newIndex[i] = block;
        }

        long size = constantWordCount() + MemoryLookup.wordSize(newIndex) + MemoryLookup.wordSize(indexBlock) + newB;
        b = newB;
        bExp = log2nlz(b);
        indexBlock = newIndex;

        return size;
    }

    @Override
    public long countedShrink() {
        long size = 0;
        n--;

        if (n == b*b / 16)
            size = countedRebuildShrink();

        int blockIdx = n >> bExp;
        indexBlock[blockIdx].pop();

        if (indexBlock[blockIdx].isEmpty() && blockIdx+1 < b)
            indexBlock[blockIdx+1] = null;

        if (size == 0)
            return wordCount();
        return size;
    }

    private long countedRebuildShrink(){
        int newB = Math.max(b >> 1, 1);

        DataBlock<T>[] newIndex = allocateDatablocks(newB);

        for (int i = 0; i < b; i++){

            if (indexBlock[i] == null || indexBlock[i].size() == 0 || 2*i >= newB)
                break;

            T[] firstItems = Arrays.copyOf(indexBlock[i].items, newB);
            newIndex[2*i] = new DataBlock<>(firstItems, Math.min(indexBlock[i].size(), newB));


            if (2*i + 1 < newIndex.length && indexBlock[i].size() > newB){
                T[] secondItems = Arrays.copyOfRange(indexBlock[i].items, newB, b);
                newIndex[2*i + 1] = new DataBlock<>(secondItems, indexBlock[i].size() - newB);
            }
            indexBlock[i] = null;
        }

        long size = constantWordCount() + MemoryLookup.wordSize(newIndex) + MemoryLookup.wordSize(indexBlock) + b;

        b = newB;
        bExp = log2nlz(b);
        indexBlock = newIndex;
        return size;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("Sitarski{");
        for (int i = 0; i < b; i++){
            if (indexBlock[i] == null)
                break;

            if (i != 0)
                sb.append(", ");
            sb.append(indexBlock[i]);
        }
        sb.append("}");
        return sb.toString();
    }


    @Override
    public long wordCount() {
        return constantWordCount() + MemoryLookup.wordSize(indexBlock);
    }

    private long constantWordCount(){
        return MemoryLookup.wordSize(n) + MemoryLookup.wordSize(b) + MemoryLookup.wordSize(bExp);
    }
}
