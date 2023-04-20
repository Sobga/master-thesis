package resizableArrays;

import memory.MemoryLookup;
import utils.Utils;

import java.util.Arrays;

public class Tarjan<T> implements ResizableArray<T>{
    private DataBlock<T>[] largeBlocks;
    private CyclicArray<DataBlock<T>> smallBlocks;
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
        smallBlocks = new CyclicArray<>(2*b);
        smallBlocks.append(new DataBlock<>(b));
        largeBlocks = allocateDatablocks(b);
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
        return locate(i, null);
    }

    @Override
    public void set(int i, T a) {
        // Is the index inside the large blocks?
        int idx;
        DataBlock<T> block;
        if (i < itemsInLargeBlocks){
            block = largeBlocks[i >> bSQExp];
            idx = i & (bSQ - 1); // i mod bSQ
        } else {
            i -= itemsInLargeBlocks;
            block = smallBlocks.get(i >> bExp);
            idx = i & (b - 1);
        }

        block.items[idx] = a;
    }

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
    }

    @Override
    public void grow(T a) {
        // Is the datastructure too full?
        if (n >= bCB)
            rebuildGrow();

        DataBlock<T> lastBlock = smallBlocks.last();
        if (lastBlock.isFull()){
            if (smallBlocks.length() == b << 1) {
                // TODO: Save one block to be used as the next free one
                // Store items in large block
                T[] items = Utils.createTypedArray(bSQ);
                for (int i = 0; i < b; i++){
                    DataBlock<T> block = smallBlocks.removeFirst();
                    System.arraycopy(block.items, 0, items, b*i, b);
                }
                largeBlocks[largeIdx++] = new DataBlock<>(items, bSQ);
                itemsInLargeBlocks += bSQ;
            }
            // Allocate another small block
            lastBlock = new DataBlock<>(b);
            smallBlocks.append(lastBlock);
        }

        lastBlock.append(a);
        n++;
    }

    private DataBlock<T> getLastNonFull(){
        int smallIdx = (n - itemsInLargeBlocks) >> bExp;
        return smallBlocks.get(smallIdx);
    }


    private void rebuildGrow(){
        int newB = b << 1;
        int newBSQ = newB * newB;

        DataBlock<T>[] newLargeBlocks = allocateDatablocks(newB); // TODO: Revisit size computation

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

        // Structure for small blocks
        CyclicArray<DataBlock<T>> newSmallBlocks = new CyclicArray<>(2*newB);

        // Remaining large blocks (at most 3) must be moved to small blocks
        for (int j = 4*i; j < largeIdx; j++){
            for (int k = 0, stop = b >> 1; k < stop; k++){
                T[] items = Arrays.copyOfRange(largeBlocks[j].items, newB*k, newB*(k+1));
                newSmallBlocks.append(new DataBlock<>(items, newB));
            }
        }

        // Add remaining small blocks
        for (int j = 0, stop = smallBlocks.length() / 2; j < stop; j++){
            T[] items = Utils.createTypedArray(newB);
            DataBlock<T> firstBlock = smallBlocks.removeFirst();
            DataBlock<T> secondBlock = smallBlocks.removeFirst();

            System.arraycopy(firstBlock.items, 0, items, 0, b);
            System.arraycopy(secondBlock.items, 0, items, b, b);
            newSmallBlocks.append(new DataBlock<>(items, firstBlock.size() + secondBlock.size()));
        }

        if (smallBlocks.length() != 0){
            T[] items = Utils.createTypedArray(newB);
            DataBlock<T> remainingBlock = smallBlocks.removeFirst();
            int size = remainingBlock.size();

            System.arraycopy(remainingBlock.items, 0, items, 0, size);
            newSmallBlocks.append(new DataBlock<>(items, size));
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
        if (n <= bSQ*b/64)
            rebuildShrink();

        // Are all small blocks empty?
        int nBlocks = smallBlocks.length();
        DataBlock<T> lastBlock = smallBlocks.last();
        if  (nBlocks <= 1 && (lastBlock == null || lastBlock.isEmpty())){
            // Take a large block and turn it into small blocks
            largeIdx--;
            itemsInLargeBlocks -= bSQ;
            DataBlock<T> oldLarge = largeBlocks[largeIdx];
            largeBlocks[largeIdx] = null;


            int start = 0;
            if (smallBlocks.length() == 1){
                DataBlock<T> block = smallBlocks.get(0);
                System.arraycopy(oldLarge.items, 0, block.items, 0, b);
                block.n = b;
                start = 1;
            }

            for (int i = start; i < b; i++){
                T[] items = Arrays.copyOfRange(oldLarge.items, i*b, (i+1)*b);
                smallBlocks.append(new DataBlock<>(items, b));
            }
        }

        // Remove item from last (non-empty) block. If the last two blocks become empty, remove the last.
        n--;
        DataBlock<T> loc = getLastWithItems();
        DataBlock<T> last = smallBlocks.last();
        T ret = loc.pop();
        if (loc.isEmpty() && loc != last)
            smallBlocks.removeLast();
        return ret;
    }

    private DataBlock<T> getLastWithItems(){
        int smallIdx = (n - itemsInLargeBlocks) >> bExp;
        return smallBlocks.get(smallIdx);
    }

    private void rebuildShrink(){
        int newB = b >> 1;
        int newBSQ = newB * newB;
        DataBlock<T>[] newLargeBlocks = allocateDatablocks(newB); // TODO: Revisit size computation
        int newLargeIdx = 0;

        // TODO: Assumes all old large blocks will fit in new blocks
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
                DataBlock<T> oldBlock = smallBlocks.removeFirst();
                System.arraycopy(oldBlock.items, 0, items, i * b, b);
            }
            largeBlocks[largeIdx++] = new DataBlock<>(items, newBSQ);
        }

        CyclicArray<DataBlock<T>> newSmallBlocks = new CyclicArray<>(2*newB);
        // Split all small blocks into new
        for (int i = 0, stop = smallBlocks.length(); i < stop; i++){
            DataBlock<T> oldBlock = smallBlocks.removeFirst();
            int oldSize = oldBlock.size();
            T[] firstItems = Arrays.copyOfRange(oldBlock.items, 0, newB);
            newSmallBlocks.append(new DataBlock<>(firstItems, Math.min(oldSize, newB)));
            if (oldSize > newB){
                T[] secondItems = Arrays.copyOfRange(oldBlock.items, newB, 2*newB);
                newSmallBlocks.append(new DataBlock<>(secondItems, oldSize - newB));
            }
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
        // Is the datastructure too full?
        if (n >= b*b*b)
            size = countedRebuildGrow();

        if (smallBlocks.last().isFull()){
            if (smallBlocks.length() == 2*b) {
                size = Math.max(wordCount() + (long) b * b, size);

                // Store items in large block
                T[] items = Utils.createTypedArray(b*b);
                for (int i = 0; i < b; i++){
                    DataBlock<T> block = smallBlocks.removeFirst();
                    System.arraycopy(block.items, 0, items, b*i, b);
                }

                // Error?
                largeBlocks[largeIdx++] = new DataBlock<>(items, b*b);
            }
            // Allocate another small block
            smallBlocks.append(new DataBlock<>(b));
        }


        DataBlock<T> last = getLastNonFull();
        last.append(a);
        n++;

        if (size == 0)
            return wordCount();
        return size;
    }

    private long countedRebuildGrow(){
        int bSQ = b*b;
        int newB = b << 1;
        int newBSQ = newB * newB;

        DataBlock<T>[] newLargeBlocks = allocateDatablocks(newB); // TODO: Revisit size computation

        // Since large blocks now contain 4x the amount of items, we need 4 large blocks from before
        int i = 0;
        for (; i < largeIdx/4; i++){
            T[] items = Utils.createTypedArray(newBSQ);
            for (int j = 0; j < 4; j++){
                int blockIdx = 4*i + j;
                System.arraycopy(largeBlocks[blockIdx].items, 0, items, bSQ*j, bSQ);
                largeBlocks[blockIdx] = null;
            }
            newLargeBlocks[i] = new DataBlock<>(items, newBSQ);
        }

        // Structure for small blocks
        CyclicArray<DataBlock<T>> newSmallBlocks = new CyclicArray<>(2*newB);

        // Remaining large blocks (at most 3) must be moved to small blocks
        for (int j = 4*i; j < largeIdx; j++){
            for (int k = 0; k < b/2; k++){
                T[] items = Arrays.copyOfRange(largeBlocks[j].items, newB*k, newB*(k+1));
                newSmallBlocks.append(new DataBlock<>(items, newB));
            }
        }

        // Add remaining small blocks
        for (int j = 0, stop = smallBlocks.length() / 2; j < stop; j++){
            T[] items = Utils.createTypedArray(newB);
            DataBlock<T> firstBlock = smallBlocks.removeFirst();
            DataBlock<T> secondBlock = smallBlocks.removeFirst();

            System.arraycopy(firstBlock.items, 0, items, 0, b);
            System.arraycopy(secondBlock.items, 0, items, b, b);
            newSmallBlocks.append(new DataBlock<>(items, firstBlock.size() + secondBlock.size()));
        }

        if (smallBlocks.length() != 0){
            T[] items = Utils.createTypedArray(newB);
            DataBlock<T> remainingBlock = smallBlocks.removeFirst();
            int size = remainingBlock.size();

            System.arraycopy(remainingBlock.items, 0, items, 0, size);
            newSmallBlocks.append(new DataBlock<>(items, size));
        }

        long size = constantWordCount() +
                MemoryLookup.wordSize(largeBlocks) +
                MemoryLookup.wordSize(newLargeBlocks)+
                MemoryLookup.wordSize(smallBlocks) +
                MemoryLookup.wordSize(newSmallBlocks) +
                newBSQ; // Overhead of arrays whilst moving values

        // Finalize values being moved
        largeBlocks = newLargeBlocks;
        smallBlocks = newSmallBlocks;
        largeIdx = i;
        b = newB;

        return size;
    }

    @Override
    public long countedShrink() {
        long size = 0;
        if (n <= b*b*b/64)
            size = countedRebuildShrink();

        // Are all small blocks empty?
        int nBlocks = smallBlocks.length();
        DataBlock<T> lastBlock = smallBlocks.last();
        if  (nBlocks <= 1 && (lastBlock == null || lastBlock.isEmpty())){
            size = Math.max(size, wordCount() + ((long) b)*b);
            // Take a large block and turn it into small blocks
            largeIdx--;
            DataBlock<T> oldLarge = largeBlocks[largeIdx];
            largeBlocks[largeIdx] = null;


            int start = 0;
            // Is there one empty small block remaining?
            if (smallBlocks.length() == 1){
                DataBlock<T> block = smallBlocks.get(0);
                System.arraycopy(oldLarge.items, 0, block.items, 0, b);
                block.n = b;
                start = 1;
            }

            for (int i = start; i < b; i++){
                T[] items = Arrays.copyOfRange(oldLarge.items, i*b, (i+1)*b);
                smallBlocks.append(new DataBlock<>(items, b));
            }
        }

        // Remove item from last (non-empty) block. If the last two blocks become empty, remove the last.
        n--;
        DataBlock<T> loc = getLastWithItems();
        DataBlock<T> last = smallBlocks.last();
        T ret = loc.pop();
        if (loc.isEmpty() && loc != last)
            smallBlocks.removeLast();

        if (size == 0)
            return wordCount();
        return size;
    }

    private long countedRebuildShrink(){
        long size = 0;

        int newB = b >> 1;
        int newBSQ = newB * newB;
        DataBlock<T>[] newLargeBlocks = allocateDatablocks(newB); // TODO: Revisit size computation
        int newLargeIdx = 0;

        // TODO: Assumes all old large blocks will fit in new blocks
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
                ((long) b)*b; // Overhead in copying

        largeIdx = newLargeIdx;
        largeBlocks = newLargeBlocks;

        // Move some of the old small blocks to be a single large block
        if (n - largeIdx * newBSQ > newBSQ) {
            T[] items = Utils.createTypedArray(newBSQ);
            for (int i = 0, stop = b / 4; i < stop; i++) {
                DataBlock<T> oldBlock = smallBlocks.removeFirst();
                System.arraycopy(oldBlock.items, 0, items, i * b, b);
            }
            largeBlocks[largeIdx++] = new DataBlock<>(items, newBSQ);
        }

        CyclicArray<DataBlock<T>> newSmallBlocks = new CyclicArray<>(2*newB);
        // Split all small blocks into new
        for (int i = 0, stop = smallBlocks.length(); i < stop; i++){
            DataBlock<T> oldBlock = smallBlocks.removeFirst();
            int oldSize = oldBlock.size();
            T[] firstItems = Arrays.copyOfRange(oldBlock.items, 0, newB);
            newSmallBlocks.append(new DataBlock<>(firstItems, Math.min(oldSize, newB)));
            if (oldSize > newB){
                T[] secondItems = Arrays.copyOfRange(oldBlock.items, newB, 2*newB);
                newSmallBlocks.append(new DataBlock<>(secondItems, oldSize - newB));
            }
        }
        size = Math.max(size,
                MemoryLookup.wordSize(largeBlocks) +
                MemoryLookup.wordSize(smallBlocks) +
                MemoryLookup.wordSize(newSmallBlocks) +
                b); // Size overhead when moving down a size

        smallBlocks = newSmallBlocks;
        b = newB;
        bSQ = newBSQ;
        itemsInLargeBlocks = bSQ * largeIdx;
        return size + constantWordCount();
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
