package net.fabricmc.example.mixin;

import btw.item.items.FishingRodItemBaited;
import btw.item.items.RottenFleshItem;
import emi.dev.emi.emi.api.EmiApi;
import emi.dev.emi.emi.api.stack.EmiStack;
import net.fabricmc.example.Holder;
import net.minecraft.src.*;
import org.lwjgl.Sys;
import org.lwjgl.input.Keyboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import static btw.item.BTWItemIDs.FISHING_ROD_BAITED_ITEM_ID;

@Mixin(EntityClientPlayerMP.class)
@Environment(EnvType.CLIENT)
public class AutoFishClient {
	// Internal state
	private static boolean keyPressed = false;
	private static boolean NeedToFish = false;
	private static int actionDelay = 0;
	private static int tryCount = 0;

	@Inject(method = "onUpdate", at = @At("HEAD"))
	private void autoFishTick(CallbackInfo ci) {
		Minecraft mc = Minecraft.getMinecraft();
		var player = mc.thePlayer;
		if (mc.currentScreen != null) return;

		// Toggle on G press (edge detection)
		if (Keyboard.isKeyDown(Keyboard.KEY_G) && !keyPressed) {
			Holder.autoFishEnabled = !Holder.autoFishEnabled;
		}
		keyPressed = Keyboard.isKeyDown(Keyboard.KEY_G);

		if (!Holder.autoFishEnabled) return;
		if (actionDelay > 0) {
			actionDelay--;
			return;
		}
		ItemStack rod = player.getCurrentEquippedItem();
		if(rod == null) return;
		if (rod.getItem().getClass() != FishingRodItemBaited.class) {
			var playerHotbar = player.inventory.mainInventory;
			if (playerHotbar[8] != null && playerHotbar[8].getItem().getClass() == RottenFleshItem.class) {
				mc.playerController.sendUseItem(player, mc.theWorld, rod);
				actionDelay = 20;
			} else{
				Holder.autoFishEnabled = false;
			}
			return;
		}
		EntityFishHook bobber = player.fishEntity;
		tryCount = 0;
		if (bobber == null) {
			// Recast
			mc.playerController.sendUseItem(player, mc.theWorld, rod);
			actionDelay = 50;  // Short anti-spam
		} else {
			if(NeedToFish){
				mc.playerController.sendUseItem(player, mc.theWorld, rod);
				NeedToFish = false;
				actionDelay = 100;  // Wait for bobber despawn
			}else{
				var motionYInt = ((int)(bobber.motionY*100));
				if(motionYInt != 0 && motionYInt != 1){
					System.out.println(motionYInt);
					System.out.println("In water" + bobber.inWater);
					System.out.println(player.getCurrentEquippedItem().getItem());
				}
				if ((motionYInt==3 || motionYInt==-3 ||motionYInt==-4 || motionYInt==4)) {
					NeedToFish = true;
					actionDelay = 5;
				}
			}
		}
	}
}