package resizableArrays;

import utils.Utils;

public class TestArray<T> implements ResizableArray<T>{
    private final ResizableArray<DataBlock<T>> largeBlocks;
    private final BrodnikPowerTwo<T> smallBlocks;
    private final float alpha;
    private int n;
    private int k = 0;
    private int itemsInLarge;
    private int blocksInSuper = 0;

    private int nextSize = 0;
    private int maxBlocks = 0;

    public TestArray(){
        this(2f/3f);
    }

    public TestArray(float alpha){
        this.alpha = alpha;
        largeBlocks = new ConstantArray<>(1.0f);
        smallBlocks = new BrodnikPowerTwo<>();
        clear();
    }

    @Override
    public void clear() {
        n = 0;
        k = 0;
        blocksInSuper = 0;
        itemsInLarge = 0;
        nextSize = 1 << (int) Math.ceil(alpha*k);
        maxBlocks = 1 << (int) Math.floor((1-alpha)*(k+0.1));
        largeBlocks.clear();
        smallBlocks.clear();
    }

    @Override
    public int length() {
        return n;
    }

    @Override
    public T get(int i) {
        /*int itemsInLarge = LARGE_MASK & ((1 << 2*largeBlocks.length()) - 1);

        if (i < itemsInLarge){
            int kPrime = 32-Integer.numberOfLeadingZeros(i);
            int largeIdx = (kPrime + 1) / 2;
            int itemsBefore = LARGE_MASK & ((1 << largeIdx) - 1);
            return largeBlocks.get(largeIdx).items[i - itemsBefore];
        }


        return smallBlocks.get(i - itemsInLarge);*/

        if (i >= itemsInLarge)
            return smallBlocks.get(i - itemsInLarge);

        int r = i + 1;

        // Superblock idx
        int kPrime = (31-Integer.numberOfLeadingZeros(r));
        int datablocksBefore = datablocksBefore(kPrime);
        int idxBits = (int) Math.ceil(alpha * kPrime);

        // Find datablock in superblock
        int blocksBefore = Utils.removeHighestSetBit(r) >> idxBits;
        DataBlock<T> block = largeBlocks.get(datablocksBefore+blocksBefore);

        // Find index in datablock
        int blockIdx = r & ((1 << idxBits) - 1);
        return block.items[blockIdx];
    }

    @Override
    public void set(int i, T a) {

    }

    private int datablocksBefore(int kPrev){
        /*double b = 1 - alpha;
        double top = Math.pow(2, b*kPrev + b) - 1;
        double bot = Math.pow(2, b) - 1;
        return (int) (top / bot);*/
        int res = 0;
        for (int i = 0; i < kPrev; i++)
            res += 1 << (int) Math.floor((1-alpha)*(i+0.1));
        return res;
    }

    @Override
    public void grow(T a) {
        //int nextSize = 1 << (largeBlocks.length()/4);
//        int nextSize = largeBlocks.length() * largeBlocks.length();

        // Big enough for another large block
        if (smallBlocks.length() >= nextSize){

            // Move all items in small blocks over
            T[] items = smallBlocks.asArray();
            DataBlock<T> newBlock = new DataBlock<>(items, nextSize);
            largeBlocks.grow(newBlock);
            itemsInLarge += nextSize;
            blocksInSuper++;

            // Clear all small items
            smallBlocks.clear();
            if (blocksInSuper >= maxBlocks){
                k++;
                blocksInSuper = 0;
                nextSize = 1 << (int) Math.ceil(alpha*k);
                maxBlocks = 1 << (int) Math.floor((1-alpha)*(k+0.1));
            }
        }

        smallBlocks.grow(a);
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
