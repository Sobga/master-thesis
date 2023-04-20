package resizableArrays;

import utils.Utils;

public class TestArray<T> implements ResizableArray<T>{
    private ResizableArray<DataBlock<T>> largeBlocks;
    private BrodnikPowerTwo<T> smallBlocks;
    private int n;
    private int k = 0;
    private int blocksInSuper = 0;
    private final float alpha = 2f/3f;
    private int smallItems = 0;
    private static int LARGE_MASK = 0;

    public TestArray(){
        for (int i = 0; i < 16; i++)
            LARGE_MASK |= 1 << 2*i;
        largeBlocks = new ConstantArray<>(1.0f);
        smallBlocks = new BrodnikPowerTwo<>();
        clear();
    }

    @Override
    public void clear() {
        smallItems = 0;
        largeBlocks.clear();
        smallBlocks.clear();
    }

    @Override
    public int length() {
        return n;
    }

    @Override
    public T get(int i) {
        int itemsInLarge = LARGE_MASK & ((1 << 2*largeBlocks.length()) - 1);

        if (i < itemsInLarge){
            int kPrime = 32-Integer.numberOfLeadingZeros(i);
            int largeIdx = (kPrime + 1) / 2;
            int itemsBefore = LARGE_MASK & ((1 << largeIdx) - 1);
            return largeBlocks.get(largeIdx).items[i - itemsBefore];
        }


        return smallBlocks.get(i - itemsInLarge);
    }

    @Override
    public void set(int i, T a) {

    }

    @Override
    public void grow(T a) {
        //int nextSize = 1 << (largeBlocks.length()/4);
//        int nextSize = largeBlocks.length() * largeBlocks.length();
        int nextSize = 1 << (int) Math.ceil(alpha*k);
        // Big enough for another large block
        if (smallItems >= nextSize){

            // Move all items in small blocks over
            T[] items = Utils.createTypedArray(nextSize);
            int i = 0;
            for (T item: smallBlocks)
                items[i++] = item;
            DataBlock<T> newBlock = new DataBlock<>(items, nextSize);
            largeBlocks.grow(newBlock);

            // Clear all small items
            smallItems = 0;
            smallBlocks.clear();

            int maxBlocks = 1 << (int) Math.floor((1-alpha)*k);
            if (++blocksInSuper > maxBlocks){
                k++;
                blocksInSuper = 0;
            }
        }

        smallBlocks.grow(a);
        smallItems++;
        n++;
    }

    @Override
    public T shrink() {
        return null;
    }

    @Override
    public String getName() {
        return "TestArray";
    }

    @Override
    public long wordCount() {
        return smallBlocks.wordCount() + largeBlocks.wordCount();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("TestArray{");
        for (int i = 0; i < n; i++){
            if (i != 0)
                sb.append(", ");
            sb.append(get(i));
        }
        sb.append("}");
        return sb.toString();
    }
}
