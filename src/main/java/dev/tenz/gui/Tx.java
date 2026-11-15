package dev.tenz.gui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;

/** All Tenz text goes through here so it uses the Poppins font (assets/tenz/font/ui.json). */
public final class Tx {
    private static final Style STYLE = Style.EMPTY.withFont(
        new FontDescription.Resource(Identifier.fromNamespaceAndPath("tenz", "ui")));

    public static Component c(String s) { return Component.literal(s).setStyle(STYLE); }
    /** Menus: clean text, no drop shadow. */
    public static void draw(GuiGraphicsExtractor g, Font f, String s, int x, int y, int color) { g.text(f, c(s), x, y, color, false); }
    /** HUD overlays: keeps a soft shadow so text stays readable over the world. */
    public static void hud(GuiGraphicsExtractor g, Font f, String s, int x, int y, int color) { g.text(f, c(s), x, y, color, true); }
    public static int w(Font f, String s) { return f.width(c(s)); }
}
