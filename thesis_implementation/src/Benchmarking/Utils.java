package Benchmarking;

import java.util.Arrays;

public class Utils {
    public static long slowMedian(long[] A){
        int n = A.length;

        long[] copy = A.clone();
        Arrays.sort(copy);


        if (n % 2 == 1)
            return copy[n/2];

        return (copy[(n-1)/2] + copy[n/2]) / 2;
    }
}
