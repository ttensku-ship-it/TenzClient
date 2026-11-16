package dev.tenz.hud;

import dev.tenz.gui.Draw;
import dev.tenz.gui.Theme;
import dev.tenz.gui.Tx;
import dev.tenz.module.Modules;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/**
 * HUD overlays: FPS pill with live graph, animated keystrokes, sliding auction card.
 * Every animation runs on real frame time (dt), not game ticks, so it stays smooth at 60, 144 or 240 FPS.
 */
public final class Overlays {
    private static long last = System.nanoTime();
    private static float dt = 0.016f;

    // FPS pill
    private static float fpsAlpha, fpsShown = 60f, dotPhase;
    private static final float[] fpsHist = new float[28];
    private static long histAt = 0;
    private static float cr = 85, cg = 224, cb = 138;

    // keystrokes
    private static float keysAlpha;
    private static final float[] keyP = new float[5];

    // auction card
    private static float aucSlide, bidFlash;
    private static double shownBid, lastBid;

    public static void render(GuiGraphicsExtractor g) {
        Minecraft mc = Minecraft.getInstance();
        Theme t = Theme.current();
        long now = System.nanoTime();
        dt = Math.min(0.1f, (now - last) / 1e9f);
        last = now;
        fps(g, mc, t);
        keys(g, mc, t);
        auction(g, mc, t);
        stack(g, mc, t);
        dev.tenz.fun.PayRender.render(g, mc, t);
    }

    /** Frame-rate independent ease toward a target. Higher speed = snappier. */
    private static float approach(float cur, float target, float speed) {
        return cur + (target - cur) * (1f - (float) Math.exp(-speed * dt));
    }
    private static float ease(float k) {
        k = Math.min(1f, Math.max(0f, k));
        return 1f - (1f - k) * (1f - k) * (1f - k);
    }

    // ---------------- FPS ----------------
    private static void fps(GuiGraphicsExtractor g, Minecraft mc, Theme t) {
        fpsAlpha = approach(fpsAlpha, Modules.FPS.enabled ? 1f : 0f, 10f);
        if (fpsAlpha < 0.02f) return;
        int fps = mc.getFps();
        fpsShown = approach(fpsShown, fps, 7f);
        long ms = System.currentTimeMillis();
        if (ms - histAt > 100) {
            histAt = ms;
            System.arraycopy(fpsHist, 1, fpsHist, 0, fpsHist.length - 1);
            fpsHist[fpsHist.length - 1] = fps;
        }
        int target = fps >= 60 ? t.good() : fps >= 30 ? 0xFFFFD34D : 0xFFFF5A5A;
        float k = 1f - (float) Math.exp(-6f * dt);
        cr += (((target >> 16) & 255) - cr) * k;
        cg += (((target >> 8) & 255) - cg) * k;
        cb += ((target & 255) - cb) * k;
        int col = 0xFF000000 | ((int) cr << 16) | ((int) cg << 8) | (int) cb;

        float e = ease(fpsAlpha);
        String txt = Math.round(fpsShown) + " FPS";
        int tw = Tx.w(mc.font, txt);
        int w = 14 + tw + 6 + fpsHist.length + 8, h = 16;
        int x = 6 + (int) ((1f - e) * -16), y = 6;
        Draw.rr(g, x, y, w, h, Draw.a(t.bg(), e));
        Draw.ring(g, x, y, w, h, Draw.a(0x44FFFFFF, e));

        dotPhase += dt * 4f;
        float pulse = 0.6f + 0.4f * (float) Math.sin(dotPhase);
        g.fill(x + 6, y + 6, x + 10, y + 10, Draw.a(col, e * pulse));
        Tx.hud(g, mc.font, txt, x + 14, y + 4, Draw.a(t.text(), e));

        float mx = 60f;
        for (float f : fpsHist) mx = Math.max(mx, f);
        int sx = x + 14 + tw + 6;
        for (int i = 0; i < fpsHist.length; i++) {
            int bh = Math.max(1, (int) (fpsHist[i] / mx * 9f));
            g.fill(sx + i, y + 12 - bh, sx + i + 1, y + 12, Draw.a(col, e * 0.85f));
        }
    }

    // ---------------- Balance / tracker / coords / clock (left column, under the auction card) ----------------
    private static float balA, trkA, crdA, clkA, balFlash;
    private static double balShown = -1, balLast = -1;
    private static final java.time.format.DateTimeFormatter CLK = java.time.format.DateTimeFormatter.ofPattern("h:mm:ss a");

