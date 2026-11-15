package dev.tenz.fun;

import dev.tenz.module.Modules;
import java.util.*;

/** Payment events: feed, banners, jackpots. Fed by chat ("X paid you $10M"), /tenz pay, or a future donation hookup. */
public final class Payments {
    public record Pay(String name, double amount, long t) {}
    private record Pending(long at, String name, double amount) {}

    public static final Deque<Pay> feed = new ArrayDeque<>();
    public static final Map<String, Double> totals = new HashMap<>();
    public static double totalRaised = 0, biggest = 0;
    public static Pay bannerPay, legendPay, wantedPay, slotsPay;
    public static long rainAt = 0, rainMs = 0;
    private static final List<Pending> pending = new ArrayList<>();

    public static void add(String name, double amt) {
        long now = System.currentTimeMillis();
        totals.merge(name, amt, Double::sum);
        totalRaised += amt;
        Pay p = new Pay(name, amt, now);
        boolean newBiggest = amt > biggest;
        if (newBiggest) biggest = amt;
        if (!Modules.PAYMENTS.enabled) return;
        if (Modules.FEED.enabled) { feed.addFirst(p); while (feed.size() > 6) feed.removeLast(); }
        if (Modules.BANNER.enabled && amt >= 1e8) bannerPay = p;
        if (Modules.LEGEND.enabled && amt >= 1e9) legendPay = p;
        if (Modules.RAIN.enabled && amt >= 5e7) { rainAt = now; rainMs = amt >= 1e9 ? 7000 : 4000; }
        if (Modules.SLOTS.enabled && amt >= 2.5e8 && amt < 1e9) slotsPay = p;
        if (Modules.WANTED.enabled && amt >= 5e8 && newBiggest) wantedPay = p;
    }

    public static double parse(String num, String suffix) {
        double v = Double.parseDouble(num.replace(",", ""));
        return switch (suffix.toUpperCase()) {
            case "K" -> v * 1e3; case "M" -> v * 1e6; case "B" -> v * 1e9; case "T" -> v * 1e12; default -> v;
        };
    }

    public static int tierColor(double a) {
        if (a < 5e7) return 0xFF3DDC5A;
        if (a < 1e8) return 0xFF4FE0F0;
        if (a < 5e8) return 0xFFFFA020;
        if (a < 1e9) return 0xFFFF6BB5;
        return 0xFFFF4040;
    }

    /** Fires a test sequence so you can see every animation. */
    public static void demo() {
        long now = System.currentTimeMillis();
        double[] a = {1e7, 1e7, 5e7, 1e8, 2.5e8, 5e8, 1e9, 5e9};
        String[] n = {"I_AmPost", "Skimmmmn", "Birdcpvp", "7zSully", "ItzIran", "K1RBE", "ItzIran", "vexrum"};
        for (int i = 0; i < a.length; i++) pending.add(new Pending(now + i * 6000L, n[i], a[i]));
    }

    public static void tick() {
        long now = System.currentTimeMillis();
        for (Iterator<Pending> it = pending.iterator(); it.hasNext(); ) {
            Pending p = it.next();
            if (now >= p.at()) { it.remove(); add(p.name(), p.amount()); }
        }
    }
}
