package resizableArrays;

import memory.MemoryLookup;
import utils.Utils;

import javax.xml.crypto.Data;
import java.util.Arrays;

public class Tarjan<T> implements ResizableArray<T>{
    private DataBlock<T>[] largeBlocks;
    private CyclicArray<DataBlock<T>> smallBlocks;
//    private DataBlock<T>[] activeBlocks;
//    private DataBlock<T>[] inactiveBlocks;
    private int largeIdx;
//    private int smallIdx;
    private int n;
    private int b = 1;

    public Tarjan(){
        clear();
    }

    @Override
    public void clear() {
        n = 0;
        b = 2;
        largeIdx = 0;
//        smallIdx = 0;
//        activeBlocks = allocateDatablocks(b);
//        inactiveBlocks = allocateDatablocks(b);
        smallBlocks = new CyclicArray<>(2*b);
        smallBlocks.append(new DataBlock<>(b));
        largeBlocks = allocateDatablocks(b);
    }

    @Override
    public long byteCount() {
        return  MemoryLookup.wordSize(n) +
                MemoryLookup.wordSize(b) +
                MemoryLookup.wordSize(largeIdx) +
                MemoryLookup.wordSize(largeBlocks) +
                MemoryLookup.wordSize(smallBlocks);
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
        locate(i, a);
    }

    private T locate(int idx, T a){
        int bSQ = b*b; // TODO: Use bit tricks

        // Is the index inside the large blocks?
        if (idx < bSQ * largeIdx)
            return getOrSet(largeBlocks[idx / bSQ], idx % bSQ, a);

        idx -= bSQ * largeIdx;

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
        if (n >= b*b*b)
            rebuild(b << 1);

        if (smallBlocks.last().isFull()){
            if (smallBlocks.length() == 2*b) {
                // TODO: Save one block to be used as the next free one
                // Store items in large block
                T[] items = Utils.createTypedArray(b*b);
                for (int i = 0; i < b; i++){
                    DataBlock<T> block = smallBlocks.removeFirst();
                    System.arraycopy(block.items, 0, items, b*i, b);
                }
                // ERROR: Need to rebuild at some point
                largeBlocks[largeIdx++] = new DataBlock<>(items, b*b);
            }
            // Allocate another small block
            smallBlocks.append(new DataBlock<>(b));
        }

        n++;
        DataBlock<T> last = getLastNonFull();
        last.append(a);
    }



    @Override
    public T shrink() {
        if (n <= b*b*b/64)
            rebuild(b >> 1);

        // Are all small blocks empty?
        int nBlocks = smallBlocks.length();
        DataBlock<T> lastBlock = smallBlocks.last();
        if  (nBlocks <= 1 && (lastBlock == null || lastBlock.isEmpty())){
            // Take a large block and turn it into small blocks
            largeIdx--;
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

    private DataBlock<T> getLastNonFull(){
        int nBlocks = smallBlocks.length();
        if (nBlocks - 2 >= 0 && !smallBlocks.get(nBlocks - 2).isFull())
            return smallBlocks.get(nBlocks-2);
        return smallBlocks.last();
    }

    private DataBlock<T> getLastWithItems(){
        DataBlock<T> last = smallBlocks.last();
        if (last.isEmpty())
            return smallBlocks.get(smallBlocks.length() - 2);
        return last;
    }

    private void rebuild(int newB){
        int newBSQ = newB * newB;
        int bSQ = b * b;

        // We are increasing b
        if (newB > b){
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

            // Finalize values being moved
            largeBlocks = newLargeBlocks;
            smallBlocks = newSmallBlocks;
            largeIdx = i;
            b = newB;
        }
        else {

        }
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
