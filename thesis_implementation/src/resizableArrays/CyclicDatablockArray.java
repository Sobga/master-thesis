package resizableArrays;

import memory.MemoryLookup;
import memory.WordCountable;
import utils.Utils;

import java.util.Iterator;

public class CyclicDatablockArray<T> implements WordCountable {
    private int n;
    private int nBlocks;
    int capacity;               // Total capacity of structure

    private int start;          // Start offset of cyclic array

    private DataBlock<T>[] blocks;
    private DataBlock<T> lastBlock;

    private int itemMask;       // Bitmask to select items

    private int blockSize;      // Size of a block (power of two)
    private int blockExponent;  // 2^blockExponent == blockSize
    private int blockMask;


    public CyclicDatablockArray(int indexSize, int blockSize) {
        assert Utils.isPowerOfTwo(indexSize);
        assert Utils.isPowerOfTwo(blockSize);
        clear(indexSize, blockSize);
    }

    public void clear(){
        clear(1, 1);
    }

    private void clear(int indexSize, int blockSize){
        blockExponent = Utils.log2nlz(blockSize);
        this.blockSize = blockSize;
        itemMask = blockSize - 1;

        blocks = new DataBlock[indexSize];
        blockMask = blocks.length - 1;
        start = 0;
        lastBlock = new DataBlock<>(blockSize);
        blocks[start] = lastBlock;
        nBlocks = 1;
        n = 0;
        capacity = blockSize * blocks.length;
    }

    public final int length(){return n;}
    public final boolean isFull(){
        return n == capacity;
    }
    public final boolean isEmpty() {return n == 0;}

    public T get(int i) {
        DataBlock<T> block = getBlock(i >> blockExponent);
        return block.items[i & itemMask];
    }

    public void set(int i, T a){
        DataBlock<T> block = getBlock(i >> blockExponent);
        block.items[i & itemMask] = a;
    }

    private DataBlock<T> getBlock(int i){
        return blocks[(start + i) & blockMask];
    }

    private void setBlock(int i, DataBlock<T> d){
        blocks[(start + i) & blockMask] = d;
    }

    public void append(T a){
        assert n < capacity;
        if (lastBlock.isFull()){
            lastBlock = new DataBlock<>(blockSize);
            blocks[(nBlocks + start) & blockMask] = lastBlock;
            nBlocks++;
        }
        n++;
        lastBlock.append(a);
    }

    public void appendMany(T[] items, int nItems){
        assert n + items.length < capacity;
        int remainingItems = nItems;

        // Fill out first datablock
        int lastCapacity = Math.min(blockSize - lastBlock.n, remainingItems);
        System.arraycopy(items, 0, lastBlock.items, lastBlock.n, lastCapacity);
        lastBlock.n += lastCapacity;
        remainingItems -= lastCapacity;


        int stop = (int) Math.ceil((float) remainingItems / blockSize);
        int startOffset = nBlocks + start;

        for (int i = 0; i < stop; i++){
            // Find subpart of array
            int blockStart = i*blockSize + lastCapacity;
            int length = Math.min(remainingItems, blockSize);
            remainingItems-=length;
            T[] subItems = Utils.createTypedArray(blockSize);
            System.arraycopy(items, blockStart, subItems, 0, length);

            // Append new block
            blocks[(i + startOffset) & blockMask] = new DataBlock<>(subItems, length);
        }

        nBlocks += stop;
        n += items.length;
        lastBlock = getBlock(nBlocks - 1);

        /*
        for (int i = 0; i < items.length; i++)
            append(items[i]);*/
    }

    public T pop(){
        assert n > 0;
        if (lastBlock.isEmpty()){
            nBlocks--;
            lastBlock = getBlock(nBlocks - 1);
            setBlock(nBlocks, null);
        }
        n--;
        return lastBlock.pop();
    }

