import benchmarking.Operation;
import resizableArrays.ResArrayList;
import resizableArrays.ResizableArray;
import resizableArrays.Tarjan;
import utils.Utils;

import java.util.ArrayList;
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
        arrays.add(new Tarjan<>());
//        arrays.add(new BrodnikPowerTwo<>());
//        arrays.add(new Sitarski<>());

        // Generate random operations
        Random random = new Random(0);
        long seed = random.nextLong();
        System.out.println("Seed: " + seed);
        Utils.setSeed(seed);
        Operation[] operations = Utils.generateOperations(1000000, 2, 0, 0, 1);

        for (int i = 0; i < operations.length; i++){
            for (ResizableArray<Integer> array : arrays)
                operations[i].applyOperation(array);
            assert (resizableEqual(baseline, arrays));
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

        // All values reported must be correct as well
        for (int i = 0; i < length; i++){
            int value = baseline.get(i);
            for (ResizableArray<Integer> array : arrays)
                if (array.get(i) != value)
                    return false;
        }
        return true;
    }

    public static void main(String[] args) {
        new Main().run();
    }
}



