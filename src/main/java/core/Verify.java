package core;

/** Tiny assertion helper (replaces TestNG's Assert). A failed verify throws AssertionError. */
public final class Verify {
    private Verify() {}

    public static void isTrue(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    public static void equal(Object expected, Object actual, String message) {
        if (!expected.equals(actual)) {
            throw new AssertionError(message + " -> expected [" + expected + "] but found [" + actual + "]");
        }
    }

    public static void contains(String text, String part, String message) {
        if (!text.contains(part)) {
            throw new AssertionError(message + " -> [" + text + "] does not contain [" + part + "]");
        }
    }
}
