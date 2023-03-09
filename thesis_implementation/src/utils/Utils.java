package utils;

import benchmarking.Operation;
import benchmarking.OperationType;

import java.util.Arrays;
import java.util.Random;

public class Utils {
    public static long slowMedian(long[] A){
        int n = A.length;

        long[] copy = A.clone();
        Arrays.sort(copy);

        if (n % 2 == 1)
            return copy[n/2];

        return (copy[(n-1)/2] + copy[n/2]) / 2;
    }

    public static Operation[] generateOperations(int size, boolean doRandom){
        if (doRandom)
            return generateOperations(size);
        return generateOperations(size, 1, 0, 0, 0);
    }

    public static Operation[] generateOperations(int size){
        return generateOperations(size, 1, 1, 1, 1);
    }

    public static Operation[] generateOperations(int size, int wGrow, int wGet, int wSet, int wShrink){
        Operation[] operations = new Operation[size];
        Random random = new Random();

        int[] cumSum = new int[]{
                wGet,
                wGet + wSet,
                wGet + wSet + wGrow,
                wGet + wSet + wGrow + wShrink
        };

        int n = 0;
        for (int i = 0; i < size; i++){
            if (n == 0){
                operations[i] = new Operation(OperationType.GROW, i, i);
                n++;
                continue;
            }

            // Generate random operation
            int sample = random.nextInt(cumSum[3]);
            int j = 0;
            for (; j < cumSum.length; j++){
                if (sample < cumSum[j])
                    break;
            }
            OperationType op = OperationType.fromInt(j);

            int idx = random.nextInt(n); // Location of operation
            operations[i] = new Operation(op, idx, i);

            // Update n
            if (op == OperationType.GROW)
                n++;
            else if (op == OperationType.SHRINK)
                n--;

        }
        return operations;
    }

    public static Operation[] growShrink(int grows, int shrinks){
        Operation[] operations = new Operation[grows+shrinks];

        for (int i = 0; i < operations.length; i++){
            OperationType op = OperationType.fromInt(i < grows ? 2 : 3);
            operations[i] = new Operation(op, -1, i);
        }
        return operations;
    }

    public static <T> T[] createTypedArray(int size){
        return (T[]) new Object[size];
    }

    public static int log2nlz(int bits ) {
        if( bits == 0 )
            return 0; // or throw exception
        return 31 - Integer.numberOfLeadingZeros( bits );
    }
}
