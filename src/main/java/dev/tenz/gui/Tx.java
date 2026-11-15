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
    public static void draw(GuiGraphicsExtractor g, Font f, String s, int x, int y, int color) { Tx.draw(g, f, c(s), x, y, color); }
    public static int w(Font f, String s) { return f.width(c(s)); }
}
