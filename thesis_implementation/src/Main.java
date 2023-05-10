import resizableArrays.ResArrayList;
import resizableArrays.ResizableArray;
import resizableArrays.*;
import utils.Utils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

public class Main {
    public void run(){
        // Baseline implementation to compare against
        ResizableArray<Integer> baseline = new ResArrayList<>();

        // Resizable Arrays to test
        ArrayList<ResizableArray<Integer>> arrays = new ArrayList<>();
        arrays.add(baseline);
//        arrays.add(new ConstantArray<>(1));
//        arrays.add(new ConstantLazyArray<>(1));
//        arrays.add(new Brodnik<>());
//        arrays.add(new Tarjan<>());
//        arrays.add(new BrodnikPowerTwo<>());
//        arrays.add(new Sitarski<>());
        arrays.add(new TestArray<>());
        // Generate random operations
        Random random = new Random(0);
        long seed = random.nextLong();
        System.out.println("Seed: " + seed);
        Utils.setSeed(seed);

        int n = (int) 1E6;
        for (ResizableArray<Integer> array : arrays){
            for (int i = 0; i < n; i++) {
                array.grow(i);
            }
        }

        //int size = arrays.get(0).length();
        //int[] indices = Utils.indexingPermutation(n);
        for (ResizableArray<Integer> array : arrays)
            {
                for (int i = 0; i < n; i++){

    //                array.set(indices[i], i);
//                    array.grow(i);
                    array.shrink();
    //                array.countedGrow(i);

            }
            //assert (resizableEqual(baseline, arrays));
        }
    }

    // Determines if all arrays are equal (ie. contain the same elements)
    public boolean resizableEqual(ResizableArray<Integer> baseline, ArrayList<ResizableArray<Integer>> arrays){
        if (arrays.size() == 0)
            return true;

        // All lengths must be equal
        int length = baseline.length();
        for (ResizableArray<Integer> array : arrays)
            if (array.length() != length)
                return false;

        for (ResizableArray<Integer> array: arrays){
            if (array == baseline)
                continue;
            Iterator<Integer> baseIterator = baseline.iterator();
            Iterator<Integer> arrayIterator = array.iterator();

            while (baseIterator.hasNext()) {
                int baseItem = baseIterator.next();
                int arrayItem = arrayIterator.next();
                if (baseItem != arrayItem) {
                    System.out.println("Error, got " + arrayItem + " should have been " + baseItem);
                    return false;
                }
            }
        }
        return true;
    }

    public static void main(String[] args) {
        new Main().run();
    }
}



