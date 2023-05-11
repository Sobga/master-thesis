package resizableArrays;

import memory.MemoryLookup;
import utils.Utils;

import java.util.Arrays;

public class Tarjan<T> implements ResizableArray<T>{
    private DataBlock<T>[] largeBlocks;
    private CyclicDatablockArray<T> smallBlocks;
    private int largeIdx;
    private int itemsInLargeBlocks;
    private int n;
    private int b;
    private int bSQ;    // b^2
    private int bCB;    // b^3
    private int bExp;   // 2^bExp = b
    private int bSQExp; // 2^bSQExp = bSQ


    public Tarjan(){
        clear();
    }

    @Override
    public void clear() {
        n = 0;
        b = 2;
        bSQ = b*b;
        bCB = bSQ * b;
        bExp = 1;
        bSQExp = 2;
        largeIdx = 0;
        itemsInLargeBlocks = 0;
        smallBlocks = new CyclicDatablockArray<>(2*b,b);
        largeBlocks = new DataBlock[1];
    }

    @Override
    public long wordCount() {
        return  constantWordCount() +
                MemoryLookup.wordSize(largeBlocks) +
                MemoryLookup.wordSize(smallBlocks);
    }

    private long constantWordCount() {
        return  MemoryLookup.wordSize(n) +
                MemoryLookup.wordSize(b) +
                MemoryLookup.wordSize(largeIdx);
    }

    @Override
    public int length() {
        return n;
    }

    @Override
    public T get(int i) {
        // Is the index inside the large blocks?
        if (i < itemsInLargeBlocks){
            DataBlock<T> block = largeBlocks[i >> bSQExp];
            int idx = i & (bSQ - 1); // i mod bSQ
            return block.items[idx];
        } else {
            i -= itemsInLargeBlocks;
            return smallBlocks.get(i);
        }
    }

    @Override
    public void set(int i, T a) {
        // Is the index inside the large blocks?
        if (i < itemsInLargeBlocks){
            DataBlock<T> block = largeBlocks[i >> bSQExp];
            int idx = i & (bSQ - 1); // i mod bSQ
            block.items[idx] = a;
        } else {
            i -= itemsInLargeBlocks;
            smallBlocks.set(i, a);
        }
    }

    /*
    private T locate(int idx, T a){
        // Is the index inside the large blocks?
        if (idx < itemsInLargeBlocks)
            return getOrSet(largeBlocks[idx / bSQ], idx % bSQ, a);

        idx -= itemsInLargeBlocks;

        return getOrSet(smallBlocks.get(idx / b), idx % b, a);
    }

    private T getOrSet(DataBlock<T> dataBlock, int idx, T a){
        if (a == null)
            return dataBlock.items[idx];
        dataBlock.items[idx] = a;
        return a;
    }*/

    @Override
    public void grow(T a) {
        // Is the datastructure too full?
        if (n >= bCB)
            rebuildGrow();

        if (smallBlocks.isFull()){
            // TODO: Save one block to be used as the next free one
            // Store items in large block
            T[] items = smallBlocks.removeKLast(bSQ);
            largeBlocks[largeIdx++] = new DataBlock<>(items, bSQ);
            itemsInLargeBlocks += bSQ;
        }

        smallBlocks.append(a);
        n++;
    }

    private void rebuildGrow(){
        int newB = b << 1;
        int newBSQ = newB * newB;

        DataBlock<T>[] newLargeBlocks = new DataBlock[newB];

        // Since large blocks now contain 4x the amount of items, we need 4 large blocks from before
        int i = 0;
        for (int stop = largeIdx >> 2; i < stop; i++){
            T[] items = Utils.createTypedArray(newBSQ);
            for (int j = 0; j < 4; j++){
                int blockIdx = 4*i + j;
                System.arraycopy(largeBlocks[blockIdx].items, 0, items, bSQ*j, bSQ);
                largeBlocks[blockIdx] = null;
            }
            newLargeBlocks[i] = new DataBlock<>(items, newBSQ);
        }

        // New Structure for small blocks
        CyclicDatablockArray<T> newSmallBlocks = new CyclicDatablockArray<>(2*newB, newB);

        // Remaining large blocks (at most 3) must be moved to small blocks
        for (int j = 4*i; j < largeIdx; j++){
            DataBlock<T> oldBlock = largeBlocks[j];
            newSmallBlocks.appendMany(oldBlock.items, oldBlock.n);
            largeBlocks[j] = null;
        }

        // Add remaining small blocks
        while (!smallBlocks.isEmpty()){
            DataBlock<T> block = smallBlocks.dequeueDatablock();
            newSmallBlocks.appendMany(block.items, block.n);
        }

        // Finalize values being moved
        b = newB;
        bSQ = b*b;
        bCB = bSQ * b;
        bExp += 1;
        bSQExp += 2;
        largeBlocks = newLargeBlocks;
        smallBlocks = newSmallBlocks;
        largeIdx = i;
        itemsInLargeBlocks = largeIdx * bSQ;
    }

