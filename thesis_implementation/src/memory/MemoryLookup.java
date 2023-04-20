package memory;

public class MemoryLookup {
    public static final int WORD_SIZE = 1;
    public static long wordSize(Object o){
        if (o == null)
            return 0;

        if (o instanceof WordCountable)
            return ((WordCountable) o).wordCount();

        if (isPrimitive(o))
            return WORD_SIZE;

        if (o.getClass().isArray()){
            Object[] oArr = (Object[]) o;
            long sum = WORD_SIZE * oArr.length;

            if (!isPrimitive(oArr[0])) {
                for (Object obj : oArr)
                    sum += wordSize(obj);
            }
            return sum;

        }
        System.out.println("No bytesize defined for " + o);
        return -1;
    }

    public static boolean isPrimitive(Object o){
        if (o == null)
            return false;
        if (o instanceof Integer)
            return true;
        if (o instanceof Long)
            return true;
        if (o instanceof Boolean)
            return true;
        if (o instanceof Float)
            return true;
        if (o instanceof Double)
            return true;
        return false;
    }
}
