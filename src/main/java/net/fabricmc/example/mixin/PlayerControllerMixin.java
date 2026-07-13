package net.fabricmc.example.mixin;

import net.fabricmc.example.Holder;
import net.minecraft.src.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerControllerMP.class)
public class PlayerControllerMixin {

    @Inject(method = "onPlayerDestroyBlock", at = @At("HEAD"))
    private void onDestroyBlock(int x, int y, int z, int side, CallbackInfoReturnable<Boolean> cir) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.theWorld != null) {
            Holder.onBlockBroken();
        }
    }
}