    @Override
    public T shrink() {
        if (n <= bCB/64)
            rebuildShrink();

        // Are all small blocks empty?
        if  (smallBlocks.isEmpty()){
            // Take a large block and turn it into small blocks
            largeIdx--;
            itemsInLargeBlocks -= bSQ;
            DataBlock<T> oldLarge = largeBlocks[largeIdx];
            largeBlocks[largeIdx] = null;

            smallBlocks.appendMany(oldLarge.items, bSQ);
        }
        n--;
        return smallBlocks.pop();
    }

    private void rebuildShrink(){
        int newB = b >> 1;
        int newBSQ = newB * newB;
        DataBlock<T>[] newLargeBlocks = allocateDatablocks(newB);
        int newLargeIdx = 0;

        for (int i = 0; i < largeIdx; i++){
            // Split old large block into 4 "smaller" large blocks
            DataBlock<T> oldBlock = largeBlocks[i];
            for (int j = 0; j < 4; j++){
                T[] items = Arrays.copyOfRange(oldBlock.items, newBSQ*j, newBSQ*(j+1));
                newLargeBlocks[newLargeIdx++] = new DataBlock<>(items, newBSQ);
            }
            // De-allocate old large block
            largeBlocks[i] = null;
        }

        largeIdx = newLargeIdx;
        largeBlocks = newLargeBlocks;

        // Move some of the old small blocks to be a single large block
        if (n - largeIdx * newBSQ > newBSQ) {
            T[] items = Utils.createTypedArray(newBSQ);
            for (int i = 0, stop = b / 4; i < stop; i++) {
                DataBlock<T> oldBlock = smallBlocks.dequeueDatablock();
                System.arraycopy(oldBlock.items, 0, items, i * b, b);
            }
            largeBlocks[largeIdx++] = new DataBlock<>(items, newBSQ);
        }

        CyclicDatablockArray<T> newSmallBlocks = new CyclicDatablockArray<>(2*newB, newB);
        // Split all small blocks into new
        while (!smallBlocks.isEmpty()){
            DataBlock<T> oldBlock = smallBlocks.dequeueDatablock();
            newSmallBlocks.appendMany(oldBlock.items, oldBlock.n);
        }

        smallBlocks = newSmallBlocks;
        b = newB;
        bSQ = b*b;
        bCB = bSQ * b;
        bExp = Utils.log2nlz(b);
        bSQExp = 2*bExp;
        itemsInLargeBlocks = bSQ * largeIdx;
    }

    @Override
    public long countedGrow(T a) {
        long size = 0;
        if (n >= bCB)
            size = countedRebuildGrow();

        if (smallBlocks.isFull()){
            // TODO: Save one block to be used as the next free one
            // Store items in large block
            T[] items = smallBlocks.removeKLast(bSQ);
            largeBlocks[largeIdx++] = new DataBlock<>(items, bSQ);
            itemsInLargeBlocks += bSQ;

            size = Math.max(wordCount() + (long) items.length, size);
        }

        smallBlocks.append(a);
        n++;
        if (size == 0)
            return wordCount();
        return size;
    }

    private long countedRebuildGrow(){
        int newB = b << 1;
        int newBSQ = newB * newB;

        DataBlock<T>[] newLargeBlocks = new DataBlock[newB];

        // Since large blocks now contain 4x the amount of items, we need 4 large blocks from before
        int i = 0;
        for (int stop = largeIdx >> 2; i < stop; i++){
            T[] items = Utils.createTypedArray(newBSQ);
            for (int j = 0; j < 4; j++){
                int blockIdx = 4*i + j;
                System.arraycopy(largeBlocks[blockIdx].items, 0, items, bSQ*j, bSQ);
                largeBlocks[blockIdx] = null;
            }
            newLargeBlocks[i] = new DataBlock<>(items, newBSQ);
        }

        // New Structure for small blocks
        CyclicDatablockArray<T> newSmallBlocks = new CyclicDatablockArray<>(2*newB, newB);

        // Remaining large blocks (at most 3) must be moved to small blocks
        for (int j = 4*i; j < largeIdx; j++){
            DataBlock<T> oldBlock = largeBlocks[j];
            newSmallBlocks.appendMany(oldBlock.items, oldBlock.n);
            largeBlocks[j] = null;
        }

        // Add remaining small blocks
        while (!smallBlocks.isEmpty()){
            DataBlock<T> block = smallBlocks.dequeueDatablock();
            newSmallBlocks.appendMany(block.items, block.n);
        }

        long size = constantWordCount() +
                MemoryLookup.wordSize(largeBlocks) +
                MemoryLookup.wordSize(newLargeBlocks)+
                MemoryLookup.wordSize(smallBlocks) +
                MemoryLookup.wordSize(newSmallBlocks) +
                newBSQ; // Overhead of arrays whilst moving values

        // Finalize values being moved
        b = newB;
        bSQ = b*b;
        bCB = bSQ * b;
        bExp += 1;
        bSQExp += 2;
        largeBlocks = newLargeBlocks;
        smallBlocks = newSmallBlocks;
        largeIdx = i;
        itemsInLargeBlocks = largeIdx * bSQ;

        return size;
    }