    public T[] removeKLast(int k){
        assert (k % blockSize == 0);
        assert Math.abs(nBlocks * blockSize - n) < blockSize;

        T[] items = Utils.createTypedArray(k);
        int movedItems = 0;     // Number of moved items
        int blockIdx = 0;       // Number of moved blocks
        while (movedItems < k){
            // Pop oldest block
            DataBlock<T> block = getBlock(blockIdx);
            setBlock(blockIdx, null);

            System.arraycopy(block.items, 0, items, blockSize * blockIdx, blockSize);
            blockIdx += 1;
            movedItems += blockSize;
        }
        nBlocks -= blockIdx;
        start = (start + blockIdx) & blockMask; // Start is shifted with the number of moved blocks
        n-=k;
        assert Math.abs(nBlocks * blockSize - n) < blockSize;
        return items;
    }

    public void rebuild(int indexSize, int newBlockSize){
        assert Utils.isPowerOfTwo(indexSize);
        assert Utils.isPowerOfTwo(newBlockSize);
        int newBlockExponent = Utils.log2nlz(newBlockSize);

        //int newBlockSize = 1 << newBlockExponent;
        int newItemMask = newBlockSize - 1;

        DataBlock<T>[] newBlocks = new DataBlock[indexSize];
        if (newBlockSize == blockSize){
            // Datablocks are kept same size - just move them over
            int i = 0;
            for (; i < nBlocks; i++)
                newBlocks[i] = getBlock(i);
            if (i == 0){
                // Cyclic array was empty when it was rebuilt
                lastBlock = new DataBlock<>(newBlockSize);
                nBlocks = 1;
                newBlocks[0] = lastBlock;
            } else
                lastBlock = newBlocks[i-1];
        } else if (n == 0){
            // Usually called when array must be shrunk
            lastBlock = new DataBlock<>(newBlockSize);
            nBlocks = 1;
            newBlocks[0] = lastBlock;
        } else {
            // Must combine/separate blocks -- assume combine for now
            int i = 0;
            assert (newBlockSize == 2*blockSize);
            for (; i < Math.max(nBlocks / 2, 1); i++){
                T[] items = Utils.createTypedArray(newBlockSize);
                int fillLevel = 0;
                // Copy and deallocate first block
                DataBlock<T> firstBlock = getBlock(2*i);
                System.arraycopy(firstBlock.items, 0, items, 0, blockSize);
                fillLevel += firstBlock.size();
                setBlock(2*i, null);


                // Copy and deallocate second block
                if (2*i+1 < blockSize) {
                    DataBlock<T> secondBlock = getBlock(2*i + 1);
                    System.arraycopy(secondBlock.items, 0, items, blockSize, blockSize);
                    fillLevel += secondBlock.size();
                    setBlock(2*i + 1, null);
                }
                DataBlock<T> block = new DataBlock<>(items, fillLevel);
                newBlocks[i] = block;
            }
            lastBlock = newBlocks[i - 1];
            nBlocks = i;
        }

        // Move values over
        blockSize = newBlockSize;
        blockExponent = newBlockExponent;
        itemMask = newItemMask;
        blocks = newBlocks;
        blockMask = blocks.length - 1;
        start = 0;
        capacity = blockSize * blocks.length;
    }


    public DataBlock<T> dequeueDatablock(){
        DataBlock<T> ret = blocks[start];
        blocks[start] = null;
        start = (start+1) & blockMask;
        nBlocks--;
        n-=ret.n;
        return ret;
    }

    @Override
    public long wordCount() {
        long size = constantWordCount() ;
        if (n == 0)
            return size + blocks.length;

        if (n > 0 && MemoryLookup.isPrimitive(get(0)))
            return size + nBlocks * MemoryLookup.wordSize(blocks[start]) + blocks.length;
        return size + MemoryLookup.wordSize(blocks);
    }

    private long constantWordCount(){
        return  MemoryLookup.wordSize(n) +
                MemoryLookup.wordSize(nBlocks) +
                MemoryLookup.wordSize(itemMask) +
                MemoryLookup.wordSize(start) +
                MemoryLookup.wordSize(blockExponent) +
                1; // For lastBlock
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("CDA{n=").append(n).append(",[");
        for (int i = 0; i < n; i++){
            if (i != 0)
                sb.append(", ");
            sb.append(get(i));
        }
        sb.append("]}");
        return sb.toString();
    }
}