    private static void stack(GuiGraphicsExtractor g, Minecraft mc, Theme t) {
        balA = approach(balA, Modules.BALANCE.enabled ? 1f : 0f, 10f);
        trkA = approach(trkA, Modules.TRACKER.enabled ? 1f : 0f, 10f);
        crdA = approach(crdA, Modules.COORDS.enabled && mc.player != null ? 1f : 0f, 10f);
        clkA = approach(clkA, Modules.CLOCK.enabled ? 1f : 0f, 10f);
        // slide down smoothly when the auction card is showing
        int y = 88 + (int) (58 * ease(aucSlide));

        if (balA > 0.02f) { y += balance(g, mc, t, y, ease(balA)); }
        if (trkA > 0.02f) { y += tracker(g, mc, t, y, ease(trkA)); }
        if (crdA > 0.02f && mc.player != null) {
            String s = "XYZ  " + (int) Math.floor(mc.player.getX()) + "  " + (int) Math.floor(mc.player.getY()) + "  " + (int) Math.floor(mc.player.getZ());
            y += pill(g, mc, t, y, s, ease(crdA));
        }
        if (clkA > 0.02f) { y += pill(g, mc, t, y, java.time.LocalTime.now().format(CLK).toLowerCase(), ease(clkA)); }
    }

    /** Small one-line pill; returns the height it used (including the gap). */
    private static int pill(GuiGraphicsExtractor g, Minecraft mc, Theme t, int y, String s, float e) {
        int w = Tx.w(mc.font, s) + 16, h = 16;
        int x = 6 + (int) ((1f - e) * -16);
        Draw.rr(g, x, y, w, h, Draw.a(t.bg(), e));
        Draw.ring(g, x, y, w, h, Draw.a(0x44FFFFFF, e));
        Tx.hud(g, mc.font, s, x + 8, y + 4, Draw.a(t.text(), e));
        return (int) ((h + 4) * e);
    }

    private static int balance(GuiGraphicsExtractor g, Minecraft mc, Theme t, int y, float e) {
        boolean known = dev.tenz.fun.Balance.known();
        double v = known ? dev.tenz.fun.Balance.value : 0;
        if (balShown < 0) balShown = v;
        if (v != balLast) { if (v > balLast && balLast >= 0) balFlash = 1f; balLast = v; }
        balFlash = approach(balFlash, 0f, 3f);
        balShown += (v - balShown) * (1f - Math.exp(-8f * dt));
        if (Math.abs(v - balShown) < 1) balShown = v;

        String val = known ? dev.tenz.fun.Balance.full(balShown) : "waiting...";
        String sub = known ? "Balance" : "waiting for balance...";
        int w = Math.max(100, Tx.w(mc.font, val) + 20), h = 30;
        int x = 6 + (int) ((1f - e) * -16);
        Draw.rr(g, x, y, w, h, Draw.a(t.bg(), e));
        Draw.ring(g, x, y, w, h, Draw.a(Draw.lerp(0x44FFFFFF, t.good(), balFlash), e));
        g.fill(x + 1, y + 4, x + 3, y + h - 4, Draw.a(t.accent(), e));
        Tx.hud(g, mc.font, sub, x + 10, y + 5, Draw.a(t.dim(), e));
        if (known && dev.tenz.fun.Balance.sessionChange() != 0) {
            double d = dev.tenz.fun.Balance.sessionChange();
            String ds = (d > 0 ? "+" : "-") + AuctionState.money(Math.abs(d));
            Tx.hud(g, mc.font, ds, x + w - 8 - Tx.w(mc.font, ds), y + 5, Draw.a(d > 0 ? t.good() : 0xFFFF6B6B, e));
        }
        Tx.hud(g, mc.font, val, x + 10, y + 17, Draw.a(Draw.lerp(t.text(), t.good(), balFlash), e));
        return (int) ((h + 4) * e);
    }

    private static float trkSign = 0f, trkDelta = 0f;
    private static double trkShown = -1;
    private static final int RED = 0xFFFF5A5A;

