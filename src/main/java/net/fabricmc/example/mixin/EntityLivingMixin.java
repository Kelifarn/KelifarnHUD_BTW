package net.fabricmc.example.mixin;

import net.fabricmc.example.Holder;
import net.minecraft.src.DamageSource;
import net.minecraft.src.EntityCreature;
import net.minecraft.src.EntityMob;
import net.minecraft.src.EntityPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityCreature.class)
public class EntityLivingMixin {

    @Inject(method = "onDeath(Lnet/minecraft/src/DamageSource;)V", at = @At("HEAD"))
    private void onDeathMobExpirience(DamageSource source, CallbackInfo ci) {
        if(source.getSourceOfDamage() instanceof EntityPlayer){
            EntityMob mob = (EntityMob)((Object)this);
            Holder.onMobDeath(mob.getLastAttacker());
        }
    }
}
