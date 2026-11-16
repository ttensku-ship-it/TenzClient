package dev.tenz.fun;

/** Your money, read from the chat line the server prints ("You have $ 13,619,634"). Payments add to it between updates. */
public final class Balance {
    public static double value = -1;        // -1 = not seen yet
    public static double sessionStart = -1; // first value seen this session
    public static long updatedMs = 0;
    /** Every change this session as {time ms, balance}, oldest first. The HUD tracker draws its graph from this. */
    public static final java.util.List<double[]> hist = new java.util.ArrayList<>();
    public static long startMs = 0;

    private static void record() {
        long now = System.currentTimeMillis();
        if (hist.isEmpty()) startMs = now;
        hist.add(new double[] {now, value});
        if (hist.size() > 4000) hist.subList(0, 1000).clear();
    }

    public static boolean known() { return value >= 0; }

    public static void set(double v) {
        if (sessionStart < 0) sessionStart = v;
        boolean changed = v != value;
        value = v;
        updatedMs = System.currentTimeMillis();
        if (changed || hist.isEmpty()) record();
    }

    /** A payment arrived: bump the shown balance until the server tells us the exact number. */
    public static void added(double v) {
        if (known()) { value += v; updatedMs = System.currentTimeMillis(); record(); }
    }

    public static double sessionChange() { return known() && sessionStart >= 0 ? value - sessionStart : 0; }

    public static String full(double v) { return String.format("$%,.0f", v); }

    private Balance() {}
}