    /** Balance tracker: your balance, how far you are up (green) or down (red) this session, and a graph of it. */
    private static int tracker(GuiGraphicsExtractor g, Minecraft mc, Theme t, int y, float e) {
        boolean known = dev.tenz.fun.Balance.known();
        double bal = known ? dev.tenz.fun.Balance.value : 0;
        double delta = dev.tenz.fun.Balance.sessionChange();
        if (trkShown < 0) trkShown = bal;
        trkShown += (bal - trkShown) * (1f - Math.exp(-8f * dt));
        if (Math.abs(bal - trkShown) < 1) trkShown = bal;
        trkDelta += (float) ((delta - trkDelta) * (1f - Math.exp(-8f * dt)));
        float target = delta > 0 ? 1f : delta < 0 ? -1f : 0f;
        trkSign = approach(trkSign, target, 8f);
        int tint = Draw.lerp(t.text(), trkSign >= 0 ? t.good() : RED, Math.min(1f, Math.abs(trkSign)));

        int w = 144, h = 58;
        int x = 6 + (int) ((1f - e) * -16);
        Draw.rr(g, x, y, w, h, Draw.a(t.bg(), e));
        Draw.ring(g, x, y, w, h, Draw.a(Draw.lerp(0x44FFFFFF, tint, Math.abs(trkSign) * 0.6f), e));
        g.fill(x + 1, y + 4, x + 3, y + h - 4, Draw.a(known ? tint : t.accent(), e));

        Tx.hud(g, mc.font, "Tracker", x + 10, y + 5, Draw.a(t.dim(), e));
        String right = !known ? "" : (delta > 0 ? "UP" : delta < 0 ? "DOWN" : "EVEN");
        Tx.hud(g, mc.font, right, x + w - 8 - Tx.w(mc.font, right), y + 5, Draw.a(tint, e));

        String balS = known ? dev.tenz.fun.Balance.full(trkShown) : "waiting...";
        Tx.hud(g, mc.font, balS, x + 10, y + 17, Draw.a(known ? t.text() : t.dim(), e));
        if (known) {
            double d = trkDelta;
            String ds = (d >= 0 ? "+" : "-") + AuctionState.money(Math.abs(d));
            Tx.hud(g, mc.font, ds, x + w - 8 - Tx.w(mc.font, ds), y + 17, Draw.a(tint, e));
        } else {
            String hint = dev.tenz.fun.BalancePoll.failed ? "auto failed: use /tenz bal <amount>"
                : Modules.autoBal.equals("Off") ? "run /bal or /tenz bal <amount>" : "reading your balance...";
            Tx.hud(g, mc.font, hint, x + 10, y + 30, Draw.a(t.dim(), e));
        }

        // graph of the balance this session: columns above the start line are green, below are red
        int gx = x + 10, gy = y + 31, gw = w - 20, gh = 20;
        var hist = dev.tenz.fun.Balance.hist;
        if (known && hist.size() >= 1) {
            double t0 = hist.get(0)[0], t1 = Math.max(t0 + 60_000d, System.currentTimeMillis());
            double base = dev.tenz.fun.Balance.sessionStart;
            double lo = base, hi = base;
            for (double[] smp : hist) { lo = Math.min(lo, smp[1]); hi = Math.max(hi, smp[1]); }
            double range = Math.max(1d, hi - lo);
            int baseY = gy + (int) Math.round((hi - base) / range * (gh - 1));
            int idx = 0;
            double cur = hist.get(0)[1];
            for (int px = 0; px < gw; px++) {
                double tt = t0 + (t1 - t0) * (px + 1) / gw;
                while (idx < hist.size() && hist.get(idx)[0] <= tt) { cur = hist.get(idx)[1]; idx++; }
                int vy = gy + (int) Math.round((hi - cur) / range * (gh - 1));
                boolean up = cur >= base;
                int col = up ? t.good() : RED;
                int top = Math.min(vy, baseY), bot = Math.max(vy, baseY) + 1;
                g.fill(gx + px, top, gx + px + 1, bot, Draw.a(col, e * 0.55f));
                g.fill(gx + px, vy, gx + px + 1, vy + 1, Draw.a(col, e));
            }
            g.fill(gx, baseY, gx + gw, baseY + 1, Draw.a(0x55FFFFFF, e));
        } else if (known) {
            g.fill(gx, gy + gh / 2, gx + gw, gy + gh / 2 + 1, Draw.a(0x55FFFFFF, e));
        }
        return (int) ((h + 4) * e);
    }

