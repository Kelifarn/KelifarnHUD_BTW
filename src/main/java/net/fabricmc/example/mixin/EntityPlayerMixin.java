package net.fabricmc.example.mixin;

import net.fabricmc.example.Holder;
import net.minecraft.src.Block;
import net.minecraft.src.EntityPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityPlayer.class)
public class EntityPlayerMixin {
    @Inject(method = "getCurrentPlayerStrVsBlock", at = @At("RETURN"), cancellable = true)
    private void onGetStrVsBlock(Block block, int i, int j, int k, CallbackInfoReturnable<Float> cir) {
        float base = cir.getReturnValue();
        if (Holder.upgradeMiningSpeed > 0) {
            cir.setReturnValue(base * (1.0f + Holder.upgradeMiningSpeed * 0.15f));
        }
    }
}
