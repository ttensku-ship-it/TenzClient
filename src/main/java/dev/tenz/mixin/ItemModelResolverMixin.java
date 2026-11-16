package dev.tenz.mixin;

import dev.tenz.fun.KnifeSkins;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Knife models: draws your held item with a Tenz knife model by swapping its model before it is resolved. */
@Mixin(ItemModelResolver.class)
public abstract class ItemModelResolverMixin {
    @Unique private boolean tenz$busy = false;

    @Inject(method = "updateForLiving", at = @At("HEAD"), cancellable = true, require = 0)
    private void tenz$knife(ItemStackRenderState state, ItemStack stack, ItemDisplayContext ctx,
                            LivingEntity entity, CallbackInfo ci) {
        if (tenz$busy) return;
        ItemStack swapped = KnifeSkins.swap(stack, ctx, entity);
        if (swapped == null) return;
        tenz$busy = true;
        try {
            ((ItemModelResolver) (Object) this).updateForLiving(state, swapped, ctx, entity);
        } finally {
            tenz$busy = false;
        }
        ci.cancel();
    }
}
