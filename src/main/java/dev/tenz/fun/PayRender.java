package dev.tenz.fun;

import dev.tenz.gui.Theme;
import dev.tenz.gui.Tx;
import dev.tenz.hud.AuctionState;
import dev.tenz.module.Modules;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/** Draws every payment animation. All original code and shapes; colors follow tiers. */
public final class PayRender {
    private static final String[] SYM = {"$", "7", "@", "#"};

    public static void render(GuiGraphicsExtractor g, Minecraft mc, Theme t) {
        Payments.tick();
        if (!Modules.PAYMENTS.enabled) return;
        int w = mc.getWindow().getGuiScaledWidth(), h = mc.getWindow().getGuiScaledHeight();
        long now = System.currentTimeMillis();
        rain(g, mc, w, h, now);
        feed(g, mc, t, w, now);
        banner(g, mc, t, w, now);
        slots(g, mc, w, h, now);
        wanted(g, mc, w, h, now);
        legend(g, mc, w, h, now);
    }

    static float rnd(int i, int salt) { return (((i * 73856093) ^ (salt * 19349663)) & 0xFFFF) / 65535f; }
    static float ease(float k) { k = Math.min(1f, Math.max(0f, k)); return 1f - (1f - k) * (1f - k) * (1f - k); }

    static void big(GuiGraphicsExtractor g, Minecraft mc, String s, float cx, float y, float scale, int color) {
        var pose = g.pose();
        pose.pushMatrix();
        pose.translate(cx, y);
        pose.scale(scale, scale);
        Tx.draw(g, mc.font, s, -Tx.w(mc.font, s) / 2, 0, color);
        pose.popMatrix();
    }

    static void avatar(GuiGraphicsExtractor g, Minecraft mc, String name, int x, int y, int size) {
        int hsh = name.hashCode();
        int col = 0xFF000000 | (((hsh & 0x7F7F7F) + 0x303030) & 0xFFFFFF);
        g.fill(x, y, x + size, y + size, col);
        big(g, mc, name.isEmpty() ? "?" : name.substring(0, 1).toUpperCase(), x + size / 2f, y + size / 2f - size * 0.4f, size / 11f, 0xFFFFFFFF);
    }

    private static void feed(GuiGraphicsExtractor g, Minecraft mc, Theme t, int w, long now) {
        if (!Modules.FEED.enabled) return;
        Payments.feed.removeIf(p -> now - p.t() > 5500);
        int i = 0;
        for (Payments.Pay p : Payments.feed) {
            float k = ease((now - p.t()) / 280f);
            int cw = 132, ch = 26, x = w - cw - 6 + (int) ((1f - k) * (cw + 10)), y = 6 + i * 30;
            int tier = Payments.tierColor(p.amount());
            g.fill(x, y, x + cw, y + ch, t.bg() | 0xE0000000);
            g.fill(x, y, x + 3, y + ch, tier);
            avatar(g, mc, p.name(), x + 8, y + 4, 18);
            Tx.draw(g, mc.font, p.name(), x + 30, y + 4, t.text());
            Tx.draw(g, mc.font, "+" + AuctionState.money(p.amount()), x + 30, y + 15, tier);
            i++;
        }
    }

    private static void banner(GuiGraphicsExtractor g, Minecraft mc, Theme t, int w, long now) {
        Payments.Pay p = Payments.bannerPay;
        if (!Modules.BANNER.enabled || p == null) return;
        long age = now - p.t();
        if (age > 4500) return;
        float k = ease(age / 300f);
        int bw = 220, x = (w - bw) / 2, y = (int) (-44 + 44 * k);
        g.fill(x, y, x + bw, y + 40, 0xE0101820);
        g.fill(x, y + 38, x + bw, y + 40, Payments.tierColor(p.amount()));
        big(g, mc, "* IT'S RAINING YOU *", w / 2f, y + 6, 1.2f, t.accent());
        big(g, mc, p.name() + " donated " + AuctionState.money(p.amount()) + "!", w / 2f, y + 22, 1.2f, 0xFFFFFFFF);
    }

    private static void rain(GuiGraphicsExtractor g, Minecraft mc, int w, int h, long now) {
        long age = now - Payments.rainAt;
        if (age > Payments.rainMs) return;
        for (int i = 0; i < 40; i++) {
            float x = rnd(i, 1) * w;
            float y = (age * 0.12f * (0.6f + rnd(i, 2)) + rnd(i, 3) * h) % h;
            g.fill((int) x, (int) y, (int) x + 9, (int) y + 9, 0xFF2EAA4A);
            Tx.draw(g, mc.font, "$", (int) x + 2, (int) y + 1, 0xFFFFFFFF);
        }
    }

