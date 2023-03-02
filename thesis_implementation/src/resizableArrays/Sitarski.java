package resizableArrays;

import memory.MemoryLookup;
import utils.Utils;

import java.util.Arrays;

public class Sitarski<T> implements ResizableArray<T>{
    private DataBlock<T>[] indexBlock;
    private int b = 1; // Assume b is kept as a power of 2
    private int n;

    public Sitarski(){
        indexBlock = (DataBlock<T>[]) new DataBlock[b];
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
        DataBlock<T> block = indexBlock[i >> log2nlz(b)];
        return block.items[i & (b-1)];
    }

    @Override
    public void set(int i, T a) {
        DataBlock<T> block = indexBlock[i >> log2nlz(b)];
        block.items[i & (b-1)] = a;
    }

    @Override
    public void grow(T a) {
        // Is the datastructure too full?
        if (n == b*b){
            // Rebuild
            rebuild(true);
        }

        int blockIdx = n >> log2nlz(b);

        // We need a new block
        if (indexBlock[blockIdx] == null)
            indexBlock[blockIdx] = new DataBlock<>(b);

        // Add item
        indexBlock[blockIdx].append(a);
        n++;
    }

    @Override
    public T shrink() {
        n--;

        if (n == b*b / 16){
            rebuild(false);
        }
        int blockIdx = n >> log2nlz(b);
        T ret = indexBlock[blockIdx].pop();

        if (indexBlock[blockIdx].isEmpty() && blockIdx+1 < b)
            indexBlock[blockIdx+1] = null;
        return ret;
    }

    // Rebuild the datastructure with a new B value. Are we increasing or decreasing B?
    private void rebuild(boolean doIncrease){
        int newB = Math.max(doIncrease ? b << 1 : b >> 1, 1);
        DataBlock<T>[] newIndex = (DataBlock<T>[]) new DataBlock[newB];

        // Copy values over
        if (doIncrease){
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
        } else{
            // Block size is decreasing; each new block can only hold half of the previous items
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
        }

        b = newB;
        indexBlock = newIndex;
    }

    @Override
    public void clear() {
        b = 1;
        n = 0;
        indexBlock = (DataBlock<T>[]) new DataBlock[b];
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

    public static int log2nlz(int bits ) {
        if( bits == 0 )
            return 0; // or throw exception
        return 31 - Integer.numberOfLeadingZeros( bits );
    }

    @Override
    public long byteCount() {
        return MemoryLookup.wordSize(n) + MemoryLookup.wordSize(b) + MemoryLookup.wordSize(indexBlock);
    }
}
