package dev.tenz.fun;

import dev.tenz.module.Modules;
import net.minecraft.client.Minecraft;

/**
 * Keeps your balance (and so the tracker + graph) up to date by itself: every so often it runs your balance
 * command (default /bal) and reads the answer. The answer is hidden from chat so it does not spam you.
 */
public final class BalancePoll {
    public static final String[] CHOICES = {"Off", "30s", "1 min", "2 min"};
    public static boolean failed = false;

    private static Object lastLevel = null;
    private static long nextAt = 0L;
    private static long lastSentMs = 0L;

    public static long intervalMs() {
        return switch (Modules.autoBal) { case "30s" -> 30_000L; case "1 min" -> 60_000L; case "2 min" -> 120_000L; default -> 0L; };
    }

    /** True for a few seconds after we asked, so the reply line can be hidden. */
    public static boolean recentlySent() { return System.currentTimeMillis() - lastSentMs < 3500L; }

    /** Call every client tick. */
    public static void tick(Minecraft mc) {
        if (mc.player == null || mc.level == null) { lastLevel = null; return; }
        long now = System.currentTimeMillis();
        if (mc.level != lastLevel) { lastLevel = mc.level; nextAt = now + 4000L; }   // ask shortly after joining
        long iv = intervalMs();
        if (iv == 0L || failed) return;
        if (!Modules.TRACKER.enabled && !Modules.BALANCE.enabled) return;
        if (now < nextAt) return;
        nextAt = now + iv;
        send(mc);
    }

    /** Runs the balance command now. */
    public static void send(Minecraft mc) {
        String cmd = Modules.balCmd == null ? "" : Modules.balCmd.replace("/", "").trim();
        if (cmd.isEmpty() || mc.level == null) return;
        try {
            Object conn = mc.getConnection();
            if (conn == null) return;
            conn.getClass().getMethod("sendCommand", String.class).invoke(conn, cmd);
            lastSentMs = System.currentTimeMillis();
        } catch (Exception e) {
            failed = true;   // this game version has no sendCommand(String): fall back to /tenz bal
        }
    }

    /** Restart the timer (used when the setting changes). */
    public static void reset() { nextAt = 0L; failed = false; }

    private BalancePoll() {}
}
