package resizableArrays;

import memory.WordCountable;

import java.lang.reflect.Field;
import java.util.ArrayList;

public class ArrayListExtended<T> extends ArrayList<T> implements WordCountable{
    private Field arrayField;
    public ArrayListExtended(){
        super();

        Class cls = null;
        try {
            cls = Class.forName("java.util.ArrayList");
            arrayField = cls.getDeclaredField("elementData");
        } catch (ClassNotFoundException | NoSuchFieldException e) {
            e.printStackTrace();
        }
        arrayField.setAccessible(true);
    }

    public long wordCount() {
        try {
            Object[] o = (Object[]) arrayField.get(this);
            return o.length;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return -1;
    }
}