    // ---------------- Keystrokes ----------------
    private static void keys(GuiGraphicsExtractor g, Minecraft mc, Theme t) {
        keysAlpha = approach(keysAlpha, Modules.KEYS.enabled ? 1f : 0f, 10f);
        if (keysAlpha < 0.02f) return;
        float e = ease(keysAlpha);
        int x = 6 + (int) ((1f - e) * -16), y = 28;
        key(g, mc, t, 0, "W", x + 18, y, 16, mc.options.keyUp.isDown(), e);
        key(g, mc, t, 1, "A", x, y + 18, 16, mc.options.keyLeft.isDown(), e);
        key(g, mc, t, 2, "S", x + 18, y + 18, 16, mc.options.keyDown.isDown(), e);
        key(g, mc, t, 3, "D", x + 36, y + 18, 16, mc.options.keyRight.isDown(), e);
        key(g, mc, t, 4, "SPACE", x, y + 36, 52, mc.options.keyJump.isDown(), e);
    }

    private static void key(GuiGraphicsExtractor g, Minecraft mc, Theme t, int i, String s, int x, int y, int w, boolean down, float e) {
        keyP[i] = approach(keyP[i], down ? 1f : 0f, 24f);
        float p = keyP[i];
        int in = Math.round(p); // keys sink 1px when pressed
        int fill = Draw.lerp(t.bg(), t.accent(), p);
        Draw.rr(g, x + in, y + in, w - 2 * in, 16 - 2 * in, Draw.a(fill, e));
        Draw.ring(g, x + in, y + in, w - 2 * in, 16 - 2 * in, Draw.a(0x40FFFFFF, e));
        int tc = Draw.lerp(t.text(), 0xFF000000, p);
        Tx.hud(g, mc.font, s, x + (w - Tx.w(mc.font, s)) / 2, y + 4, Draw.a(tc, e));
    }

    // ---------------- Auction card ----------------
    private static void auction(GuiGraphicsExtractor g, Minecraft mc, Theme t) {
        boolean on = Modules.AUCTION.enabled && AuctionState.active();
        aucSlide = approach(aucSlide, on ? 1f : 0f, 9f);
        if (aucSlide < 0.02f) { shownBid = 0; lastBid = 0; return; }

        if (AuctionState.topBid != lastBid) {
            if (AuctionState.topBid > lastBid) bidFlash = 1f;
            lastBid = AuctionState.topBid;
        }
        bidFlash = approach(bidFlash, 0f, 3f);
        shownBid += (AuctionState.topBid - shownBid) * (1f - Math.exp(-8f * dt));
        if (Math.abs(AuctionState.topBid - shownBid) < 1) shownBid = AuctionState.topBid;

        float e = ease(aucSlide);
        int w = 160, h = 52, y = 88;
        int x = 6 - (int) ((1f - e) * (w + 12));
        Draw.rr(g, x, y, w, h, Draw.a(t.bg(), e));
        Draw.ring(g, x, y, w, h, Draw.a(Draw.lerp(0x44FFFFFF, t.accent(), bidFlash), e));
        g.fill(x + 1, y + 4, x + 3, y + h - 4, Draw.a(t.accent(), e));

        Tx.hud(g, mc.font, "Auction", x + 10, y + 6, Draw.a(t.dim(), e));
        String left = AuctionState.timeLeft();
        Tx.hud(g, mc.font, left, x + w - 8 - Tx.w(mc.font, left), y + 6, Draw.a(t.text(), e));
        if (!AuctionState.stack.isEmpty()) g.item(AuctionState.stack, x + 10, y + 18);
        int tx = AuctionState.stack.isEmpty() ? x + 10 : x + 30;
        Tx.hud(g, mc.font, AuctionState.item, tx, y + 20, Draw.a(t.text(), e));
        Tx.hud(g, mc.font, AuctionState.topBidder, x + 10, y + 34, Draw.a(t.text(), e));
        String m = AuctionState.money(shownBid);
        int mc2 = Draw.lerp(t.good(), 0xFFFFFFFF, bidFlash);
        Tx.hud(g, mc.font, m, x + w - 8 - Tx.w(mc.font, m), y + 34, Draw.a(mc2, e));

        // time bar that drains smoothly
        float frac = Math.max(0f, Math.min(1f, (AuctionState.endsAtMs - System.currentTimeMillis()) / (float) AuctionState.durationMs));
        int bx = x + 10, bw = w - 20, by = y + h - 7;
        g.fill(bx, by, bx + bw, by + 2, Draw.a(0x33FFFFFF, e));
        g.fill(bx, by, bx + (int) (bw * frac), by + 2, Draw.a(t.accent(), e));
    }
}
