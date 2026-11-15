package dev.tenz.hud;

import dev.tenz.gui.Theme;
import dev.tenz.gui.Tx;
import dev.tenz.module.Modules;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/** All HUD overlays draw from here: FPS, keystrokes, auction card. Colors come from the active theme. */
public final class Overlays {
    public static void render(GuiGraphicsExtractor g) {
        Minecraft mc = Minecraft.getInstance();
        Theme t = Theme.current();
        if (Modules.FPS.enabled) {
            g.fill(6, 6, 56, 20, t.bg());
            Tx.draw(g, mc.font, mc.getFps() + " FPS", 10, 9, t.text());
        }
        if (Modules.KEYS.enabled) keys(g, mc, t);
        if (Modules.AUCTION.enabled && AuctionState.active()) auction(g, mc, t);
        dev.tenz.fun.PayRender.render(g, mc, t);
    }

    private static void keys(GuiGraphicsExtractor g, Minecraft mc, Theme t) {
        int x = 6, y = 26;
        key(g, mc, t, "W", x + 18, y, mc.options.keyUp.isDown());
        key(g, mc, t, "A", x, y + 18, mc.options.keyLeft.isDown());
        key(g, mc, t, "S", x + 18, y + 18, mc.options.keyDown.isDown());
        key(g, mc, t, "D", x + 36, y + 18, mc.options.keyRight.isDown());
    }
    private static void key(GuiGraphicsExtractor g, Minecraft mc, Theme t, String s, int x, int y, boolean down) {
        g.fill(x, y, x + 16, y + 16, down ? t.accent() : t.bg());
        Tx.draw(g, mc.font, s, x + 5, y + 4, down ? 0xFF000000 : t.text());
    }

    private static void auction(GuiGraphicsExtractor g, Minecraft mc, Theme t) {
        int w = 160, h = 44, x = 6, y = 66;
        g.fill(x, y, x + w, y + h, t.bg());
        g.fill(x, y, x + 2, y + h, t.accent());
        Tx.draw(g, mc.font, "Auction", x + 8, y + 6, t.dim());
        if (!AuctionState.stack.isEmpty()) g.item(AuctionState.stack, x + 8, y + 18);
        int tx = AuctionState.stack.isEmpty() ? x + 8 : x + 28;
        Tx.draw(g, mc.font, AuctionState.item, tx, y + 18, t.text());
        String left = AuctionState.timeLeft();
        Tx.draw(g, mc.font, left, x + w - 8 - Tx.w(mc.font, left), y + 6, t.text());
        Tx.draw(g, mc.font, AuctionState.topBidder, x + 8, y + 33, t.text());
        String m = AuctionState.money(AuctionState.topBid);
        Tx.draw(g, mc.font, m, x + w - 8 - Tx.w(mc.font, m), y + 33, t.good());
    }
}
