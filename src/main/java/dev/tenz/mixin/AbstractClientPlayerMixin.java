package dev.tenz.mixin;

import dev.tenz.module.Modules;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.ClientAsset;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.PlayerSkin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Fun tab: swaps every player's skin texture for the alien one (client-side only). */
@Mixin(AbstractClientPlayer.class)
public abstract class AbstractClientPlayerMixin {
    private static final Identifier TENZ_ALIEN = Identifier.fromNamespaceAndPath("tenz", "entity/alien");

    @Inject(method = "getSkin", at = @At("RETURN"), cancellable = true, require = 0)
    private void tenz$alien(CallbackInfoReturnable<PlayerSkin> cir) {
        Modules.alienHooked = true;
        if (!Modules.ALIEN.enabled) return;
        PlayerSkin s = cir.getReturnValue();
        cir.setReturnValue(new PlayerSkin(new ClientAsset.ResourceTexture(TENZ_ALIEN), s.cape(), s.elytra(), s.model(), s.secure()));
    }
}
