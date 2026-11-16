package dev.tenz.mixin;

import dev.tenz.fun.SkyPresets;
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
    @Inject(method = "extractRenderState", at = @At("RETURN"), require = 0)
    private void tenz$sky(ClientLevel level, float partialTick, Camera camera, SkyRenderState state, CallbackInfo ci) {
        if (!Modules.SKY.enabled) return;
        int idx = SkyPresets.index(Modules.skyPreset);
        float[] c = SkyPresets.color(idx, SkyPresets.drift());
        state.skyColor = new Vector3f(c[0], c[1], c[2]);
        state.starBrightness = SkyPresets.PRESETS[idx][6];
    }
}
