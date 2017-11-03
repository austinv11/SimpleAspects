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

    /**
     * This generates a signal representing a return statement with no return value associated with it.
     *
     * @return The signal.
     */
    public static ExecutionSignal<Void> returnVoid() {
        return new ExecutionSignal(SignalType.RETURN, null, null);
    }

    /**
     * This generates a signal representing a return statement with a return value associated with it.
     *
     * @return The signal.
     */
    public static <R> ExecutionSignal<R> returnValue(R val) {
        return new ExecutionSignal<>(SignalType.RETURN_VALUE, val, null);
    }

    /**
     * This generates a signal representing an exception throw statement.
     *
     * @return The signal.
     */
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

    /**
     * This generates a simple pass signal, meaning no side effects will be produced.
     *
     * @return The signal.
     */
    public static ExecutionSignal<Void> pass() {
        return new ExecutionSignal(SignalType.PASS, null, null);
    }

    private ExecutionSignal(SignalType type, R toReturn, Throwable toThrow) {
        this.type = type;
        this.toReturn = toReturn;
        this.toThrow = toThrow;
    }

    /**
     * Gets the type of signal this represents.
     *
     * @return The signal type.
     */
    public SignalType getType() {
        return type;
    }

    /**
     * Gets the return value for the signal if such a value exists.
     *
     * @return The return value.
     */
    public R getReturnValue() {
        return toReturn;
    }

    /**
     * Gets the throwable thrown for the signal if the throwable exists.
     *
     * @return The throwable.
     */
    public Throwable getThrowable() {
        return toThrow;
    }

    /**
     * This represents all the types of possible signals an {@link ExecutionSignal} object can represent.
     */
    public enum SignalType {
        RETURN, RETURN_VALUE, PASS, THROW
    }
}
