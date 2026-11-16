package dev.tenz.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.tenz.fun.Tricks;
import net.minecraft.client.renderer.FirstPersonHandsAndItemsRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.FirstPersonHandsAndItemsRenderState;
import net.minecraft.client.renderer.state.level.PlayerRenderState;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Knife tricks: while a trick is playing, spins the main-hand item (and arm) around the hand. */
@Mixin(FirstPersonHandsAndItemsRenderer.class)
public abstract class FirstPersonItemMixin {
    @Unique private boolean tenz$pushed = false;

    @Inject(method = "submitArmWithItem", at = @At("HEAD"), require = 0)
    private void tenz$head(PlayerRenderState a, FirstPersonHandsAndItemsRenderState b, float f1, float f2,
                           InteractionHand hand, float f3, ItemStack stack, float f4,
                           PoseStack ps, SubmitNodeCollector c, int light, CallbackInfo ci) {
        tenz$pushed = false;
        if (hand != InteractionHand.MAIN_HAND || !Tricks.running()) return;
        ps.pushPose();
        Tricks.apply(ps);
        tenz$pushed = true;
    }

    @Inject(method = "submitArmWithItem", at = @At("RETURN"), require = 0)
    private void tenz$tail(PlayerRenderState a, FirstPersonHandsAndItemsRenderState b, float f1, float f2,
                           InteractionHand hand, float f3, ItemStack stack, float f4,
                           PoseStack ps, SubmitNodeCollector c, int light, CallbackInfo ci) {
        if (tenz$pushed) { ps.popPose(); tenz$pushed = false; }
    }
}
