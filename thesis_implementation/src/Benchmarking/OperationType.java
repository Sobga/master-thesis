package Benchmarking;

public enum OperationType {
    GET,
    SET,
    GROW,
    SHRINK;

    public static OperationType fromInt(int opIDX){
        return switch (opIDX) {
            case 0 -> GET;
            case 1 -> SET;
            case 2 -> GROW;
            case 3 -> SHRINK;
            default -> null;
        };
    }
}