    private static void slots(GuiGraphicsExtractor g, Minecraft mc, int w, int h, long now) {
        Payments.Pay p = Payments.slotsPay;
        if (!Modules.SLOTS.enabled || p == null) return;
        long age = now - p.t();
        if (age > 4800) return;
        int bw = 108, x = (w - bw) / 2, y = h / 2 - 70;
        g.fill(x - 3, y - 3, x + bw + 3, y + 36, 0xFFC8A020);
        g.fill(x, y, x + bw, y + 33, 0xFF101018);
        for (int i = 0; i < 3; i++) {
            int cx = x + 5 + i * 34;
            g.fill(cx, y + 4, cx + 28, y + 29, 0xFFEFEFD8);
            long stop = 1200 + i * 500L;
            String s = age < stop ? SYM[(int) ((age / 70 + i) % 4)] : "$";
            big(g, mc, s, cx + 14, y + 8, 2f, age < stop ? 0xFF555555 : 0xFF1E9A3C);
        }
        if (age > 2400) {
            boolean flash = (age / 150) % 2 == 0;
            big(g, mc, "JACKPOT!!!", w / 2f, y + 42, 2f, flash ? 0xFFFF4040 : 0xFFFFD700);
            big(g, mc, p.name() + " +" + AuctionState.money(p.amount()), w / 2f, y + 62, 1f, 0xFFFFFFFF);
        }
    }

    private static void wanted(GuiGraphicsExtractor g, Minecraft mc, int w, int h, long now) {
        Payments.Pay p = Payments.wantedPay;
        if (!Modules.WANTED.enabled || p == null) return;
        long age = now - p.t();
        if (age > 5500) return;
        float k = ease(age / 350f);
        int pw = 92, ph = 112, x = w - pw - 10 + (int) ((1f - k) * (pw + 20)), y = h / 2 - ph / 2;
        g.fill(x - 2, y - 2, x + pw + 2, y + ph + 2, 0xFF8A6A30);
        g.fill(x, y, x + pw, y + ph, 0xFFD8C08A);
        big(g, mc, "WANTED", x + pw / 2f, y + 6, 1.5f, 0xFF3A2410);
        avatar(g, mc, p.name(), x + (pw - 40) / 2, y + 24, 40);
        big(g, mc, p.name(), x + pw / 2f, y + 68, 1f, 0xFF3A2410);
        big(g, mc, "REWARD", x + pw / 2f, y + 82, 1f, 0xFFA01818);
        big(g, mc, AuctionState.money(p.amount()), x + pw / 2f, y + 94, 1.2f, 0xFFA01818);
    }

    private static void legend(GuiGraphicsExtractor g, Minecraft mc, int w, int h, long now) {
        Payments.Pay p = Payments.legendPay;
        if (!Modules.LEGEND.enabled || p == null) return;
        long age = now - p.t();
        if (age > 5500) return;
        float k = ease(age / 300f);
        int fade = age > 4800 ? (int) (0x55 * (1f - (age - 4800) / 700f)) : 0x55;
        g.fill(0, 0, w, h, (Math.max(0, fade) << 24));
        int cx = w / 2, cy = h / 2 - 30;
        for (int r = 110; r > 10; r -= 20) {
            int rr = (int) (r * k * (1f + 0.05f * (float) Math.sin(age / 120.0)));
            g.fill(cx - rr, cy - rr, cx + rr, cy + rr, 0x18FFD700);
        }
        g.fill(cx - 18, cy - 18, cx + 18, cy + 18, 0xFFFFD04A);
        g.fill(cx - 12, cy - 12, cx + 12, cy + 12, 0xFFB07840);
        g.fill(cx - 18, cy - 30, cx - 10, cy - 18, 0xFFFFD700);
        g.fill(cx - 4, cy - 34, cx + 4, cy - 18, 0xFFFFD700);
        g.fill(cx + 10, cy - 30, cx + 18, cy - 18, 0xFFFFD700);
        big(g, mc, "*** LEGENDARY ***", cx, cy + 28, 1.6f * k, 0xFFFFD700);
        big(g, mc, AuctionState.money(p.amount()), cx, cy + 46, 4f * k, 0xFFFFD700);
        big(g, mc, p.name() + " IS A LEGEND", cx, cy + 84, 1.5f * k, 0xFFFFFFFF);
        int[] cols = {0xFFFF5555, 0xFF55FF55, 0xFF55AAFF, 0xFFFFFF55, 0xFFFF77FF};
        for (int i = 0; i < 70; i++) {
            float x = rnd(i, 4) * w;
            float y = (age * 0.1f * (0.5f + rnd(i, 5)) + rnd(i, 6) * h) % h;
            g.fill((int) x, (int) y, (int) x + 3, (int) y + 3, cols[i % cols.length]);
        }
    }
}
