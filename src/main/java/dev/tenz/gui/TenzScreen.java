package dev.tenz.gui;

import dev.tenz.fun.Payments;
import dev.tenz.hud.AuctionState;
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
    private static final String[] MODULES = {"Tracker", "Auction", "HUD", "Visuals", "Knives", "Fun"};
    private static final String[] GENERAL = {"Appearance", "Audio", "Configs", "Settings"};
    private static final String[] SKIES = {"Galaxy", "Orbit", "Iris", "Velvet", "Twilight", "Silver"};
    private static final DateTimeFormatter CLOCK = DateTimeFormatter.ofPattern("h:mm a");
    private static final int W = 420, H = 276;

    private static String tab = "HUD";
    private static String view = "Tracker";
    private static String range = "Week";
    private static String query = "";
    private static boolean skyOpen = false;
    private EditBox search;
    private final List<Btn> btns = new ArrayList<>();
    private int lmx, lmy;

    public TenzScreen() { super(Component.literal("Tenz")); }

    /** Menu size from Settings, shrunk if the window is too small to fit it. */
    private float sc() {
        float want = Modules.uiScale / 100f;
        float fit = Math.min(width / (W + 10f), height / (H + 10f));
        return Math.max(0.5f, Math.min(want, fit));
    }
    private int left() { return (int) (width / sc()) / 2 - W / 2; }
    private int top()  { return (int) (height / sc()) / 2 - H / 2; }

    @Override
    protected void init() {
        search = new EditBox(font, left() + W - 118, top() + 12, 100, 10, Component.literal("Search"));
        search.setBordered(false);
        search.setHint(Component.literal("Search modules..."));
        search.setValue(query);
        search.setResponder(v -> query = v);
        addRenderableWidget(search);
    }

    /** Rounded rectangle (small radius, stepped corners). Rows never overlap so translucent colors stay even. */
    private static void rr(GuiGraphicsExtractor g, int x, int y, int w, int h, int c) {
        g.fill(x + 2, y, x + w - 2, y + 1, c);
        g.fill(x + 1, y + 1, x + w - 1, y + 2, c);
        g.fill(x, y + 2, x + w, y + h - 2, c);
        g.fill(x + 1, y + h - 2, x + w - 1, y + h - 1, c);
        g.fill(x + 2, y + h - 1, x + w - 2, y + h, c);
    }

    /** 1px rounded outline. */
    private static void ring(GuiGraphicsExtractor g, int x, int y, int w, int h, int c) {
        g.fill(x + 2, y, x + w - 2, y + 1, c);
        g.fill(x + 2, y + h - 1, x + w - 2, y + h, c);
        g.fill(x, y + 2, x + 1, y + h - 2, c);
        g.fill(x + w - 1, y + 2, x + w, y + h - 2, c);
        g.fill(x + 1, y + 1, x + 2, y + 2, c);
        g.fill(x + w - 2, y + 1, x + w - 1, y + 2, c);
        g.fill(x + 1, y + h - 2, x + 2, y + h - 1, c);
        g.fill(x + w - 2, y + h - 2, x + w - 1, y + h - 1, c);
    }

    /** Glass window: tinted body, soft light sheen fading down from the top, bright edge. */
    private static void glass(GuiGraphicsExtractor g, int x, int y, int w, int h, int tint) {
        rr(g, x, y, w, h, tint);
        for (int i = 0; i < 56; i++) {
            int a = (int) (0x2A * (1.0 - i / 56.0));
            if (a > 0) g.fill(x + 2, y + 2 + i, x + w - 2, y + 3 + i, (a << 24) | 0xFFFFFF);
        }
        ring(g, x, y, w, h, 0x55FFFFFF);
    }

    private boolean over(int x, int y, int w, int h) { return lmx >= x && lmx <= x + w && lmy >= y && lmy <= y + h; }
    private static int soft(int c, int alpha) { return (c & 0xFFFFFF) | (alpha << 24); }

    private void toggle(GuiGraphicsExtractor g, Theme t, int x, int y, boolean on) {
        boolean hv = over(x - 4, y - 6, 32, 24);
        rr(g, x, y, 24, 12, on ? t.accent() : 0x40FFFFFF);
        if (hv) ring(g, x - 1, y - 1, 26, 14, soft(t.accent(), 0xCC));
        int kx = on ? x + 14 : x + 2;
        rr(g, kx, y + 2, 8, 8, on ? 0xFF14141A : 0xFFE8E8F0);
    }

    private void label(GuiGraphicsExtractor g, Theme t, String s, int x, int y) { Tx.draw(g, font, s, x, y, t.dim()); }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mx, int my, float delta) {
        Theme t = Theme.current();
        btns.clear();
        float sc = sc();
        lmx = (int) (mx / sc); lmy = (int) (my / sc);
        search.setX(left() + W - 118); search.setY(top() + 12);
        var pose = g.pose();
        pose.pushMatrix();
        pose.scale(sc, sc);
        int x = left(), y = top();
        glass(g, x, y, W, H, t.bg());
        if (t.name().equals("Holo Glass")) HoloGlass.draw(g, x, y, W, H);

        // ---- sidebar ----
        rr(g, x + 6, y + 6, 108, H - 12, t.panel());
        ring(g, x + 6, y + 6, 108, H - 12, 0x22FFFFFF);
        rr(g, x + 12, y + 12, 18, 18, t.accent());
        rr(g, x + 13, y + 13, 16, 16, 0xFF14141A);
        Tx.draw(g, font, "T", x + 19, y + 17, t.text());
        Tx.draw(g, font, "Tenz", x + 36, y + 13, t.text());
        Tx.draw(g, font, "v0.5.0", x + 36, y + 23, t.dim());

        label(g, t, "MODULES", x + 14, y + 40);
        int iy = y + 52;
        for (String name : MODULES) { sideItem(g, t, name, x, iy); iy += 15; }
        label(g, t, "GENERAL", x + 14, iy + 6);
        iy += 18;
        for (String name : GENERAL) { sideItem(g, t, name, x, iy); iy += 15; }

        String player = minecraft != null && minecraft.getUser() != null ? minecraft.getUser().getName() : "Player";
        rr(g, x + 10, y + H - 30, 100, 20, t.sel());
        g.fill(x + 16, y + H - 24, x + 20, y + H - 20, t.good());
        Tx.draw(g, font, "Tenz", x + 24, y + H - 26, t.dim());
        Tx.draw(g, font, player, x + 16, y + H - 17, t.text());

        // ---- header ----
        int cx = x + 120, cw = W - 126;
        rr(g, cx, y + 6, cw, 20, t.panel());
        Tx.draw(g, font, "Hello, " + player, cx + 8, y + 12, t.text());
        Tx.draw(g, font, LocalTime.now().format(CLOCK).toLowerCase(), cx + 110, y + 12, t.dim());
        rr(g, x + W - 124, y + 8, 112, 16, t.sel());
        g.fill(cx, y + 29, cx + cw, y + 30, t.text());

        // ---- content ----
        int cy = y + 36;
        if (!query.isEmpty()) {
            String q = query.toLowerCase();
            for (Module m : Modules.ALL)
                if (m.name.toLowerCase().contains(q) || m.description.toLowerCase().contains(q)) cy = card(g, t, m, cx, cy, cw);
        } else if (tab.equals("Appearance")) {
            int cols = 3, gap = 4, cell = (cw - gap * (cols - 1)) / cols;
            for (int i = 0; i < Theme.ALL.size(); i++) {
                Theme th = Theme.ALL.get(i).live();
                int idx = i;
                int bx = cx + (i % cols) * (cell + gap), by = cy + (i / cols) * 28;
                boolean on = th.name().equals(t.name());
                rr(g, bx, by, cell, 24, on ? t.sel() : (over(bx, by, cell, 24) ? t.sel() : t.panel()));
                ring(g, bx, by, cell, 24, on ? soft(t.accent(), 0xEE) : 0x22FFFFFF);
                rr(g, bx + 6, by + 7, 10, 10, th.accent());
                rr(g, bx + 18, by + 7, 4, 10, th.bg() | 0xFF000000);
                Tx.draw(g, font, th.name(), bx + 27, by + 8, t.text());
                btns.add(new Btn(bx, by, cell, 24, () -> { Modules.themeIndex = idx; Modules.save(); }));
            }
            // font picker (applies to the menu and the HUD)
            int fy = cy + ((Theme.ALL.size() + cols - 1) / cols) * 28 + 4;
            Tx.draw(g, font, "Font", cx, fy, t.dim());
            String[] fonts = Tx.FONTS;
            int fw = (cw - 4 * (fonts.length - 1)) / fonts.length;
            for (int i = 0; i < fonts.length; i++) {
                int idx = i;
                int bx = cx + i * (fw + 4), by = fy + 12;
                boolean on = i == Math.floorMod(Modules.fontIndex, fonts.length);
                rr(g, bx, by, fw, 18, on ? t.accent() : (over(bx, by, fw, 18) ? t.sel() : t.panel()));
                String n = fonts[i];
                Tx.draw(g, font, n, bx + (fw - Tx.w(font, n)) / 2, by + 5, on ? 0xFF000000 : t.text());
                btns.add(new Btn(bx, by, fw, 18, () -> { Modules.fontIndex = idx; Modules.save(); }));
            }
        } else if (Arrays.asList("Audio", "Configs").contains(tab)) {
            String s = tab + " is coming soon";
            Tx.draw(g, font, s, cx + (cw - Tx.w(font, s)) / 2, y + H / 2, t.dim());
        } else if (tab.equals("Tracker")) {
            tracker(g, t, cx, cy, cw);
        } else if (tab.equals("Settings")) {
            settings(g, t, cx, cy, cw);
        } else {
            if (tab.equals("Auction")) {
                rr(g, cx, cy, cw, 24, t.panel());
                Tx.draw(g, font, "Open auction menu", cx + 8, cy + 4, t.text());
                Tx.draw(g, font, "Pick an item and start an auction", cx + 8, cy + 14, t.dim());
                rr(g, cx + cw - 32, cy + 6, 24, 12, t.accent());
                btns.add(new Btn(cx, cy, cw, 24, () -> minecraft.setScreenAndShow(new AuctionScreen())));
                cy += 27;
            }
            for (Module m : Modules.ALL) if (m.category.equals(tab)) cy = card(g, t, m, cx, cy, cw);
            if (tab.equals("Knives")) knives(g, t, cx, cy, cw);
            if (tab.equals("HUD")) hudPreview(g, t, cx, cy + 2, cw);
            if (tab.equals("Visuals")) skyPreview(g, t, cx, cy + 2, cw);
        }

        if (tab.equals("Fun") && query.isEmpty()) {
            String st = "Alien skin hook: " + (Modules.alienHooked ? "running" : "not triggered yet");
            Tx.draw(g, font, st, cx + 4, y + H - 14, Modules.alienHooked ? t.good() : t.dim());
        }

        super.extractRenderState(g, lmx, lmy, delta); // search box
        pose.popMatrix();
    }

    private void sideItem(GuiGraphicsExtractor g, Theme t, String name, int x, int iy) {
        if (name.equals(tab) && query.isEmpty()) {
            rr(g, x + 10, iy - 3, 100, 14, t.sel());
            ring(g, x + 10, iy - 3, 100, 14, soft(t.accent(), 0xEE));
        } else if (over(x + 10, iy - 3, 100, 14)) {
            rr(g, x + 10, iy - 3, 100, 14, t.sel());
        }
        Tx.draw(g, font, name, x + 18, iy, t.text());
        btns.add(new Btn(x + 10, iy - 3, 100, 14, () -> { tab = name; query = ""; if (search != null) search.setValue(""); }));
    }

    private int card(GuiGraphicsExtractor g, Theme t, Module m, int cx, int cy, int cw) {
        boolean hasSettings = m == Modules.SKY;
        boolean hv = over(cx, cy, cw, 24);
        rr(g, cx, cy, cw, 24, hv ? t.sel() : t.panel());
        ring(g, cx, cy, cw, 24, hv ? soft(t.accent(), 0x99) : 0x22FFFFFF);
        Tx.draw(g, font, m.name, cx + 8, cy + 4, t.text());
        Tx.draw(g, font, m.description, cx + 8, cy + 14, t.dim());
        toggle(g, t, cx + cw - 32, cy + 6, m.enabled);
        btns.add(new Btn(cx + cw - 36, cy, 36, 24, () -> { m.enabled = !m.enabled; Modules.save(); }));
        cy += 27;
        if (hasSettings) {
            rr(g, cx, cy - 2, cw, 14, over(cx, cy - 2, cw, 14) ? soft(t.accent(), 0x99) : t.sel());
            String s = skyOpen ? "Settings -" : "Settings +";
            Tx.draw(g, font, s, cx + (cw - Tx.w(font, s)) / 2, cy + 1, t.text());
            btns.add(new Btn(cx, cy - 2, cw, 14, () -> skyOpen = !skyOpen));
            cy += 16;
            if (skyOpen) {
                for (int i = 0; i < SKIES.length; i++) {
                    String name = SKIES[i];
                    int sx = cx + (i % 3) * (cw / 3), sy = cy + (i / 3) * 16;
                    boolean on = name.equals(Modules.skyPreset);
                    rr(g, sx + 2, sy, cw / 3 - 4, 13, on ? t.accent() : (over(sx + 2, sy, cw / 3 - 4, 13) ? t.sel() : t.panel()));
                    Tx.draw(g, font, name, sx + 2 + (cw / 3 - 4 - Tx.w(font, name)) / 2, sy + 3, on ? 0xFF000000 : t.text());
                    btns.add(new Btn(sx + 2, sy, cw / 3 - 4, 13, () -> { Modules.skyPreset = name; Modules.save(); }));
                }
                cy += 34;
            }
        }
        return cy;
    }

    /** HUD tab footer: live numbers, so the tab is never just a few switches. */
    private void hudPreview(GuiGraphicsExtractor g, Theme t, int cx, int cy, int cw) {
        rr(g, cx, cy, cw, 64, t.panel());
        ring(g, cx, cy, cw, 64, 0x22FFFFFF);
        Tx.draw(g, font, "Live preview", cx + 8, cy + 6, t.dim());
        Tx.draw(g, font, "Auto balance", cx + 8, cy + 47, t.dim());
        String[] ch = dev.tenz.fun.BalancePoll.CHOICES;
        for (int i = 0; i < ch.length; i++) {
            String n = ch[i];
            choice(g, t, cx + 76 + i * 52, cy + 43, 48, 16, n, n.equals(Modules.autoBal), () -> { Modules.autoBal = n; dev.tenz.fun.BalancePoll.reset(); Modules.save(); });
        }
        String bal = dev.tenz.fun.Balance.known() ? dev.tenz.fun.Balance.full(dev.tenz.fun.Balance.value) : "not seen yet";
        Tx.draw(g, font, "Balance", cx + 8, cy + 20, t.dim());
        Tx.draw(g, font, bal, cx + 8, cy + 30, t.text());
        double d = dev.tenz.fun.Balance.sessionChange();
        boolean kn = dev.tenz.fun.Balance.known();
        String tr = !kn ? "-" : (d >= 0 ? "+" : "-") + AuctionState.money(Math.abs(d));
        Tx.draw(g, font, "This session", cx + cw / 2, cy + 20, t.dim());
        Tx.draw(g, font, tr, cx + cw / 2, cy + 30, !kn ? t.dim() : d > 0 ? t.good() : d < 0 ? 0xFFFF5A5A : t.text());
    }

    /** Visuals tab footer: the selected sky as a color strip that drifts like the real one. */
    private void skyPreview(GuiGraphicsExtractor g, Theme t, int cx, int cy, int cw) {
        rr(g, cx, cy, cw, 44, t.panel());
        ring(g, cx, cy, cw, 44, 0x22FFFFFF);
        Tx.draw(g, font, "Sky preview: " + Modules.skyPreset, cx + 8, cy + 6, t.dim());
        int idx = dev.tenz.fun.SkyPresets.index(Modules.skyPreset);
        int bw = cw - 16;
        for (int i = 0; i < bw; i++) {
            float k = (float) i / bw;
            float[] c = dev.tenz.fun.SkyPresets.color(idx, k);
            int col = 0xFF000000 | ((int) (c[0] * 255) << 16) | ((int) (c[1] * 255) << 8) | (int) (c[2] * 255);
            g.fill(cx + 8 + i, cy + 20, cx + 9 + i, cy + 36, col);
        }
        int mk = cx + 8 + (int) (dev.tenz.fun.SkyPresets.drift() * (bw - 2));
        g.fill(mk, cy + 18, mk + 2, cy + 38, 0xFFFFFFFF);
    }

    /** Small row of choice buttons. Returns nothing; clicking runs the setter. */
    private void choice(GuiGraphicsExtractor g, Theme t, int x, int y, int w, int h, String label, boolean on, Runnable pick) {
        rr(g, x, y, w, h, on ? t.accent() : (over(x, y, w, h) ? t.sel() : soft(t.accent(), 0x22)));
        Tx.draw(g, font, label, x + (w - Tx.w(font, label)) / 2, y + (h - 8) / 2, on ? 0xFF000000 : t.text());
        btns.add(new Btn(x, y, w, h, pick));
    }

    /** Knives tab: which knife model you hold, its finish, and the trick keys. */
    private void knives(GuiGraphicsExtractor g, Theme t, int cx, int cy, int cw) {
        // ---- model ----
        rr(g, cx, cy, cw, 104, t.panel());
        ring(g, cx, cy, cw, 104, 0x22FFFFFF);
        Tx.draw(g, font, "Knife model", cx + 8, cy + 6, t.text());
        Tx.draw(g, font, "Only you see it. Hold a sword or knife.", cx + cw - 8 - Tx.w(font, "Only you see it. Hold a sword or knife."), cy + 6, t.dim());
        String[] models = dev.tenz.fun.KnifeSkins.MODELS;
        int per = 4, bw = (cw - 16 - 4 * (per - 1)) / per;
        for (int i = 0; i < models.length; i++) {
            String n = models[i];
            int bx = cx + 8 + (i % per) * (bw + 4), by = cy + 19 + (i / per) * 19;
            choice(g, t, bx, by, bw, 16, n, n.equals(Modules.knifeModel), () -> { Modules.knifeModel = n; Modules.save(); });
        }
        Tx.draw(g, font, "Finish", cx + 8, cy + 59, t.dim());
        String[] fins = dev.tenz.fun.KnifeSkins.FINISHES;
        int fw = (cw - 16 - 3 * (fins.length - 1)) / fins.length;
        for (int i = 0; i < fins.length; i++) {
            String n = fins[i];
            choice(g, t, cx + 8 + i * (fw + 3), cy + 71, fw, 16, n, n.equals(Modules.knifeFinish), () -> { Modules.knifeFinish = n; Modules.save(); });
        }
        Tx.draw(g, font, "Applies to", cx + 8, cy + 91, t.dim());
        String[] ap = dev.tenz.fun.KnifeSkins.APPLY;
        for (int i = 0; i < ap.length; i++) {
            String n = ap[i];
            int aw = Tx.w(font, n) + 16;
            int ax = cx + 62 + (i == 0 ? 0 : Tx.w(font, ap[0]) + 20);
            choice(g, t, ax, cy + 87, aw, 15, n, n.equals(Modules.knifeApply), () -> { Modules.knifeApply = n; Modules.save(); });
        }

        // ---- tricks ----
        int ty = cy + 110;
        rr(g, cx, ty, cw, 62, t.panel());
        ring(g, cx, ty, cw, 62, 0x22FFFFFF);
        Tx.draw(g, font, "Trick", cx + 8, ty + 6, t.text());
        Tx.draw(g, font, "R plays it, V plays a random one", cx + cw - 8 - Tx.w(font, "R plays it, V plays a random one"), ty + 6, t.dim());
        String[] tricks = dev.tenz.fun.Tricks.NAMES;
        int tw = (cw - 16 - 4 * (tricks.length - 1)) / tricks.length;
        for (int i = 0; i < tricks.length; i++) {
            String n = tricks[i];
            choice(g, t, cx + 8 + i * (tw + 4), ty + 19, tw, 15, n, n.equals(Modules.knifeTrick), () -> { Modules.knifeTrick = n; Modules.save(); });
        }
        Tx.draw(g, font, "Speed", cx + 8, ty + 42, t.dim());
        String[] speeds = dev.tenz.fun.Tricks.SPEEDS;
        for (int i = 0; i < speeds.length; i++) {
            String n = speeds[i];
            choice(g, t, cx + 50 + i * 54, ty + 38, 50, 15, n, n.equals(Modules.knifeSpeed), () -> { Modules.knifeSpeed = n; Modules.save(); });
        }
    }

    private void settings(GuiGraphicsExtractor g, Theme t, int cx, int cy, int cw) {
        rr(g, cx, cy, cw, 54, t.panel());
        ring(g, cx, cy, cw, 54, 0x22FFFFFF);
        Tx.draw(g, font, "Menu size", cx + 8, cy + 7, t.text());
        Tx.draw(g, font, "Makes the whole Tenz menu bigger or smaller", cx + 8, cy + 18, t.dim());
        int[] opts = {100, 125, 150};
        for (int i = 0; i < 3; i++) {
            int pct = opts[i];
            int bx = cx + 8 + i * 64;
            boolean on = Modules.uiScale == pct;
            rr(g, bx, cy + 32, 60, 15, on ? t.accent() : (over(bx, cy + 32, 60, 15) ? t.sel() : soft(t.accent(), 0x22)));
            String l = pct + "%";
            Tx.draw(g, font, l, bx + (60 - Tx.w(font, l)) / 2, cy + 35, on ? 0xFF000000 : t.text());
            btns.add(new Btn(bx, cy + 32, 60, 15, () -> { Modules.uiScale = pct; Modules.save(); }));
        }
    }

    // ---------------- Tracker tab ----------------
    private static final String[] VIEWS = {"Tracker", "Payments", "Orders"};
    private static final String[] RANGES = {"Stream", "Day", "Week"};

    private void tracker(GuiGraphicsExtractor g, Theme t, int cx, int cy, int cw) {
        // view switcher
        rr(g, cx, cy, cw, 22, t.panel());
        ring(g, cx, cy, cw, 22, 0x22FFFFFF);
        int sw = (cw - 8) / 3;
        for (int i = 0; i < 3; i++) {
            String v = VIEWS[i];
            int sx = cx + 4 + i * sw;
            boolean on = v.equals(view);
            if (on) rr(g, sx, cy + 3, sw, 16, t.accent());
            else if (over(sx, cy + 3, sw, 16)) rr(g, sx, cy + 3, sw, 16, t.sel());
            Tx.draw(g, font, v, sx + (sw - Tx.w(font, v)) / 2, cy + 7, on ? 0xFF000000 : t.text());
            btns.add(new Btn(sx, cy + 3, sw, 16, () -> view = v));
        }
        cy += 26;

        if (view.equals("Payments")) { paymentsList(g, t, cx, cy, cw); return; }
        if (view.equals("Orders")) {
            String s = "No orders yet";
            Tx.draw(g, font, s, cx + (cw - Tx.w(font, s)) / 2, cy + 80, t.dim());
            return;
        }

        // range + total
        rr(g, cx, cy, cw, 156, t.panel());
        ring(g, cx, cy, cw, 156, 0x22FFFFFF);
        for (int i = 0; i < 3; i++) {
            String r = RANGES[i];
            int bx = cx + 8 + i * 46;
            boolean on = r.equals(range);
            rr(g, bx, cy + 7, 42, 14, on ? t.accent() : (over(bx, cy + 7, 42, 14) ? t.sel() : soft(t.accent(), 0x22)));
            Tx.draw(g, font, r, bx + (42 - Tx.w(font, r)) / 2, cy + 10, on ? 0xFF000000 : t.dim());
            btns.add(new Btn(bx, cy + 7, 42, 14, () -> range = r));
        }

        long now = System.currentTimeMillis();
        long span = switch (range) { case "Stream" -> Math.max(60_000L, now - Payments.sessionStart); case "Day" -> 86_400_000L; default -> 7 * 86_400_000L; };
        long from = range.equals("Stream") ? Payments.sessionStart : now - span;
        final int N = 60;
        double[] cum = new double[N + 1];
        int count = 0; double biggest = 0, total = 0;
        for (Payments.Pay p : Payments.history) {
            if (p.t() < from) continue;
            count++; total += p.amount(); biggest = Math.max(biggest, p.amount());
            int b = (int) Math.min(N, Math.max(0, (p.t() - from) * N / span));
            cum[b] += p.amount();
        }
        for (int i = 1; i <= N; i++) cum[i] += cum[i - 1];

        // big number (scaled text)
        String big = "+" + AuctionState.money(total);
        var pose = g.pose();
        pose.pushMatrix();
        pose.translate(cx + 10, cy + 28);
        pose.scale(2f, 2f);
        Tx.draw(g, font, big, 0, 0, t.text());
        pose.popMatrix();
        String sub = "+" + AuctionState.money(total) + (range.equals("Stream") ? " this stream" : range.equals("Day") ? " today" : " this week");
        Tx.draw(g, font, sub, cx + 10, cy + 47, t.good());

        // graph
        int gx = cx + 10, gy = cy + 62, gw = cw - 20, gh = 74;
        if (count == 0) {
            String s = "No payments yet. Try /tenz demo";
            Tx.draw(g, font, s, gx + (gw - Tx.w(font, s)) / 2, gy + gh / 2 - 4, t.dim());
        } else {
            double max = Math.max(1, cum[N]);
            Tx.draw(g, font, AuctionState.money(max), gx, gy - 2, t.dim());
            for (int px = 0; px < gw; px++) {
                double f = px * (double) N / gw;
                int i0 = (int) f; int i1 = Math.min(N, i0 + 1);
                double v = cum[i0] + (cum[i1] - cum[i0]) * (f - i0);
                double f2 = (px + 1) * (double) N / gw;
                int j0 = Math.min(N, (int) f2); int j1 = Math.min(N, j0 + 1);
                double v2 = cum[j0] + (cum[j1] - cum[j0]) * (f2 - j0);
                int ya = gy + gh - (int) (v / max * (gh - 10));
                int yb = gy + gh - (int) (v2 / max * (gh - 10));
                int top = Math.min(ya, yb), bot = Math.max(ya, yb) + 2;
                g.fill(gx + px, top, gx + px + 1, bot, t.good());
                g.fill(gx + px, bot, gx + px + 1, gy + gh, soft(t.good(), 0x1C));
            }
        }
        g.fill(gx, gy + gh, gx + gw, gy + gh + 1, 0x33FFFFFF);
        String l = range.equals("Stream") ? "Start" : range.equals("Day") ? "24h ago" : "7d ago";
        Tx.draw(g, font, l, gx, gy + gh + 4, t.dim());
        Tx.draw(g, font, "Now", gx + gw - Tx.w(font, "Now"), gy + gh + 4, t.dim());
        cy += 160;

        // stats
        rr(g, cx, cy, cw, 24, t.panel());
        ring(g, cx, cy, cw, 24, 0x22FFFFFF);
        Tx.draw(g, font, "Payments", cx + 8, cy + 4, t.text());
        Tx.draw(g, font, count + " received", cx + 8, cy + 14, t.dim());
        String bs = "Biggest " + AuctionState.money(biggest);
        Tx.draw(g, font, bs, cx + cw - 8 - Tx.w(font, bs), cy + 8, t.good());
    }

    private void paymentsList(GuiGraphicsExtractor g, Theme t, int cx, int cy, int cw) {
        List<Payments.Pay> h = Payments.history;
        if (h.isEmpty()) {
            String s = "No payments yet";
            Tx.draw(g, font, s, cx + (cw - Tx.w(font, s)) / 2, cy + 80, t.dim());
            return;
        }
        long now = System.currentTimeMillis();
        for (int i = 0; i < 8 && i < h.size(); i++) {
            Payments.Pay p = h.get(h.size() - 1 - i);
            rr(g, cx, cy, cw, 20, t.panel());
            Tx.draw(g, font, p.name(), cx + 8, cy + 6, t.text());
            String amt = "+" + AuctionState.money(p.amount());
            Tx.draw(g, font, amt, cx + cw - 8 - Tx.w(font, amt), cy + 6, t.good());
            long m = Math.max(0, (now - p.t()) / 60000);
            String ago = m < 60 ? m + "m ago" : m < 1440 ? (m / 60) + "h ago" : (m / 1440) + "d ago";
            Tx.draw(g, font, ago, cx + cw - 80 - Tx.w(font, ago), cy + 6, t.dim());
            cy += 22;
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent e, boolean dbl) {
        float sc = sc();
        double mx = e.x() / sc, my = e.y() / sc;
        for (Btn b : btns) if (b.hit(mx, my)) { b.action().run(); return true; }
        boolean inSearch = mx >= left() + W - 124 && mx <= left() + W - 12 && my >= top() + 8 && my <= top() + 24;
        setFocused(inSearch ? search : null);
        search.setFocused(inSearch);
        return inSearch;
    }
}
