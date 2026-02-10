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
		int screenWidth = var5.getScaledWidth();
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
		int afWidth = mc.fontRenderer.getStringWidth(afStatus);
		int afX = screenWidth - afWidth - 10;
		int afY = 30;  // Stack below time
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
//    private void renderWeapon() {
//        ItemStack itemStack = client.player.inventory.getMainHandStack();
//        if (itemStack != null) {
//            if (itemStack.getItem().isDamageable()) {
//                int x = 5;
//                int y = 5 + 15 + (client.player.inventory.armor.length) * 16;
//
//                int stringWidth = this.textRenderer.getStringWidth(itemStack.getMaxDamage() - itemStack.getDamage() + "/" + itemStack.getMaxDamage());
//
//                ItemStack arrowItemStack = null;
//                int arrows = 0;
//                if (itemStack.getItem() instanceof BowItem) {
//                    for (ItemStack itemStack1 : client.player.inventory.main) {
//                        if (itemStack1 != null && itemStack1.getItem().equals(Items.ARROW)) {
//                            arrows += itemStack1.count;
//                            arrowItemStack = itemStack1;
//                        }
//                    }
//                    stringWidth = Math.max(stringWidth, this.textRenderer.getStringWidth("x " + arrows));
//                }
//
//                fill(x, y - 3, x + 22 + stringWidth + 5, y + 19 + (arrowItemStack != null ? 16 : 0), -1873784752);
//
//                if (arrowItemStack != null) {
//                    this.itemRenderer.renderInGuiWithOverrides(arrowItemStack, x + 3, y + 16);
//                    this.textRenderer.draw("x " + arrows, x + 22, y + 20, Color.WHITE.getRGB());
//                }
//
//                this.itemRenderer.renderInGuiWithOverrides(itemStack, x + 3, y);
//                this.textRenderer.draw(itemStack.getMaxDamage() - itemStack.getDamage() + "/" + itemStack.getMaxDamage(), x + 22, y + 4, Color.WHITE.getRGB());
//            }
//            if (itemStack.getItem() instanceof PotionItem) {
//                int x = 5;
//                int y = 5 + 15 + (client.player.inventory.armor.length) * 16;
//
//                int stringWidth = this.textRenderer.getStringWidth(itemStack.getTooltip(client.player, false).get(1).split("7", 2)[1]);
//                fill(x, y - 3, x + 22 + stringWidth + 5, y + 19, -1873784752);
//
//                this.itemRenderer.renderInGuiWithOverrides(itemStack, x + 3, y);
//                this.textRenderer.draw(itemStack.getTooltip(client.player, false).get(1).split("7", 2)[1], x + 22, y + 4, Color.WHITE.getRGB());
//            }
//        }
//    }
//