package dev.tenz.mixin;

import dev.tenz.module.Modules;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SkyRenderer;
import net.minecraft.client.renderer.state.level.SkyRenderState;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Custom sky: after vanilla fills the sky state, recolor it. Slowly drifting colors, stars always on. */
@Mixin(SkyRenderer.class)
public abstract class SkyRendererMixin {
    // base color A, base color B (it drifts between them), star brightness
    private static final float[][] PRESETS = {
        /* Galaxy   */ {0.10f, 0.04f, 0.28f,  0.30f, 0.05f, 0.45f, 1.0f},
        /* Orbit    */ {0.02f, 0.05f, 0.20f,  0.05f, 0.20f, 0.42f, 1.0f},
        /* Iris     */ {0.28f, 0.14f, 0.60f,  0.45f, 0.25f, 0.80f, 0.8f},
        /* Velvet   */ {0.30f, 0.02f, 0.14f,  0.50f, 0.05f, 0.25f, 0.9f},
        /* Twilight */ {0.95f, 0.45f, 0.30f,  0.35f, 0.20f, 0.55f, 0.7f},
        /* Silver   */ {0.55f, 0.60f, 0.68f,  0.75f, 0.78f, 0.85f, 0.6f},
    };
    private static final String[] NAMES = {"Galaxy", "Orbit", "Iris", "Velvet", "Twilight", "Silver"};

    @Inject(method = "extractRenderState", at = @At("RETURN"), require = 0)
    private void tenz$sky(ClientLevel level, float partialTick, Camera camera, SkyRenderState state, CallbackInfo ci) {
        if (!Modules.SKY.enabled) return;
        int idx = 0;
        for (int i = 0; i < NAMES.length; i++) if (NAMES[i].equals(Modules.skyPreset)) idx = i;
        float[] p = PRESETS[idx];
        float t = (float) ((System.currentTimeMillis() % 40000L) / 40000.0 * Math.PI * 2.0);
        float k = 0.5f + 0.5f * (float) Math.sin(t);
        state.skyColor = new Vector3f(
            p[0] + (p[3] - p[0]) * k,
            p[1] + (p[4] - p[1]) * k,
            p[2] + (p[5] - p[2]) * k);
        state.starBrightness = p[6];
    }
}
