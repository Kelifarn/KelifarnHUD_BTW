package net.fabricmc.example.mixin;

import btw.item.items.FishingRodItemBaited;
import btw.item.items.RottenFleshItem;
import emi.dev.emi.emi.api.EmiApi;
import emi.dev.emi.emi.api.stack.EmiStack;
import net.fabricmc.example.GuiBookmarkEditor;
import net.fabricmc.example.GuiAbilitiesTree;
import net.fabricmc.example.GuiBookmarkEditor;
import net.fabricmc.example.GuiFullMap;
import net.fabricmc.example.Holder;
import net.minecraft.src.*;
import net.minecraft.src.Item;
import net.minecraft.src.ItemFishingRod;
import org.lwjgl.Sys;
import org.lwjgl.input.Keyboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;


@Mixin(EntityClientPlayerMP.class)
@Environment(EnvType.CLIENT)
public class AutoFishClient {
	// Internal state
	private static boolean keyPressed = false;
	private static boolean mapKeyPressed = false;
	private static boolean bookmarkKeyPressed = false;
	private static boolean abilitiesKeyPressed = false;
	private static boolean NeedToFish = false;
	private static int actionDelay = 0;

	@Inject(method = "onUpdate", at = @At("HEAD"))
	private void autoFishTick(CallbackInfo ci) {
		Minecraft mc = Minecraft.getMinecraft();
		var player = mc.thePlayer;
		if (mc.currentScreen != null)
			return;

		// Toggle on G press (edge detection)
		if (Keyboard.isKeyDown(Keyboard.KEY_G) && !keyPressed) {
			Holder.autoFishEnabled = !Holder.autoFishEnabled;
		}
		keyPressed = Keyboard.isKeyDown(Keyboard.KEY_G);

		// Open map on M press (edge detection)
		if (Keyboard.isKeyDown(Keyboard.KEY_M) && !mapKeyPressed) {
			mc.displayGuiScreen(new GuiFullMap());
		}
		mapKeyPressed = Keyboard.isKeyDown(Keyboard.KEY_M);

		// Open bookmark editor on B press (edge detection)
		if (Keyboard.isKeyDown(Keyboard.KEY_B) && !bookmarkKeyPressed) {
			int posX = (int) Math.floor(player.posX);
			int posZ = (int) Math.floor(player.posZ);
			mc.displayGuiScreen(new GuiBookmarkEditor(null, posX, posZ));
		}
		bookmarkKeyPressed = Keyboard.isKeyDown(Keyboard.KEY_B);

		// Open abilities tree on K press (edge detection)
		if (Keyboard.isKeyDown(Keyboard.KEY_K) && !abilitiesKeyPressed) {
			mc.displayGuiScreen(new GuiAbilitiesTree());
		}
		abilitiesKeyPressed = Keyboard.isKeyDown(Keyboard.KEY_K);

		if (player != null) {
			// Track healing
			int currentHealth = (int)player.getHealth();
			if (Holder.lastHealth != -1 && currentHealth > Holder.lastHealth) {
				int diff = currentHealth - Holder.lastHealth;
				Holder.addSurvivalXP(diff * 15);
			}
			Holder.lastHealth = currentHealth;

			// Track eating (hunger restore)
			int currentFood = player.getFoodStats().getFoodLevel();
			if (Holder.lastFood != -1 && currentFood > Holder.lastFood) {
				int diff = currentFood - Holder.lastFood;
				Holder.addSurvivalXP(diff * 10);
			}
			Holder.lastFood = currentFood;
		}

		if (!Holder.autoFishEnabled)
			return;
		if (actionDelay > 0) {
			actionDelay--;
			return;
		}
		ItemStack rod = player.getCurrentEquippedItem();
		if (rod == null)
			return;
		if (rod.getItem().getClass() != FishingRodItemBaited.class) {
			var playerHotbar = player.inventory.mainInventory;
			if (playerHotbar[8] != null && playerHotbar[8].getItem().getClass() == RottenFleshItem.class
					&& rod.getItem().getClass() == ItemFishingRod.class) {
				mc.playerController.sendUseItem(player, mc.theWorld, rod);
				actionDelay = 20;
			} else {
				Holder.autoFishEnabled = false;
			}
			return;
		}
		EntityFishHook bobber = player.fishEntity;
		if (bobber == null) {
			// Recast
			mc.playerController.sendUseItem(player, mc.theWorld, rod);
			actionDelay = 50; // Short anti-spam
		} else {
			if (NeedToFish) {
				mc.playerController.sendUseItem(player, mc.theWorld, rod);
				NeedToFish = false;
				actionDelay = 30; // Wait for bobber despawn
			} else {
				var motionYInt = ((int) (bobber.motionY * 100));
				if ((motionYInt == 3 || motionYInt == -3 || motionYInt == -4 || motionYInt == 4)) {
					NeedToFish = true;
					actionDelay = 5;
				}
			}
		}
	}
}