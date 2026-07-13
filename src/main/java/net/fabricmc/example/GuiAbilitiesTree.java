package net.fabricmc.example;

import net.minecraft.src.*;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.util.List;

public class GuiAbilitiesTree extends GuiScreen {
    private final Minecraft mc = Minecraft.getMinecraft();

    public GuiAbilitiesTree() {
    }

    @Override
    public void initGui() {
        this.buttonList.clear();
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        // Mining Column (Column 1): centerX - 147, width = 86
        this.buttonList.add(new GuiButton(10, centerX - 147, centerY - 20, 86, 20, getUpgradeBtnText("Fast Miner", Holder.upgradeMiningSpeed, 3)));
        
        // Fighting Column (Column 2): centerX - 43, width = 86
        
        // Survival Column (Column 3): centerX + 61, width = 86
        
        // Reset and Close: bottom center
        this.buttonList.add(new GuiButton(101, centerX + 5, centerY + 50, 90, 20, "Close"));
    }

    private String getUpgradeBtnText(String name, int level, int max) {
        if (level >= max) return name + " [MAX]";
        return name + " [" + level + "/" + max + "]";
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == 10) { // Fast Miner
            if (Holder.miningPoints >= 1 && Holder.upgradeMiningSpeed < 3) {
                Holder.miningPoints -= 1;
                Holder.upgradeMiningSpeed += 1;
                button.displayString = getUpgradeBtnText("Fast Miner", Holder.upgradeMiningSpeed, 3);
                Holder.saveBookmarks();
            }
        } else if (button.id == 101) { // Close
            mc.displayGuiScreen(null);
        }
    }

    @Override
    protected void keyTyped(char character, int key) {
        if (key == Keyboard.KEY_ESCAPE || key == Keyboard.KEY_K) {
            mc.displayGuiScreen(null);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        drawCenteredString(this.fontRenderer, "Character Abilities Tree", centerX, centerY - 80, 0xFFFFFF);

        int columnY = centerY - 65;
        int columnHeight = 105;

        // Draw backgrounds for three columns
        drawRectInner(centerX - 152, columnY, centerX - 56, columnY + columnHeight, 0x60000000);
        drawRectInner(centerX - 48, columnY, centerX + 48, columnY + columnHeight, 0x60000000);
        drawRectInner(centerX + 56, columnY, centerX + 152, columnY + columnHeight, 0x60000000);

        // Column 1: Mining
        drawCenteredString(this.fontRenderer, "\u00a7aMining", centerX - 104, centerY - 60, 0xFFFFFF);
        drawCenteredString(this.fontRenderer, "Level " + Holder.miningLevel, centerX - 104, centerY - 50, 0xA0A0A0);
        drawCenteredString(this.fontRenderer, "\u00a7ePoints: " + Holder.miningPoints, centerX - 104, centerY - 40, 0xFFFFFF);
        // XP progress bar
        int barX1 = centerX - 144;
        int barY1 = centerY - 28;
        drawRectInner(barX1, barY1, barX1 + 80, barY1 + 4, 0xFF303030);
        int reqMining = Holder.getRequiredXP(Holder.miningLevel);
        int mProgress = (int) (80.0f * ((float) Holder.miningXP / reqMining));
        drawRectInner(barX1, barY1, barX1 + mProgress, barY1 + 4, 0xFF55FF55);

        // Column 2: Fighting
        drawCenteredString(this.fontRenderer, "\u00a7cFighting", centerX, centerY - 60, 0xFFFFFF);
        drawCenteredString(this.fontRenderer, "Level " + Holder.fightingLevel, centerX, centerY - 50, 0xA0A0A0);
        drawCenteredString(this.fontRenderer, "\u00a7ePoints: " + Holder.fightingPoints, centerX, centerY - 40, 0xFFFFFF);
        // XP progress bar
        int barX2 = centerX - 40;
        int barY2 = centerY - 28;
        drawRectInner(barX2, barY2, barX2 + 80, barY2 + 4, 0xFF303030);
        int reqFighting = Holder.getRequiredXP(Holder.fightingLevel);
        int fProgress = (int) (80.0f * ((float) Holder.fightingXP / reqFighting));
        drawRectInner(barX2, barY2, barX2 + fProgress, barY2 + 4, 0xFFFF5555);

        // Column 3: Survival
        drawCenteredString(this.fontRenderer, "\u00a7bSurvival", centerX + 104, centerY - 60, 0xFFFFFF);
        drawCenteredString(this.fontRenderer, "Level " + Holder.survivalLevel, centerX + 104, centerY - 50, 0xA0A0A0);
        drawCenteredString(this.fontRenderer, "\u00a7ePoints: " + Holder.survivalPoints, centerX + 104, centerY - 40, 0xFFFFFF);
        // XP progress bar
        int barX3 = centerX + 64;
        int barY3 = centerY - 28;
        drawRectInner(barX3, barY3, barX3 + 80, barY3 + 4, 0xFF303030);
        int reqSurvival = Holder.getRequiredXP(Holder.survivalLevel);
        int sProgress = (int) (80.0f * ((float) Holder.survivalXP / reqSurvival));
        drawRectInner(barX3, barY3, barX3 + sProgress, barY3 + 4, 0xFF55FFFF);

        super.drawScreen(mouseX, mouseY, partialTicks);

        // Draw hover tooltips on top of buttons
        for (Object obj : this.buttonList) {
            GuiButton btn = (GuiButton) obj;
            if (mouseX >= btn.xPosition && mouseX < btn.xPosition + btn.width &&
                mouseY >= btn.yPosition && mouseY < btn.yPosition + btn.height) {
                drawTooltip(btn.id, mouseX, mouseY);
            }
        }
    }

    private void drawTooltip(int id, int x, int y) {
        String title = "";
        String desc = "";
        String cost = "";
        int color = 0xFFFFFF;

        if (id == 10) {
            title = "Fast Miner";
            desc = "Increases mining speed by 15% per level.";
            cost = "Cost: 1 Mining Point";
            color = 0x55FF55;
        } else {
            return; // No tooltip for Reset/Close
        }

        int titleW = this.fontRenderer.getStringWidth(title);
        int descW = this.fontRenderer.getStringWidth(desc);
        int costW = this.fontRenderer.getStringWidth(cost);
        int boxW = Math.max(titleW, Math.max(descW, costW)) + 12;
        int boxH = 42;

        int boxX = x + 10;
        int boxY = y - 20;

        if (boxX + boxW > this.width) {
            boxX = x - boxW - 5;
        }
        if (boxY + boxH > this.height) {
            boxY = this.height - boxH - 5;
        }

        drawRectInner(boxX, boxY, boxX + boxW, boxY + boxH, 0xF0101010);
        drawRectInner(boxX + 1, boxY + 1, boxX + boxW - 1, boxY + boxH - 1, 0xE0202020);

        this.fontRenderer.drawStringWithShadow(title, boxX + 6, boxY + 5, color);
        this.fontRenderer.drawStringWithShadow(desc, boxX + 6, boxY + 17, 0xCCCCCC);
        this.fontRenderer.drawStringWithShadow(cost, boxX + 6, boxY + 29, 0xFFFF55);
    }

    private void drawRectInner(int x1, int y1, int x2, int y2, int color) {
        Tessellator tessellator = Tessellator.instance;
        float a = (float) (color >> 24 & 255) / 255.0F;
        float r = (float) (color >> 16 & 255) / 255.0F;
        float g = (float) (color >> 8 & 255) / 255.0F;
        float b = (float) (color & 255) / 255.0F;
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glColor4f(r, g, b, a);
        tessellator.startDrawingQuads();
        tessellator.addVertex(x1, y2, 0.0);
        tessellator.addVertex(x2, y2, 0.0);
        tessellator.addVertex(x2, y1, 0.0);
        tessellator.addVertex(x1, y1, 0.0);
        tessellator.draw();
        GL11.glEnable(GL11.GL_TEXTURE_2D);
    }
}
