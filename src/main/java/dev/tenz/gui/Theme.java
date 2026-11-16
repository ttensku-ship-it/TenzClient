package dev.tenz.gui;

import dev.tenz.module.Modules;
import java.util.List;

/** Colors used by the menu and every overlay. Pick one in Appearance. "Holographic" animates over time. */
public record Theme(String name, int bg, int panel, int sel, int accent, int text, int dim, int good) {
    /** Dark tinted window + one accent color; panel / selection are see-through versions of the accent. */
    private static Theme mk(String name, int bg, int accent, int dim, int good) {
        int rgb = accent & 0xFFFFFF;
        return new Theme(name, bg, 0x2C000000 | rgb, 0x58000000 | rgb, 0xFF000000 | rgb, 0xFFFFFFFF, dim, good);
    }

    public static final List<Theme> ALL = List.of(
        new Theme("Frost", 0x99101014, 0x2CFFFFFF, 0x58FFFFFF, 0xFFFFFFFF, 0xFFFFFFFF, 0xFF808088, 0xFF55E08A),
        new Theme("Purple", 0x991A0F2E, 0x2CB26BFF, 0x58B26BFF, 0xFFB26BFF, 0xFFFFFFFF, 0xFF9C88C0, 0xFF7CF0B0),
        new Theme("Ocean", 0x99081824, 0x2C4FC3F7, 0x584FC3F7, 0xFF4FC3F7, 0xFFFFFFFF, 0xFF7FA6BC, 0xFF55E08A),
        new Theme("Rose", 0x992A1018, 0x2CFF6B9D, 0x58FF6B9D, 0xFFFF6B9D, 0xFFFFFFFF, 0xFFC08898, 0xFF7CF0B0),
        new Theme("Emerald", 0x990A1A12, 0x2C3DDC84, 0x583DDC84, 0xFF3DDC84, 0xFFFFFFFF, 0xFF80A890, 0xFFB8FF8A),
        new Theme("Sunset", 0x992A140C, 0x2CFFA24C, 0x58FFA24C, 0xFFFFA24C, 0xFFFFFFFF, 0xFFC09C84, 0xFF8AF0A0),
        new Theme("Midnight", 0x99000000, 0x2C8A8AFF, 0x588A8AFF, 0xFF8A8AFF, 0xFFF0F0F0, 0xFF707070, 0xFF55E08A),
        // black & white frosted glass
        new Theme("Glass", 0x8C0A0A0A, 0x26FFFFFF, 0x4DFFFFFF, 0xFFFFFFFF, 0xFFFFFFFF, 0xFFA6A6A6, 0xFFE8E8E8),
        // animated: the colors drift through a soft rainbow-silver sheen (see holo())
        new Theme("Holographic", 0x99101018, 0x30FFFFFF, 0x5AFFFFFF, 0xFFCFE8FF, 0xFFFFFFFF, 0xFFB0B8D0, 0xFF9CFFE0),
        mk("Crimson", 0x99240A0F, 0xFF3B4E, 0xFFC08088, 0xFF7CF0B0),
        mk("Gold", 0x99221A08, 0xFFC53D, 0xFFC0AA78, 0xFFB8FF8A),
        mk("Mint", 0x99081E1A, 0x5CF2D0, 0xFF7FB5A8, 0xFF9CFF9C),
        mk("Lavender", 0x99181430, 0xC5B3FF, 0xFFA096C8, 0xFF7CF0B0),
        mk("Sky", 0x990A1830, 0x6BB6FF, 0xFF88A4C8, 0xFF7CF0B0),
        mk("Lime", 0x99121E08, 0xB6FF3B, 0xFF9CB478, 0xFFE0FF8A),
        mk("Cyber", 0x990A0A1A, 0xFF2BD6, 0xFFA088C8, 0xFF00F0FF),
        mk("Slate", 0x9914181E, 0x9FB3C8, 0xFF7F8A96, 0xFF8AF0A0),
        mk("Coffee", 0x99201510, 0xD2A679, 0xFFB09880, 0xFFB8FF8A),
        // black & white glass with drifting topographic lines and a silver sheen (drawn by HoloGlass)
        new Theme("Holo Glass", 0x8C050505, 0x24FFFFFF, 0x4AFFFFFF, 0xFFFFFFFF, 0xFFFFFFFF, 0xFFB4B4B4, 0xFFFFFFFF)
    );

    public static Theme current() {
        Theme t = ALL.get(Math.floorMod(Modules.themeIndex, ALL.size()));
        if (t.name.equals("Holographic")) return holo();
        if (t.name.equals("Holo Glass")) return holoGlass();
        return t;
    }

    /** What to draw in a preview swatch (animated for Holographic). */
    public Theme live() {
        if (name.equals("Holographic")) return holo();
        if (name.equals("Holo Glass")) return holoGlass();
        return this;
    }

    /** Black and white glass whose white shimmers very slightly. */
    private static Theme holoGlass() {
        float k = 0.5f + 0.5f * (float) Math.sin((System.currentTimeMillis() % 6000L) / 6000.0 * Math.PI * 2);
        int gray = 0xE8 + (int) (k * 0x17);
        int acc = 0xFF000000 | (gray << 16) | (gray << 8) | gray;
        int panel = ((0x20 + (int) (k * 0x0C)) << 24) | 0xFFFFFF;
        int sel = ((0x44 + (int) (k * 0x10)) << 24) | 0xFFFFFF;
        return new Theme("Holo Glass", 0x8C050505, panel, sel, acc, 0xFFFFFFFF, 0xFFB4B4B4, 0xFFFFFFFF);
    }

    /** Time based colors, so it looks the same at any FPS. */
    private static Theme holo() {
        float h = (System.currentTimeMillis() % 9000L) / 9000f;
        int accent = hsb(h, 0.42f, 1.00f, 0xFF);
        int panel  = hsb(h + 0.12f, 0.30f, 1.00f, 0x34);
        int sel    = hsb(h + 0.30f, 0.40f, 1.00f, 0x62);
        int bg     = hsb(h + 0.55f, 0.45f, 0.16f, 0x99);
        int dim    = hsb(h + 0.05f, 0.18f, 0.85f, 0xFF);
        int good   = hsb(h + 0.35f, 0.45f, 1.00f, 0xFF);
        return new Theme("Holographic", bg, panel, sel, accent, 0xFFFFFFFF, dim, good);
    }

    private static int hsb(float h, float s, float v, int a) {
        h = h - (float) Math.floor(h);
        float r, g, b;
        float x = h * 6f; int i = (int) Math.floor(x); float f = x - i;
        float p = v * (1 - s), q = v * (1 - s * f), t = v * (1 - s * (1 - f));
        switch (i % 6) {
            case 0 -> { r = v; g = t; b = p; }
            case 1 -> { r = q; g = v; b = p; }
            case 2 -> { r = p; g = v; b = t; }
            case 3 -> { r = p; g = q; b = v; }
            case 4 -> { r = t; g = p; b = v; }
            default -> { r = v; g = p; b = q; }
        }
        return (a << 24) | ((int) (r * 255) << 16) | ((int) (g * 255) << 8) | (int) (b * 255);
    }
}
