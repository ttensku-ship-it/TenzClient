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
