package dev.tenz.hud;

import net.minecraft.world.item.ItemStack;

/** Auction data. Later a donation/gift hookup just calls bid(). */
public final class AuctionState {
    public static ItemStack stack = ItemStack.EMPTY;
    public static String item = "";
    public static String topBidder = "-";
    public static double topBid = 0;
    public static long endsAtMs = 0;
    public static long durationMs = 1;

    public static void start(ItemStack s, int qty, int seconds) {
        stack = s.copyWithCount(1);
        start(s.getHoverName().getString() + " x" + qty, seconds);
    }
    public static void start(String name, int seconds) {
        item = name; topBidder = "-"; topBid = 0;
        endsAtMs = System.currentTimeMillis() + seconds * 1000L;
        durationMs = Math.max(1, seconds * 1000L);
    }
    public static boolean active() { return System.currentTimeMillis() < endsAtMs; }
    public static void bid(String who, double amount) {
        if (active() && amount > topBid) { topBid = amount; topBidder = who; }
    }
    public static String timeLeft() {
        long s = Math.max(0, (endsAtMs - System.currentTimeMillis()) / 1000);
        return (s / 60) + ":" + String.format("%02d", s % 60);
    }
    public static String money(double v) {
        if (v >= 1e9) return String.format("$%.1fB", v / 1e9);
        if (v >= 1e6) return String.format("$%.1fM", v / 1e6);
        if (v >= 1e3) return String.format("$%.1fK", v / 1e3);
        return String.format("$%.0f", v);
    }
}
