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
                return -1;
            }
            case GROW -> {
                array.grow(data);
                return -1;
            }
            case SHRINK -> {
                return array.shrink();
            }
        }
        return -1;
    }

    @Override
    public String toString() {
        return "OP{" + op + '}';
    }
}
