package dev.tenz.fun;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.tenz.module.Modules;
import java.util.Random;
import org.joml.Quaternionf;

/**
 * First-person knife tricks. Everything is driven by real time (nanoTime), not game ticks,
 * and every rotation is eased in and out, so it stays smooth at any FPS.
 */
public final class Tricks {
    public static final String[] NAMES = {"Spin", "Flip", "Twirl", "Showoff", "Random"};
    public static final String[] SPEEDS = {"Slow", "Normal", "Fast"};
    private static final Random RNG = new Random();

    private static String active = null;
    private static long startNs = 0L;
    private static float durSec = 0.6f;

    /** Starts a trick. Does nothing if Knife tricks is switched off. */
    public static void play(String name) {
        if (!Modules.KNIFE.enabled) return;
        if (name == null || name.equals("Random")) name = NAMES[RNG.nextInt(4)];
        float base = switch (name) { case "Flip" -> 0.62f; case "Twirl" -> 0.55f; case "Showoff" -> 1.0f; default -> 0.58f; };
        float mult = switch (Modules.knifeSpeed) { case "Slow" -> 1.45f; case "Fast" -> 0.7f; default -> 1.0f; };
        active = name; durSec = base * mult; startNs = System.nanoTime();
    }

    private static float progress() {
        if (active == null) return -1f;
        float p = (System.nanoTime() - startNs) / 1e9f / durSec;
        if (p >= 1f) { active = null; return -1f; }
        return p;
    }

    public static boolean running() { return progress() >= 0f; }

    private static float inOutCubic(float p) {
        return p < 0.5f ? 4f * p * p * p : 1f - (float) Math.pow(-2f * p + 2f, 3) / 2f;
    }

    /** Rotates the held item (and arm) around the hand position. Call between pushPose / popPose. */
    public static void apply(PoseStack ps) {
        float p = progress();
        if (p < 0f || active == null) return;
        float e = inOutCubic(p);
        float lift = (float) Math.sin(Math.PI * p);
        float twoPi = (float) (Math.PI * 2.0);
        Quaternionf q = new Quaternionf();
        float up = 0f, pop = 0f;
        switch (active) {
            case "Flip" -> { q.rotateX(-twoPi * e); up = 0.12f * lift; }
            case "Twirl" -> { q.rotateZ(twoPi * 2f * e); up = 0.04f * lift; }
            case "Showoff" -> { q.rotateZ(twoPi * e); q.rotateX(-twoPi * e); up = 0.16f * lift; pop = 0.08f * lift; }
            default -> q.rotateY(twoPi * e);   // Spin
        }
        final float px = 0.45f, py = -0.5f, pz = -0.7f;   // roughly where the hand sits on screen
        ps.translate(px, py + up, pz);
        ps.rotate(q);
        float s = 1f + pop;
        ps.scale(s, s, s);
        ps.translate(-px, -py, -pz);
    }

    private Tricks() {}
}
