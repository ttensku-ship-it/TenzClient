package dev.tenz.gui;

import dev.tenz.fun.Payments;
import dev.tenz.hud.AuctionState;
import java.util.*;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Top donators board: podium for the top 3, rows for 4-8, total raised. */
public class DonorsScreen extends Screen {
    public DonorsScreen() { super(Component.literal("Top donators")); }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mx, int my, float delta) {
        Theme t = Theme.current();
        int pw = 280, ph = 236, x = width / 2 - pw / 2, y = height / 2 - ph / 2;
        g.fill(x - 2, y - 2, x + pw + 2, y + ph + 2, 0xFFC8A020);
        g.fill(x, y, x + pw, y + ph, 0xF0101018);
        String title = "TOP DONATORS";
        g.text(font, title, x + (pw - font.width(title)) / 2, y + 8, 0xFFFFD700);
        List<Map.Entry<String, Double>> top = Payments.totals.entrySet().stream()
            .sorted((a, b) -> Double.compare(b.getValue(), a.getValue())).limit(8).toList();
        int[] order = {1, 0, 2};
        int[] colors = {0xFFC8C8C8, 0xFFFFD700, 0xFFD08040};
        int[] heights = {30, 42, 24};
        String[] ranks = {"2ND", "1ST", "3RD"};
        for (int c = 0; c < 3; c++) {
            int idx = order[c];
            int cx = x + pw / 2 + (c - 1) * 80;
            int base = y + 100;
            int bh = heights[c];
            g.fill(cx - 24, base - bh, cx + 24, base, colors[c]);
            g.text(font, ranks[c], cx - font.width(ranks[c]) / 2, base - bh + 4, 0xFF000000);
            if (idx < top.size()) {
                var e = top.get(idx);
                g.fill(cx - 10, base - bh - 24, cx + 10, base - bh - 4, 0xFF8A6048);
                g.text(font, e.getKey(), cx - font.width(e.getKey()) / 2, base + 4, t.text());
                String m = AuctionState.money(e.getValue());
                g.text(font, m, cx - font.width(m) / 2, base + 14, t.good());
            }
        }
        for (int i = 3; i < 8; i++) {
            int ry = y + 134 + (i - 3) * 16;
            g.text(font, "#" + (i + 1), x + 12, ry, t.dim());
            if (i < top.size()) {
                var e = top.get(i);
                g.text(font, e.getKey(), x + 40, ry, t.text());
                String m = AuctionState.money(e.getValue());
                g.text(font, m, x + pw - 12 - font.width(m), ry, t.good());
            }
        }
        g.text(font, "TOTAL RAISED: " + AuctionState.money(Payments.totalRaised), x + 12, y + ph - 16, 0xFFFFD700);
        g.text(font, "[ESC] close", x + pw - 12 - font.width("[ESC] close"), y + ph - 16, t.dim());
    }
}
