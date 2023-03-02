package benchmarking;

import resizableArrays.ResizableArray;

public class Operation {
    OperationType op;
    int idx;
    int data;

    public Operation(OperationType op, int idx, int data) {
        this.op = op;
        this.idx = idx;
        this.data = data;
    }

    public int applyOperation(ResizableArray<Integer> array) {
        switch (op) {
            case GET -> {
                return array.get(idx);
            }
            case SET -> {
                array.set(idx, data);
            }
            case GROW -> {
                array.grow(data);
            }
            case SHRINK -> {
                return array.shrink();
            }
        }
        return -1;
    }
}
