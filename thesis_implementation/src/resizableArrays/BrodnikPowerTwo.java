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
        return locate(i, null);
    }

    public void set(int i, T a){
        locate(i, a);
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

        k = Utils.log2nlz(n);

        DataBlock<T> lastBlock = blocks.last();
        if (!lastBlock.isFull()){
            // There is space in the last data block
            lastBlock.append(a);
        } else{
            // Must allocate new datablock
            // Determine if super-block is full
//            if (isPowerOfTwo(n))
//                k++;

            // Size should be 2^(ceil(k/2))
            DataBlock<T> d = new DataBlock<>(1 << ((k + 1) / 2));
            blocks.grow(d);
            d.append(a);
        }
    }

    @Override
    public T shrink() {
        n--;
        k = Utils.log2nlz(n);

//        if (isPowerOfTwo(n))
//            k--;

        DataBlock<T> lastBlock = blocks.last();
        if (lastBlock.isEmpty()) {
            blocks.shrink();
            lastBlock = blocks.last();
        }

        return lastBlock.pop();
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

    // https://stackoverflow.com/a/600306
    private boolean isPowerOfTwo(int x) {
        return (x & (x - 1)) == 0;
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
    public long byteCount() {
        return MemoryLookup.wordSize(n) + MemoryLookup.wordSize(k) + MemoryLookup.wordSize(blocks);
    }
}
