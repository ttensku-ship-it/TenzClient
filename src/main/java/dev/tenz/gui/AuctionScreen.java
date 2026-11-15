package dev.tenz.gui;

import dev.tenz.hud.AuctionState;
import java.util.*;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;

/** Pick an item from your inventory or the full item grid (no typing), set amount + time, start. */
public class AuctionScreen extends Screen {
    private static final int COLS = 9, ROWS = 6, CELL = 18, PER_PAGE = COLS * ROWS;
    private static final int[] TIMES = {30, 60, 120, 300};
    private static List<ItemStack> allItems;

    private boolean inventoryTab = true;
    private int page = 0, qty = 1, seconds = 60;
    private ItemStack selected = ItemStack.EMPTY;
    private final List<Btn> btns = new ArrayList<>();
    private int lmx, lmy;

    public AuctionScreen() { super(Component.literal("Tenz Auction")); }

    private int left() { return width / 2 - 190; }
    private int top()  { return height / 2 - 120; }

    private List<ItemStack> source() {
        if (inventoryTab) {
            List<ItemStack> out = new ArrayList<>();
            if (minecraft != null && minecraft.player != null) {
                var inv = minecraft.player.getInventory();
                for (int i = 0; i < inv.getContainerSize(); i++) {
                    ItemStack s = inv.getItem(i);
                    if (!s.isEmpty()) out.add(s);
                }
            }
            return out;
        }
        if (allItems == null) {
            allItems = new ArrayList<>();
            for (Item it : BuiltInRegistries.ITEM) if (it != Items.AIR) allItems.add(new ItemStack(it));
        }
        return allItems;
    }

    private int pages(int n) { return Math.max(1, (n + PER_PAGE - 1) / PER_PAGE); }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mx, int my, float delta) {
        Theme t = Theme.current();
        btns.clear();
        lmx = mx; lmy = my;
        int x = left(), y = top();
        g.fill(x, y, x + 380, y + 240, t.bg());
        g.fill(x, y, x + 380, y + 2, t.accent());
        g.text(font, "Auction", x + 10, y + 9, t.text());

        button(g, t, x + 10, y + 22, 70, 14, "Inventory", inventoryTab, () -> { inventoryTab = true; page = 0; });
        button(g, t, x + 84, y + 22, 70, 14, "All items", !inventoryTab, () -> { inventoryTab = false; page = 0; });

        List<ItemStack> items = source();
        page = Math.min(page, pages(items.size()) - 1);
        int gx = x + 10, gy = y + 42;
        g.fill(gx - 2, gy - 2, gx + COLS * CELL + 2, gy + ROWS * CELL + 2, t.panel());
        for (int i = 0; i < PER_PAGE; i++) {
            int idx = page * PER_PAGE + i;
            if (idx >= items.size()) break;
            ItemStack s = items.get(idx);
            int cx = gx + (i % COLS) * CELL, cy = gy + (i / COLS) * CELL;
            if (ItemStack.isSameItem(s, selected)) g.fill(cx, cy, cx + CELL, cy + CELL, t.sel());
            g.item(s, cx + 1, cy + 1);
            btns.add(new Btn(cx, cy, CELL, CELL, () -> { selected = s.copyWithCount(1); qty = Math.min(Math.max(1, s.getCount()), 64); }));
        }
        int py = gy + ROWS * CELL + 8;
        button(g, t, gx, py, 20, 14, "<", false, () -> page = Math.max(0, page - 1));
        button(g, t, gx + COLS * CELL - 20, py, 20, 14, ">", false, () -> page = Math.min(pages(items.size()) - 1, page + 1));
        String pg = (page + 1) + " / " + pages(items.size());
        g.text(font, pg, gx + (COLS * CELL - font.width(pg)) / 2, py + 3, t.dim());

        int rx = x + 190;
        g.fill(rx, y + 22, x + 370, y + 230, t.panel());
        g.text(font, "Selected", rx + 8, y + 30, t.dim());
        if (selected.isEmpty()) {
            g.text(font, "Click an item", rx + 8, y + 46, t.text());
        } else {
            g.item(selected, rx + 8, y + 42);
            g.text(font, selected.getHoverName().getString(), rx + 30, y + 46, t.text());
        }
        g.text(font, "Amount: " + qty, rx + 8, y + 70, t.text());
        button(g, t, rx + 8, y + 82, 24, 14, "-1", false, () -> qty = Math.max(1, qty - 1));
        button(g, t, rx + 36, y + 82, 24, 14, "+1", false, () -> qty = Math.min(64, qty + 1));
        button(g, t, rx + 64, y + 82, 24, 14, "+8", false, () -> qty = Math.min(64, qty + 8));

        g.text(font, "Time", rx + 8, y + 108, t.dim());
        for (int i = 0; i < TIMES.length; i++) {
            int s = TIMES[i];
            String label = s < 60 ? s + "s" : (s / 60) + "m";
            button(g, t, rx + 8 + i * 40, y + 120, 36, 14, label, seconds == s, () -> seconds = s);
        }
        boolean ready = !selected.isEmpty();
        g.fill(rx + 8, y + 196, x + 362, y + 218, ready ? t.accent() : t.sel());
        String go = ready ? "Start auction" : "Pick an item first";
        g.text(font, go, rx + 8 + (172 - font.width(go)) / 2, y + 203, ready ? 0xFF000000 : t.dim());
        if (ready) btns.add(new Btn(rx + 8, y + 196, 172, 22, () -> {
            AuctionState.start(selected, qty, seconds);
            minecraft.setScreen(null);
        }));
    }

    private void button(GuiGraphicsExtractor g, Theme t, int x, int y, int w, int h, String label, boolean on, Runnable r) {
        boolean hv = lmx >= x && lmx <= x + w && lmy >= y && lmy <= y + h;
        g.fill(x - 1, y - 1, x + w + 1, y + h + 1, on ? t.accent() : (hv ? ((t.accent() & 0xFFFFFF) | 0x99000000) : t.panel()));
        g.fill(x, y, x + w, y + h, on ? t.accent() : (hv ? t.sel() : t.panel()));
        g.text(font, label, x + (w - font.width(label)) / 2, y + (h - 8) / 2, on ? 0xFF000000 : t.text());
        btns.add(new Btn(x, y, w, h, r));
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent e, boolean dbl) {
        for (int i = btns.size() - 1; i >= 0; i--) {
            Btn b = btns.get(i);
            if (b.hit(e.x(), e.y())) { b.action().run(); return true; }
        }
        return super.mouseClicked(e, dbl);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double sx, double sy) {
        int n = pages(source().size());
        page = Math.max(0, Math.min(n - 1, page - (int) Math.signum(sy)));
        return true;
    }
}