    @Override
    public long countedShrink() {
        long size = 0;
        if (n <= bCB/64)
            size = countedRebuildShrink();

        // Are all small blocks empty?
        if  (smallBlocks.isEmpty()){
            // Take a large block and turn it into small blocks
            largeIdx--;
            itemsInLargeBlocks -= bSQ;
            DataBlock<T> oldLarge = largeBlocks[largeIdx];
            largeBlocks[largeIdx] = null;

            smallBlocks.appendMany(oldLarge.items, bSQ);

            size = Math.max(size, wordCount() + oldLarge.items.length);
        }
        n--;
        smallBlocks.pop();

        if (size == 0)
            return wordCount();
        return size;
    }

    private long countedRebuildShrink(){
        long size = 0;

        int newB = b >> 1;
        int newBSQ = newB * newB;
        DataBlock<T>[] newLargeBlocks = allocateDatablocks(newB);
        int newLargeIdx = 0;

        for (int i = 0; i < largeIdx; i++){
            // Split old large block into 4 "smaller" large blocks
            DataBlock<T> oldBlock = largeBlocks[i];
            for (int j = 0; j < 4; j++){
                T[] items = Arrays.copyOfRange(oldBlock.items, newBSQ*j, newBSQ*(j+1));
                newLargeBlocks[newLargeIdx++] = new DataBlock<>(items, newBSQ);
            }
            // De-allocate old large block
            largeBlocks[i] = null;
        }

        size =  MemoryLookup.wordSize(largeBlocks) +
                MemoryLookup.wordSize(newLargeBlocks) +
                MemoryLookup.wordSize(smallBlocks) +
                bSQ; // Overhead in copying

        largeIdx = newLargeIdx;
        largeBlocks = newLargeBlocks;

        // Move some of the old small blocks to be a single large block
        if (n - largeIdx * newBSQ > newBSQ) {
            T[] items = Utils.createTypedArray(newBSQ);
            for (int i = 0, stop = b / 4; i < stop; i++) {
                DataBlock<T> oldBlock = smallBlocks.dequeueDatablock();
                System.arraycopy(oldBlock.items, 0, items, i * b, b);
            }
            largeBlocks[largeIdx++] = new DataBlock<>(items, newBSQ);
        }

        CyclicDatablockArray<T> newSmallBlocks = new CyclicDatablockArray<>(2*newB, newB);
        // Split all small blocks into new
        while (!smallBlocks.isEmpty()){
            DataBlock<T> oldBlock = smallBlocks.dequeueDatablock();
            newSmallBlocks.appendMany(oldBlock.items, oldBlock.n);
        }

        size = Math.max(size,
                MemoryLookup.wordSize(largeBlocks) +
                MemoryLookup.wordSize(smallBlocks) +
                MemoryLookup.wordSize(newSmallBlocks) +
                b); // Size overhead when moving down a size


        smallBlocks = newSmallBlocks;
        b = newB;
        bSQ = b*b;
        bCB = bSQ * b;
        bExp = Utils.log2nlz(b);
        bSQExp = 2*bExp;
        itemsInLargeBlocks = bSQ * largeIdx;

        return size;
    }

    @Override
    public String getName() {
        return "Tarjan";
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Tarjan{[");

        for (int i = 0; i < n; i++){
            if (i != 0)
                sb.append(", ");
            sb.append(get(i));
        }
        /*
        // Add large blocks
        for (int i = 0; i < largeIdx; i++){
            DataBlock<T> block = largeBlocks[i];
            for (int j = 0; j < b*b; j++){
                if (!(i == 0 && j == 0))
                    sb.append(", ");
                sb.append(block.items[j]);
            }
        }

        // Add small blocks
        for (int i = 0, stop_i = smallBlocks.length(); i < stop_i; i++){
            DataBlock<T> block = smallBlocks.get(i);
            for (int j = 0, stop_j = block.size(); j < stop_j; j++){
                if (!(i == 0 && j == 0))
                    sb.append(", ");
                sb.append(block.items[j]);
            }
        }*/

        sb.append("]}");
        return sb.toString();
    }
}
