import javafx.util.Pair;

import java.util.ArrayList;
import java.util.Random;

public class Main {
    public void run(){
        // Baseline implementation to compare against
        ResizableArray<Integer> baseline = new ResArrayList<>();

        // Resizable Arrays to test
        ArrayList<ResizableArray<Integer>> arrays = new ArrayList<>();
        arrays.add(baseline);
        arrays.add(new ConstantLazyArray<>(1));
        arrays.add(new Brodnik<>());
        arrays.add(new BrodnikPowerTwo<>());

        Random random = new Random(1);
        long seed = random.nextLong();
        random.setSeed(seed);

        System.out.println("Seed: " + seed);
        for (int i = 0; i < 10000; i++){

            Pair<Integer, Integer> operationInfo = getOperation(baseline, random);
            for (ResizableArray<Integer> array : arrays)
                switch (operationInfo.getKey()){
                    case 0 -> {
                        array.grow(i);
                        //System.out.println("Grow");
                    }
                    case 1 -> {
                        array.set(operationInfo.getValue(), i);
                        //System.out.println("Set");
                        }
                    case 2 -> {
                        array.shrink();
                        //System.out.println("Shrink");
                    }
                }
            assert resizableEqual(baseline, arrays);
        }
    }

    // "Randomly" selects the next iteration to be performed
    private Pair<Integer, Integer> getOperation(ResizableArray<?> baseline, Random random){
        // Must enforce a grow operation
        if (baseline.length() == 0)
            return new Pair<>(0, 0);

        int operation = random.nextInt(2);
        int idx = random.nextInt(baseline.length());
        return new Pair<>(operation, idx);
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



