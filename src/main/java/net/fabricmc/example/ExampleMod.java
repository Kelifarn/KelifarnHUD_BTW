package net.fabricmc.example;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import net.minecraft.src.Item;
import net.minecraft.src.Minecraft;
import net.minecraft.src.ItemStack;

public class ExampleMod implements ModInitializer {
	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
		new Holder();
		System.out.println("Hello Fabric world!");
//		if(!isHoldingFishingRod()) return;
//		Minecraft.getMinecraft().thePlayer.
//		if(hookExists){
//			if(isBobberInWater()) return;
//
//			else useRod();
//		}
	}
//	public boolean isHoldingFishingRod() {
//		return isItemFishingRod(Minecraft.getMinecraft().thePlayer.getHeldItem().getItem());
//	}
//	private boolean isItemFishingRod(Item item) {
//		return item == Items.FISHING_ROD || item instanceof FishingRodItem;
//	}

}
