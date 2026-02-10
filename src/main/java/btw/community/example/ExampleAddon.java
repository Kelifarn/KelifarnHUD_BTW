package btw.community.example;

import api.AddonHandler;
import btw.BTWMod;
import org.lwjgl.opengl.GL11;
import api.BTWAddon;
import api.client.mojapi.UserProfile;
import api.item.items.ArmorItem;
import api.util.status.StatusEffect;
import net.java.games.input.Component;
import net.minecraft.client.ClientBrandRetriever;
import net.minecraft.src.*;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.client.main.Main;
import org.lwjgl.opengl.GL11;
import java.awt.*;

public class ExampleAddon extends BTWAddon {
    private static ExampleAddon instance;
    public ExampleAddon() {
        super();
    }

    @Override
    public void initialize() {
        AddonHandler.logMessage(this.getName() + " Version " + this.getVersionString() + " Initializing...");

    }
}

//    @Inject(method = "renderHotbar", at = @At("RETURN"))
//    private void renderAttributes(Window window, float tickDelta, CallbackInfo ci) {
//        if (!client.options.debugEnabled) {
//            this.client.profiler.swap("extrahud");
//            this.client.profiler.push("armor");
//            renderArmor();
//            this.client.profiler.push("weapon");
//            renderWeapon();
//            this.client.profiler.push("statuseffects");
//            drawStatusEffects(window);
//            this.client.profiler.pop();
//        }
//    }
//
//    private void renderArmor() {
//        int x = 5;
//        int y = 5 + 3 + (clien .armor.length) * 16;
//        int stringWidth = 0;
//
//        for (ItemStack armorPiece : client.player.inventory.armor) {
//            if (armorPiece != null && armorPiece.getItem() instanceof ArmorItem) {
//                stringWidth = Math.max(stringWidth, this.textRenderer.getStringWidth(armorPiece.getMaxDamage() - armorPiece.getDamage() + "/" + armorPiece.getMaxDamage()));
//            }
//        }
//
//        if (stringWidth == 0) return;
//
//        fill(x, 5, x + 22 + stringWidth + 5, y + 2, -1873784752);
//
//        for (ItemStack armorPiece : client.player.inventory.armor) {
//            y -= 16;
//            if (armorPiece == null) continue;
//            this.itemRenderer.renderInGuiWithOverrides(armorPiece, x + 3, y);
//            this.textRenderer.draw(armorPiece.getMaxDamage() - armorPiece.getDamage() + "/" + armorPiece.getMaxDamage(), x + 22, y + 4, Color.WHITE.getRGB());
//        }
//    }
//

//    private void drawStatusEffects(Window window) {
//        if (this.client.getCameraEntity() instanceof PlayerEntity) {
//            Collection<StatusEffectInstance> collection = this.client.player.getStatusEffectInstances();
//            if (!collection.isEmpty()) {
//                int stringWidth = 0;
//                for (StatusEffectInstance statusEffectInstance : collection.toArray(new StatusEffectInstance[0])) {
//                    stringWidth = Math.max(stringWidth, textRenderer.getStringWidth(I18n.translate(statusEffectInstance.getTranslationKey()) + getLevel(statusEffectInstance)));
//                }
//                int x = window.getWidth() - stringWidth - 28 - 10;
//                int y = 5;
//
//                fill(x + 3, y, window.getWidth() - 5, y + 4 + collection.size() * 25, -1873784752);
//
//                for (Iterator<StatusEffectInstance> iterator = collection.iterator(); iterator.hasNext(); y += 25) {
//                    StatusEffectInstance statusEffectInstance = iterator.next();
//                    StatusEffect statusEffect = StatusEffect.STATUS_EFFECTS[statusEffectInstance.getEffectId()];
//                    GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
//                    this.client.getTextureManager().bindTexture(INVENTORY_TEXTURE);
//                    if (statusEffect.method_2443()) {
//                        int m = statusEffect.method_2444();
//                        this.drawTexture(x + 6, y + 7, m % 8 * 18, 198 + m / 8 * 18, 18, 18);
//                    }
//
//                    String string = I18n.translate(statusEffect.getTranslationKey()) + getLevel(statusEffectInstance);
//
//                    this.textRenderer.drawWithShadow(string, (float) (x + 10 + 18), (float) (y + 6), 16777215);
//                    String string2 = StatusEffect.method_2436(statusEffectInstance);
//                    this.textRenderer.drawWithShadow(string2, (float) (x + 10 + 18), (float) (y + 6 + 10), 8355711);
//                }
//            }
//        }
//    }
//
//    private String getLevel(StatusEffectInstance statusEffectInstance) {
//        switch (statusEffectInstance.getAmplifier()) {
//            case 1: return  " " + I18n.translate("enchantment.level.2");
//            case 2: return  " " + I18n.translate("enchantment.level.3");
//            case 3: return  " " + I18n.translate("enchantment.level.4");
//            default: return "";
//        }
//    }
