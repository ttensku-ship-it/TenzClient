package dev.tenz.gui;

import net.minecraft.client.gui.GuiGraphicsExtractor;

/** Small drawing helpers shared by the HUD: rounded boxes, outlines, color blending. */
public final class Draw {
    /** Rounded rectangle with stepped corners; rows never overlap so translucent colors stay even. */
    public static void rr(GuiGraphicsExtractor g, int x, int y, int w, int h, int c) {
        if (w < 5 || h < 5) { g.fill(x, y, x + Math.max(0, w), y + Math.max(0, h), c); return; }
        g.fill(x + 2, y, x + w - 2, y + 1, c);
        g.fill(x + 1, y + 1, x + w - 1, y + 2, c);
        g.fill(x, y + 2, x + w, y + h - 2, c);
        g.fill(x + 1, y + h - 2, x + w - 1, y + h - 1, c);
        g.fill(x + 2, y + h - 1, x + w - 2, y + h, c);
    }

    /** 1px rounded outline. */
    public static void ring(GuiGraphicsExtractor g, int x, int y, int w, int h, int c) {
        if (w < 5 || h < 5) return;
        g.fill(x + 2, y, x + w - 2, y + 1, c);
        g.fill(x + 2, y + h - 1, x + w - 2, y + h, c);
        g.fill(x, y + 2, x + 1, y + h - 2, c);
        g.fill(x + w - 1, y + 2, x + w, y + h - 2, c);
        g.fill(x + 1, y + 1, x + 2, y + 2, c);
        g.fill(x + w - 2, y + 1, x + w - 1, y + 2, c);
        g.fill(x + 1, y + h - 2, x + 2, y + h - 1, c);
        g.fill(x + w - 2, y + h - 2, x + w - 1, y + h - 1, c);
    }

    /** Multiplies a color's alpha by k (0..1). */
    public static int a(int c, float k) {
        int al = Math.max(0, Math.min(255, (int) ((c >>> 24) * k)));
        return (al << 24) | (c & 0xFFFFFF);
    }

    /** Blends two ARGB colors, k = 0 gives c1, k = 1 gives c2. */
    public static int lerp(int c1, int c2, float k) {
        k = Math.max(0f, Math.min(1f, k));
        int r = 0;
        for (int s = 24; s >= 0; s -= 8) {
            int a = (c1 >>> s) & 255, b = (c2 >>> s) & 255;
            r |= ((int) (a + (b - a) * k) & 255) << s;
        }
        return r;
    }
}
