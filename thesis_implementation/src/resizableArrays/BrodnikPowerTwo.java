package resizableArrays;

import memory.MemoryLookup;
import utils.Utils;

public class BrodnikPowerTwo<T> implements ResizableArray<T>{
    ResizableArray<DataBlock<T>> blocks;
    private int k;
    private int n;

    public BrodnikPowerTwo(){
        blocks = new ConstantArray<>(1f);
        blocks.grow(new DataBlock<>(1));
    }

    @Override
    public String getName() {
        return "BrodnikPowerTwo";
    }

    public int length(){
        return n;
    }

    public T get(int i) {
        int r = i + 1;
        int kPrime = (31-Integer.numberOfLeadingZeros(r));

        int kPrimeHalf = ((kPrime+1)/2);
        int b = removeHighestSetBit(r) >> kPrimeHalf;   // floor(k/2) highest bits of r, after leading 1
        int e = r & ((1 << kPrimeHalf) - 1);            //  Bitmask to extract ceil(k/2) lower bits of r

//        int p = (1 << kPrime) - 1;
        int p = prevBlockSize(kPrime);

        return blocks.get(p + b).items[e];
    }

    public void set(int i, T a){
        int r = i + 1;
        int kPrime = (31-Integer.numberOfLeadingZeros(r));

        /*
        int b = removeHighestSetBit(r) >> ((kPrime+1)/2);   // floor(k/2) highest bits of r, after leading 1
        int e = r & ((1 << (kPrime+1) / 2) - 1);            //  Bitmask to extract ceil(k/2) lower bits of r
        int p = prevBlockSize(kPrime);
        blocks.get(p + b).items[e] = a;*/

        int kCeil = (kPrime+1) >> 1;
        int kFloor = kPrime >> 1;

        int b = removeHighestSetBit(r) >> kCeil;
        int e = r & (1 << kCeil) - 1;
        int exp = 1 << kFloor;
        // int p = (kPrime & 1) == 0 ? 2*exp : 3*exp;
        // p -= 2;
        int p = (2 + (kPrime & 1)) * exp - 2;

        blocks.get(p + b).items[e] = a;
    }

    private T locate(int i, T newValue){
        int r = i + 1;
        int kPrime = (31-Integer.numberOfLeadingZeros(r));

        int b = removeHighestSetBit(r) >> ((kPrime+1)/2);   // floor(k/2) highest bits of r, after leading 1
        int e = r & ((1 << (kPrime+1) / 2) - 1);            //  Bitmask to extract ceil(k/2) lower bits of r

//        int p = (1 << kPrime) - 1;
        int p = prevBlockSize(kPrime);

        DataBlock<T> d = blocks.get(p + b);

        if (newValue != null)
            d.items[e] = newValue;
        return d.items[e];
    }

    @Override
    public void grow(T a) {
        n++;
        DataBlock<T> lastBlock = blocks.last();
        if (lastBlock.isFull()){
            // Must allocate new datablock

            // Size should be 2^(ceil(k/2))
            k = Utils.log2nlz(n);
            lastBlock = new DataBlock<>(1 << ((k + 1) / 2));
            blocks.grow(lastBlock);
        }
        lastBlock.append(a);
    }

    @Override
    public T shrink() {
        n--;

        DataBlock<T> lastBlock = blocks.last();
        if (lastBlock.isEmpty()) {
            k = Utils.log2nlz(n);
            blocks.shrink();
            lastBlock = blocks.last();
        }

        return lastBlock.pop();
    }

    @Override
    public long countedGrow(T a) {
        long size = MemoryLookup.wordSize(n) + MemoryLookup.wordSize(k);
        n++;
        DataBlock<T> lastBlock = blocks.last();
        if (lastBlock.isFull()){
            // Must allocate new datablock

            // Size should be 2^(ceil(k/2))
            k = Utils.log2nlz(n);
            lastBlock = new DataBlock<>(1 << ((k + 1) / 2));
            size += blocks.countedGrow(lastBlock);
        } else {
            size += MemoryLookup.wordSize(blocks);
        }

        lastBlock.append(a);
        return size;
    }

    @Override
    public long countedShrink() {
        long size = MemoryLookup.wordSize(n) + MemoryLookup.wordSize(k);
        n--;

        DataBlock<T> lastBlock = blocks.last();
        if (lastBlock.isEmpty()) {
            k = Utils.log2nlz(n);
            size += blocks.countedShrink();
            lastBlock = blocks.last();
        } else {
            size += MemoryLookup.wordSize(blocks);
        }
        lastBlock.pop();
        return size;
    }

    @Override
    public void clear() {
        n = 0;
        k = 0;
        blocks.clear();
        blocks.grow(new DataBlock<>(1));
    }

    @Override
    public String toString() {
        return "BrodnikPowerTwo{" +
                "blocks=" + blocks.toString() +
                ", k=" + k +
                ", n=" + n +
                '}';
    }

    private static int prevBlockSize(int k){
        int base = (1 << (1 + k/2)) - 2;
        if ((k & 1) == 0)
            return  base;
        return base + (1 << k/2);
    }

    private int removeHighestSetBit(int x){
        return x & (Integer.highestOneBit(x)-1);
    }

    public static int binlog( int bits ) // returns 0 for bits=0
    {
        int log = 0;
        if( ( bits & 0xffff0000 ) != 0 ) { bits >>>= 16; log = 16; }
        if( bits >= 256 ) { bits >>>= 8; log += 8; }
        if( bits >= 16  ) { bits >>>= 4; log += 4; }
        if( bits >= 4   ) { bits >>>= 2; log += 2; }
        return log + ( bits >>> 1 );
    }


    public static void prevBlockTest(){
        int count = 12;
        int sum = 0;

        System.out.println("k\tRes\tComputed");
        for (int k = 0; k < count; k++){
            System.out.println(k + "\t" + sum + "\t" + prevBlockSize(k));
            sum += 1 << (k/2);
        }
    }

    @Override
    public long wordCount() {
        return MemoryLookup.wordSize(n) + MemoryLookup.wordSize(k) + MemoryLookup.wordSize(blocks);
    }
}
