package com.austinv11.aspects.bridge;

/**
 * This represents a signal returned by hooks generated for weaving. This signal allows for determining behavior after
 * execution of the code block.
 *
 * <b>Note:</b> It is important to verify that this behavior is consistent with what the JVM expects a method to do
 * (like returning the appropriate return type, etc).
 */
public final class ExecutionSignal<R> {

    private final SignalType type;
    private final R toReturn;
    private final Throwable toThrow;

    public static ExecutionSignal<Void> returnVoid() {
        return new ExecutionSignal(SignalType.RETURN, null, null);
    }

    public static <R> ExecutionSignal<R> returnValue(R val) {
        return new ExecutionSignal<>(SignalType.RETURN_VALUE, val, null);
    }

    public static ExecutionSignal<Void> throwException(Throwable throwable) {
        return new ExecutionSignal(SignalType.THROW, null, throwable);
    }
//
//    public static ExecutionSignal breakLoop() {
//        return new ExecutionSignal(SignalType.BREAK, null, null);
//    }
//
//    public static ExecutionSignal continueLoop() {
//        return new ExecutionSignal(SignalType.CONTINUE, null, null);
//    }

    public static ExecutionSignal<Void> pass() {
        return new ExecutionSignal(SignalType.PASS, null, null);
    }

    private ExecutionSignal(SignalType type, R toReturn, Throwable toThrow) {
        this.type = type;
        this.toReturn = toReturn;
        this.toThrow = toThrow;
    }

    public SignalType getType() {
        return type;
    }

    public R getReturnValue() {
        return toReturn;
    }

    public Throwable getThrowable() {
        return toThrow;
    }

    public enum SignalType {
        RETURN, RETURN_VALUE, PASS, THROW
    }
}
