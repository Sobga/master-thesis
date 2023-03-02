package memory;

public class MemoryLookup {
    public static final int WORD_SIZE = 1;
    public static long wordSize(Object o){
        if (o == null)
            return 0;
        if (o instanceof Integer)
            return WORD_SIZE;
        if (o instanceof Long)
            return WORD_SIZE;
        if (o instanceof Float)
            return WORD_SIZE;

        if (o instanceof WordCountable)
            return ((WordCountable) o).byteCount();

        if (o.getClass().isArray()){
            Object[] oArr = (Object[]) o;
            long sum = WORD_SIZE * oArr.length;
            for (int i = 0; i < oArr.length; i++)
                if (!isPrimitive(oArr[i]))
                    sum += wordSize(oArr[i]);
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