package dev.tenz.gui;

import dev.tenz.module.*;
import dev.tenz.module.Module;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

/** Sidebar (MODULES / GENERAL) + greeting header, clock, module search, rounded cards with toggle switches. */
public class TenzScreen extends Screen {
    private static final String[] MODULES = {"Tracker", "Auction", "HUD", "Visuals", "Fun", "Capes"};
    private static final String[] GENERAL = {"Appearance", "Audio", "Configs", "Settings"};
    private static final String[] SKIES = {"Galaxy", "Orbit", "Iris", "Velvet", "Twilight", "Silver"};
    private static final DateTimeFormatter CLOCK = DateTimeFormatter.ofPattern("h:mm a");
    private static final int W = 420, H = 258;

    private static String tab = "HUD";
    private static String query = "";
    private static boolean skyOpen = false;
    private EditBox search;
    private final List<Btn> btns = new ArrayList<>();
    private int lmx, lmy;

    public TenzScreen() { super(Component.literal("Tenz")); }

    private int left() { return width / 2 - W / 2; }
    private int top()  { return height / 2 - H / 2; }

    @Override
    protected void init() {
        search = new EditBox(font, left() + W - 118, top() + 12, 100, 10, Component.literal("Search"));
        search.setBordered(false);
        search.setHint(Component.literal("Search modules..."));
        search.setValue(query);
        search.setResponder(v -> query = v);
        addRenderableWidget(search);
    }

    /** Rounded rectangle (small radius, stepped corners). */
    private static void rr(GuiGraphicsExtractor g, int x, int y, int w, int h, int c) {
        g.fill(x + 2, y, x + w - 2, y + h, c);
        g.fill(x + 1, y + 1, x + w - 1, y + h - 1, c);
        g.fill(x, y + 2, x + w, y + h - 2, c);
    }

    private boolean over(int x, int y, int w, int h) { return lmx >= x && lmx <= x + w && lmy >= y && lmy <= y + h; }
    private static int soft(int c, int alpha) { return (c & 0xFFFFFF) | (alpha << 24); }

    private void toggle(GuiGraphicsExtractor g, Theme t, int x, int y, boolean on) {
        boolean hv = over(x - 4, y - 6, 32, 24);
        rr(g, x - 1, y - 1, 26, 14, on ? soft(t.accent(), hv ? 0xFF : 0xCC) : soft(t.dim(), 0x66));
        rr(g, x, y, 24, 12, on ? t.accent() : t.panel());
        int kx = on ? x + 14 : x + 2;
        rr(g, kx, y + 2, 8, 8, on ? 0xFF14141A : t.dim());
    }

