package ResizableArrays;

public interface ResizableArray<T> {
    int length();
    T get(int i);
    void set(int i, T a);
    void grow(T a);
    T shrink();
    void clear();

    //
    String getName();

    // Returns the last element in the array
    default T last(){return get(length() - 1);}
    default T[] createTypedArray(int size){
        return (T[]) new Object[size];
    }
}


