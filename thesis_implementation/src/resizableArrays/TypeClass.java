package resizableArrays;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public abstract class TypeClass<T> {
    private Type type;
    public TypeClass(){
        this.type = ((ParameterizedType) this.getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }

    public Type getType() {
        return type;
    }
}
