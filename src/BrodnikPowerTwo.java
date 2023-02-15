public class BrodnikPowerTwo<T> implements ResizableArray<T>{
    ResizableArray<DataBlock<T>> blocks;
    private int k;
    private int n;

    public BrodnikPowerTwo(){
        blocks = new ConstantLazyArray<>(1);
        blocks.grow(new DataBlock<>(this,1));
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

        DataBlock<T> lastBlock = blocks.last();
        if (!lastBlock.isFull()){
            // There is space in the last data block
            lastBlock.append(a);
        } else{
            // Must allocate new datablock
            // Determine if super-block is full
            if (isPowerOfTwo(n))
                k++;

            // Size should be 2^(ceil(k/2))
            DataBlock<T> d = new DataBlock<>(this, 1 << ((k+1)/2));
            blocks.grow(d);
            d.append(a);
        }
    }

    @Override
    public T shrink() {
        if (isPowerOfTwo(n))
            k--;
        n--;

        DataBlock<T> lastBlock = blocks.last();
        if (lastBlock.isEmpty()) {
            blocks.shrink();
            lastBlock = blocks.last();
        }

        return lastBlock.pop();
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

    public static void main(String[] args) {
        int count = 12;
        int sum = 0;

        System.out.println("k\tRes\tComputed");
        for (int k = 0; k < count; k++){
            System.out.println(k + "\t" + sum + "\t" + prevBlockSize(k));
            sum += 1 << (k/2);
        }
    }
}
