package dev.tenz.gui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;

/** All Tenz text goes through here so it uses the font picked in Appearance. */
public final class Tx {
    /** Fonts you can pick in Appearance. Each name is a file in assets/tenz/font (f_<name>.json). */
    public static final String[] FONTS = {"Poppins", "Inter", "Outfit", "Work Sans", "Jura", "Minecraft"};
    private static final String[] IDS = {"f_poppins", "f_inter", "f_outfit", "f_worksans", "f_jura", "f_minecraft"};
    private static final Style[] STYLES = new Style[IDS.length];

    private static Style style() {
        int i = Math.floorMod(dev.tenz.module.Modules.fontIndex, IDS.length);
        if (STYLES[i] == null) STYLES[i] = Style.EMPTY.withFont(
            new FontDescription.Resource(Identifier.fromNamespaceAndPath("tenz", IDS[i])));
        return STYLES[i];
    }

    public static Component c(String s) { return Component.literal(s).setStyle(style()); }
    /** Menus: clean text, no drop shadow. */
    public static void draw(GuiGraphicsExtractor g, Font f, String s, int x, int y, int color) { g.text(f, c(s), x, y, color, false); }
    /** HUD overlays: keeps a soft shadow so text stays readable over the world. */
    public static void hud(GuiGraphicsExtractor g, Font f, String s, int x, int y, int color) { g.text(f, c(s), x, y, color, true); }
    public static int w(Font f, String s) { return f.width(c(s)); }
}
