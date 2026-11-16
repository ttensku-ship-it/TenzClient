package dev.tenz.gui;

import net.minecraft.client.gui.GuiGraphicsExtractor;

/**
 * Background art for the "Holo Glass" theme: faint topographic contour lines that slowly drift, plus a soft
 * silver sheen that sweeps across the window. Black and white only. Based on real time, so it is smooth at any FPS.
 */
final class HoloGlass {
    private static final int STEP = 2;          // size of one sample, in menu pixels

    static void draw(GuiGraphicsExtractor g, int x, int y, int w, int h) {
        float tm = (System.currentTimeMillis() % 600_000L) / 1000f;

        // ---- sheen: a diagonal soft band sweeping left to right ----
        float sweep = ((tm * 0.12f) % 1.6f) - 0.3f;                   // band position across the window
        for (int sx = 0; sx < w; sx += 3) {
            float p = (float) sx / w;
            float d = Math.abs(p - sweep);
            if (d > 0.22f) continue;
            float k = 1f - d / 0.22f;
            int a = (int) (k * k * 22f);
            if (a > 0) g.fill(x + sx, y, x + sx + 3, y + h, (a << 24) | 0xFFFFFF);
        }

        // ---- topographic contour lines (every 4th one a bit stronger) ----
        float ox = tm * 3.0f, oy = tm * 2.0f;
        for (int sy = 0; sy < h; sy += STEP) {
            int runStart = -1, runAlpha = 0;
            for (int sx = 0; sx <= w; sx += STEP) {
                int a = 0;
                if (sx < w) {
                    float px = sx + ox, py = sy + oy;
                    float f = (float) (Math.sin(px * 0.031 + Math.sin(py * 0.021) * 1.6)
                                     + Math.sin(py * 0.037 - px * 0.012 + tm * 0.15)
                                     + 0.6 * Math.sin((px + py) * 0.019 - tm * 0.1));
                    float lv = f * 2.2f;                                  // contour spacing
                    float fr = lv - (float) Math.floor(lv);
                    float dist = Math.min(fr, 1f - fr);
                    if (dist < 0.07f) a = (((int) Math.floor(lv)) & 3) == 0 ? 0x28 : 0x13;
                }
                if (a != 0 && runStart < 0) { runStart = sx; runAlpha = a; }
                else if ((a == 0 || a != runAlpha) && runStart >= 0) {
                    g.fill(x + runStart, y + sy, x + sx, y + sy + STEP, (runAlpha << 24) | 0xFFFFFF);
                    runStart = a != 0 ? sx : -1; runAlpha = a;
                }
            }
        }
    }

    private HoloGlass() {}
}
