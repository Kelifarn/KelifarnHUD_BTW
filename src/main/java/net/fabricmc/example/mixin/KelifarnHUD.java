package net.fabricmc.example.mixin;

import net.fabricmc.example.Holder;
import net.minecraft.src.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiIngame.class)
public class KelifarnHUD {
	public Minecraft mc = Minecraft.getMinecraft();

	@Inject(at = @At("RETURN"), method = "renderGameOverlay(FZII)V")
	private void renderAttributes(CallbackInfo info) {
		ScaledResolution var5 = new ScaledResolution(this.mc.gameSettings, this.mc.displayWidth, this.mc.displayHeight);
		int var7 = var5.getScaledHeight();
		if (mc.currentScreen != null) return;  // Skip if menu open
		var curItem = mc.thePlayer.getCurrentEquippedItem();
		if (curItem != null) {
			if (curItem.getItem().isDamageable()) {
				String text = curItem.getMaxDamage() - curItem.getItemDamage() + "/" + curItem.getMaxDamage();
				mc.fontRenderer.drawStringWithShadow(text, 20, var7-20, 0xFFFFFF);
			}
		}
		mc.fontRenderer.drawStringWithShadow(this.mc.thePlayer.getPlayerCoordinates().posX + "|"+ this.mc.thePlayer.getPlayerCoordinates().posY +"|" + this.mc.thePlayer.getPlayerCoordinates().posZ, 20, var7-10, 0xFFFFFF);
		mc.fontRenderer.drawStringWithShadow(getWorldTimeString(this.mc.theWorld.getWorldTime()), 20, 0, 0xFFFFFF);


		// AutoFish status (top-right)
		String afStatus = "AutoFish: " + (Holder.autoFishEnabled ? "ON" : "OFF");
		int afColor = Holder.autoFishEnabled ? 0xFF00FF00 : 0xFFFF0000;  // Green/Red
		int afX = 20;
		int afY = 10;  // Stack below time
		mc.fontRenderer.drawStringWithShadow(afStatus, afX, afY, afColor);
	}
	private static String getWorldTimeString(long worldTime) {
		long totalDays = worldTime / 24000L;
		long dayTime = worldTime % 24000L;
		String dayType = "Day";
		// Hours: 0-23 (full MC day cycle)
		int hours = (int) ((dayTime * 24L) / 24000L);
		if(hours >= 12){
			hours = hours -12;
			dayType = "Night";
		}
		// Minutes: 0-59
		int minutes = (int) (((dayTime * 24L % 24000L) * 60L) / 24000L);
		var moonState = totalDays % 8;
		var moonStateString= "";
		if(moonState == 0){
			moonStateString = "⬤";
		} else if(moonState == 4){
			moonStateString = "◯";
		}
		return dayType + String.format(" %02d:%02d %s", hours, minutes,moonStateString);
	}
}