    private void label(GuiGraphicsExtractor g, Theme t, String s, int x, int y) { g.text(font, s, x, y, t.dim()); }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mx, int my, float delta) {
        Theme t = Theme.current();
        btns.clear();
        lmx = mx; lmy = my;
        int x = left(), y = top();
        rr(g, x, y, W, H, t.bg());

        // ---- sidebar ----
        rr(g, x + 6, y + 6, 108, H - 12, t.panel());
        rr(g, x + 12, y + 12, 18, 18, t.accent());
        rr(g, x + 13, y + 13, 16, 16, 0xFF14141A);
        g.text(font, "T", x + 19, y + 17, t.text());
        g.text(font, "Tenz", x + 36, y + 13, t.text());
        g.text(font, "v0.1.0", x + 36, y + 23, t.dim());

        label(g, t, "MODULES", x + 14, y + 40);
        int iy = y + 52;
        for (String name : MODULES) { sideItem(g, t, name, x, iy); iy += 15; }
        label(g, t, "GENERAL", x + 14, iy + 6);
        iy += 18;
        for (String name : GENERAL) { sideItem(g, t, name, x, iy); iy += 15; }

        String player = minecraft != null && minecraft.getUser() != null ? minecraft.getUser().getName() : "Player";
        rr(g, x + 10, y + H - 30, 100, 20, t.sel());
        g.fill(x + 16, y + H - 24, x + 20, y + H - 20, t.good());
        g.text(font, "Tenz", x + 24, y + H - 26, t.dim());
        g.text(font, player, x + 16, y + H - 17, t.text());

        // ---- header ----
        int cx = x + 120, cw = W - 126;
        rr(g, cx, y + 6, cw, 20, t.panel());
        g.text(font, "Hello, " + player, cx + 8, y + 12, t.text());
        g.text(font, LocalTime.now().format(CLOCK).toLowerCase(), cx + 110, y + 12, t.dim());
        rr(g, x + W - 124, y + 8, 112, 16, t.sel());
        g.fill(cx, y + 29, cx + cw, y + 30, t.text());

        // ---- content ----
        int cy = y + 36;
        if (!query.isEmpty()) {
            String q = query.toLowerCase();
            for (Module m : Modules.ALL)
                if (m.name.toLowerCase().contains(q) || m.description.toLowerCase().contains(q)) cy = card(g, t, m, cx, cy, cw);
        } else if (tab.equals("Appearance")) {
            for (int i = 0; i < Theme.ALL.size(); i++) {
                Theme th = Theme.ALL.get(i);
                int idx = i;
                rr(g, cx, cy, cw, 24, th == t ? t.sel() : t.panel());
                g.text(font, th.name(), cx + 8, cy + 8, t.text());
                rr(g, cx + cw - 70, cy + 7, 14, 10, th.bg() | 0xFF000000);
                rr(g, cx + cw - 52, cy + 7, 14, 10, th.sel());
                rr(g, cx + cw - 34, cy + 7, 14, 10, th.accent());
                btns.add(new Btn(cx, cy, cw, 24, () -> { Modules.themeIndex = idx; Modules.save(); }));
                cy += 27;
            }
        } else if (Arrays.asList("Tracker", "Capes", "Audio", "Configs", "Settings").contains(tab)) {
            String s = tab + " is coming soon";
            g.text(font, s, cx + (cw - font.width(s)) / 2, y + H / 2, t.dim());
        } else {
            if (tab.equals("Auction")) {
                rr(g, cx, cy, cw, 24, t.panel());
                g.text(font, "Open auction menu", cx + 8, cy + 4, t.text());
                g.text(font, "Pick an item and start an auction", cx + 8, cy + 14, t.dim());
                rr(g, cx + cw - 32, cy + 6, 24, 12, t.accent());
                btns.add(new Btn(cx, cy, cw, 24, () -> minecraft.setScreenAndShow(new AuctionScreen())));
                cy += 27;
            }
            for (Module m : Modules.ALL) if (m.category.equals(tab)) cy = card(g, t, m, cx, cy, cw);
        }

        if (tab.equals("Fun") && query.isEmpty()) {
            String st = "Alien skin hook: " + (Modules.alienHooked ? "running" : "not triggered yet");
            g.text(font, st, cx + 4, y + H - 14, Modules.alienHooked ? t.good() : t.dim());
        }

        super.extractRenderState(g, mx, my, delta); // search box
    }

    private void sideItem(GuiGraphicsExtractor g, Theme t, String name, int x, int iy) {
        if (name.equals(tab) && query.isEmpty()) {
            rr(g, x + 10, iy - 3, 100, 14, t.accent());
            rr(g, x + 11, iy - 2, 98, 12, t.sel());
        } else if (over(x + 10, iy - 3, 100, 14)) {
            rr(g, x + 10, iy - 3, 100, 14, t.sel());
        }
        g.text(font, name, x + 18, iy, t.text());
        btns.add(new Btn(x + 10, iy - 3, 100, 14, () -> { tab = name; query = ""; if (search != null) search.setValue(""); }));
    }

    private int card(GuiGraphicsExtractor g, Theme t, Module m, int cx, int cy, int cw) {
        boolean hasSettings = m == Modules.SKY;
        boolean hv = over(cx, cy, cw, 24);
        rr(g, cx, cy, cw, 24, hv ? soft(t.accent(), 0x99) : t.sel());
        rr(g, cx + 1, cy + 1, cw - 2, 22, hv ? t.sel() : t.panel());
        g.text(font, m.name, cx + 8, cy + 4, t.text());
        g.text(font, m.description, cx + 8, cy + 14, t.dim());
        toggle(g, t, cx + cw - 32, cy + 6, m.enabled);
        btns.add(new Btn(cx + cw - 36, cy, 36, 24, () -> { m.enabled = !m.enabled; Modules.save(); }));
        cy += 27;
        if (hasSettings) {
            rr(g, cx, cy - 2, cw, 14, over(cx, cy - 2, cw, 14) ? soft(t.accent(), 0x99) : t.sel());
            String s = skyOpen ? "Settings -" : "Settings +";
            g.text(font, s, cx + (cw - font.width(s)) / 2, cy + 1, t.text());
            btns.add(new Btn(cx, cy - 2, cw, 14, () -> skyOpen = !skyOpen));
            cy += 16;
            if (skyOpen) {
                for (int i = 0; i < SKIES.length; i++) {
                    String name = SKIES[i];
                    int sx = cx + (i % 3) * (cw / 3), sy = cy + (i / 3) * 16;
                    boolean on = name.equals(Modules.skyPreset);
                    rr(g, sx + 2, sy, cw / 3 - 4, 13, on ? t.accent() : (over(sx + 2, sy, cw / 3 - 4, 13) ? t.sel() : t.panel()));
                    g.text(font, name, sx + 2 + (cw / 3 - 4 - font.width(name)) / 2, sy + 3, on ? 0xFF000000 : t.text());
                    btns.add(new Btn(sx + 2, sy, cw / 3 - 4, 13, () -> { Modules.skyPreset = name; Modules.save(); }));
                }
                cy += 34;
            }
        }
        return cy;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent e, boolean dbl) {
        for (Btn b : btns) if (b.hit(e.x(), e.y())) { b.action().run(); return true; }
        return super.mouseClicked(e, dbl);
    }
}
