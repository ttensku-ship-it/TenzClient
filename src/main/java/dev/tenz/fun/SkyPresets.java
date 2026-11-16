package dev.tenz.fun;

/** Colors for the custom sky presets. Shared by the sky mixin and the menu preview. */
public final class SkyPresets {
    public static final String[] NAMES = {"Galaxy", "Orbit", "Iris", "Velvet", "Twilight", "Silver"};
    // color A r,g,b | color B r,g,b | star brightness
    public static final float[][] PRESETS = {
        {0.10f, 0.04f, 0.28f,  0.30f, 0.05f, 0.45f, 1.0f},
        {0.02f, 0.05f, 0.20f,  0.05f, 0.20f, 0.42f, 1.0f},
        {0.28f, 0.14f, 0.60f,  0.45f, 0.25f, 0.80f, 0.8f},
        {0.30f, 0.02f, 0.14f,  0.50f, 0.05f, 0.25f, 0.9f},
        {0.95f, 0.45f, 0.30f,  0.35f, 0.20f, 0.55f, 0.7f},
        {0.55f, 0.60f, 0.68f,  0.75f, 0.78f, 0.85f, 0.6f},
    };

    public static int index(String name) {
        for (int i = 0; i < NAMES.length; i++) if (NAMES[i].equals(name)) return i;
        return 0;
    }

    /** k in 0..1 blends color A to color B. Returns {r,g,b}. */
    public static float[] color(int idx, float k) {
        float[] p = PRESETS[idx];
        return new float[] {p[0] + (p[3] - p[0]) * k, p[1] + (p[4] - p[1]) * k, p[2] + (p[5] - p[2]) * k};
    }

    /** Slow 40 second drift, based on real time. */
    public static float drift() {
        float t = (float) ((System.currentTimeMillis() % 40000L) / 40000.0 * Math.PI * 2.0);
        return 0.5f + 0.5f * (float) Math.sin(t);
    }

    private SkyPresets() {}
}
