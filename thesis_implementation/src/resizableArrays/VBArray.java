package resizableArrays;

import memory.WordCountable;
import utils.Utils;

public class VBArray<T> implements ResizableArray<T>{
    private final ResizableArray<T[]> largeBlocks;
    private final ResizableArray<SuperblockInfo> superInfo;
    private final CyclicDatablockArray<T> smallBlocks;
    private final float alpha;
    private int n;
    private int k = 0;
    private int itemsInLarge;
    private int blocksInSuper = 0;

    private int nextSize = 0;
    private int maxBlocks = 0;

    public VBArray(){
        this(2f/3f);
    }

    public VBArray(float alpha){
        this.alpha = alpha;
        superInfo = new ConstantArray<>(1.0f);
        largeBlocks = new ConstantArray<>(1.0f);
        smallBlocks = new CyclicDatablockArray<>(1, 1);
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
        superInfo.clear();
        superInfo.grow(new SuperblockInfo(0));
    }

    class SuperblockInfo implements WordCountable {
        final int dataBlocksBefore;
        final int idxBits;
        final int blockMask;

        public SuperblockInfo(int k){
            dataBlocksBefore = datablocksBefore(k);
            idxBits = (int) Math.ceil(alpha * k);
            blockMask = ((1 << idxBits) - 1);
        }

        @Override
        public String toString() {
            return "SBI{"
                    + dataBlocksBefore +
                    ", " + idxBits +
                    ", " + blockMask +
                    '}';
        }

        @Override
        public long wordCount() {
            return 3;
        }
    }

    @Override
    public int length() {
        return n;
    }

    @Override
    public T get(int i) {
        if (i >= itemsInLarge)
            return smallBlocks.get(i - itemsInLarge);

        int r = i + 1;

        // Superblock idx
        int kPrime = (31-Integer.numberOfLeadingZeros(r));
        int datablocksBefore = datablocksBefore(kPrime);
        int idxBits = (int) Math.ceil(alpha * kPrime);

        // Find datablock in superblock
        int blocksBefore = Utils.removeHighestSetBit(r) >> idxBits;
        T[] block = largeBlocks.get(datablocksBefore+blocksBefore);

        // Find index in datablock
        int blockIdx = r & ((1 << idxBits) - 1);
        return block[blockIdx];
    }

    @Override
    public void set(int i, T a) {
        if (i >= itemsInLarge) {
            smallBlocks.set(i - itemsInLarge, a);
            return;
        }

        int r = i + 1;

        // Superblock idx
        int kPrime = (31-Integer.numberOfLeadingZeros(r));
        SuperblockInfo blockInfo = superInfo.get(kPrime);

        int blocksBefore = Utils.removeHighestSetBit(r) >> blockInfo.idxBits;
        T[] block = largeBlocks.get(blockInfo.dataBlocksBefore + blocksBefore);

        int blockIdx = r & blockInfo.blockMask;

        block[blockIdx] = a;
    }

    private int datablocksBefore(int kPrev){
        int res = 0;
        for (int i = 0; i < kPrev; i++)
            res += 1 << (int) Math.floor((1-alpha)*(i+0.1));
        return res;
    }

    @Override
    public void grow(T a) {
        // Big enough for another large block
        if (smallBlocks.isFull()){
            // Move all items in small blocks over
            T[] items = smallBlocks.removeKLast(nextSize);
            largeBlocks.grow(items);
            itemsInLarge += nextSize;
            blocksInSuper++;

            if (blocksInSuper >= maxBlocks){
                // Start of a new superblock
                k++;
                superInfo.grow(new SuperblockInfo(k));
                blocksInSuper = 0;
                nextSize = 1 << (int) Math.ceil(alpha*k);
                maxBlocks = 1 << (int) Math.floor((1-alpha)*(k+0.1));

                if (smallBlocks.capacity < 2*nextSize)
                    rebuild();
            }
        }

        smallBlocks.append(a);
        n++;
    }

    public long countedGrow(T a){
        long size = wordCount();
        // Big enough for another large block
        if (smallBlocks.isFull()){
            // Move all items in small blocks over
            T[] items = smallBlocks.removeKLast(nextSize);
            size += items.length;

            largeBlocks.grow(items);
            itemsInLarge += nextSize;
            blocksInSuper++;

            if (blocksInSuper >= maxBlocks){
                // Start of a new superblock
                k++;
                superInfo.grow(new SuperblockInfo(k));
                blocksInSuper = 0;
                nextSize = 1 << (int) Math.ceil(alpha*k);
                maxBlocks = 1 << (int) Math.floor((1-alpha)*(k+0.1));

                if (smallBlocks.capacity < 2*nextSize)
                    rebuild();
            }
        }

        smallBlocks.append(a);
        n++;
        return size;
    }

    @Override
    public T shrink() {
        if (smallBlocks.isEmpty()){
            // A block must be converted into smaller datablocks
            blocksInSuper--;
            if (blocksInSuper < 0){
                // We are now in a smaller superblock
                k--;
                superInfo.shrink();
                nextSize = 1 << (int) Math.ceil(alpha*k);
                maxBlocks = 1 << (int) Math.floor((1-alpha)*(k+0.1));
                blocksInSuper = maxBlocks - 1;

                // Ensure small blocks don't use too much space
                if (smallBlocks.capacity > 2*nextSize)
                    rebuild();
            }

            // Move items over
            T[] removedBlock = largeBlocks.shrink();
            smallBlocks.appendMany(removedBlock, removedBlock.length);
            itemsInLarge -= removedBlock.length;
        }
        n--;
        return smallBlocks.pop();
    }

    @Override
    public long countedShrink() {
        long size = wordCount();
        if (smallBlocks.isEmpty()){
            // A block must be converted into smaller datablocks
            blocksInSuper--;
            if (blocksInSuper < 0){
                // We are now in a smaller superblock
                k--;
                superInfo.shrink();
                nextSize = 1 << (int) Math.ceil(alpha*k);
                maxBlocks = 1 << (int) Math.floor((1-alpha)*(k+0.1));
                blocksInSuper = maxBlocks - 1;

                // Ensure small blocks don't use too much space
                if (smallBlocks.capacity > 2*nextSize)
                    rebuild();
            }

            // Move items over
            T[] removedBlock = largeBlocks.shrink();
            size += removedBlock.length;
            smallBlocks.appendMany(removedBlock, removedBlock.length);
            itemsInLarge -= removedBlock.length;
        }
        n--;
        smallBlocks.pop();
        return size;
    }

    private void rebuild(){
        // Rebuild smallBlock array such that we can store 2x the next block
        int nextSizeExp = Utils.log2nlz(nextSize);
        int newBlockExponent = (int) Math.ceil(nextSizeExp / 2.0);
        smallBlocks.rebuild(1 << (nextSizeExp - newBlockExponent + 1), 1 << newBlockExponent);
    }

    @Override
    public String getName() {
        return "VB-Array";
    }

    @Override
    public long wordCount() {
        return constantCount() + smallBlocks.wordCount() + largeBlocks.wordCount() + superInfo.wordCount();
    }

    private long constantCount(){
        return 7;
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
