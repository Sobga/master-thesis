package resizableArrays;

import memory.WordCountable;

import java.util.Iterator;

public interface ResizableArray<T> extends WordCountable, Iterable<T> {
    int length();
    T get(int i);
    void set(int i, T a);
    void grow(T a);
    T shrink();
    void clear();

    String getName();

    default long countedGrow(T a){return 0;}
    default long countedShrink() {return 0;}

    // Returns the last element in the array
    default T last(){return get(length() - 1);}
    default DataBlock<T>[] allocateDatablocks(int size){
        return (DataBlock<T>[]) new DataBlock[size];
    }

    @Override
    default Iterator<T> iterator() {
        return new Iterator<>() {
            private int i = 0;
            private final int n = length();
            @Override
            public boolean hasNext() {
                return i < n;
            }

            @Override
            public T next() {
                return get(i++);
            }
        };
    }
}


