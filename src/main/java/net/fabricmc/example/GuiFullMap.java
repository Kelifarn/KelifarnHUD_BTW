package net.fabricmc.example;

import net.minecraft.src.*;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.util.Iterator;

public class GuiFullMap extends GuiScreen {
    private final Minecraft mc = Minecraft.getMinecraft();
    private float mapScale = 1.0f;
    private double offsetX = 0;
    private double offsetZ = 0;
    private int draggingX = -1;
    private int draggingY = -1;

    public GuiFullMap() {
        if (mc.thePlayer != null) {
            offsetX = mc.thePlayer.posX;
            offsetZ = mc.thePlayer.posZ;
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();

        ScaledResolution res = new ScaledResolution(mc.gameSettings, mc.displayWidth, mc.displayHeight);
        int width = res.getScaledWidth();
        int height = res.getScaledHeight();

        // Render explored chunks
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        for (Long key : Holder.exploredChunks) {
            int chunkX = (int) (key >> 32);
            int chunkZ = (int) (key & 0xFFFFFFFFL);

            double screenX = width / 2.0 + (chunkX * 16 - offsetX) * mapScale;
            double screenY = height / 2.0 + (chunkZ * 16 - offsetZ) * mapScale;
            double size = 16 * mapScale;

            if (screenX + size > 0 && screenX < width && screenY + size > 0 && screenY < height) {
                drawRectInner((int) screenX, (int) screenY, (int) (screenX + size), (int) (screenY + size), 0xFF555555);
            }
        }

        // Render bookmarks
        for (Bookmark b : Holder.bookmarks) {
            double screenX = width / 2.0 + (b.x - offsetX) * mapScale;
            double screenY = height / 2.0 + (b.z - offsetZ) * mapScale;

            if (screenX > 0 && screenX < width && screenY > 0 && screenY < height) {
                drawRectInner((int) screenX - 2, (int) screenY - 2, (int) screenX + 2, (int) screenY + 2, 0xFF000000);
                drawRectInner((int) screenX - 1, (int) screenY - 1, (int) screenX + 1, (int) screenY + 1, b.color);
            }
        }

        // Render player
        if (mc.thePlayer != null) {
            double pX = width / 2.0 + (mc.thePlayer.posX - offsetX) * mapScale;
            double pY = height / 2.0 + (mc.thePlayer.posZ - offsetZ) * mapScale;
            drawRectInner((int) pX - 2, (int) pY - 2, (int) pX + 2, (int) pY + 2, 0xFFFFFFFF);
        }

        GL11.glEnable(GL11.GL_TEXTURE_2D);

        // Render bookmark labels (with texture 2D enabled)
        for (Bookmark b : Holder.bookmarks) {
            double screenX = width / 2.0 + (b.x - offsetX) * mapScale;
            double screenY = height / 2.0 + (b.z - offsetZ) * mapScale;

            if (screenX > 0 && screenX < width && screenY > 0 && screenY < height) {
                mc.fontRenderer.drawStringWithShadow(b.name, (int) screenX + 5, (int) screenY - 4, 0xFFFFFF);
            }
        }

        super.drawScreen(mouseX, mouseY, partialTicks);

        // Instructions
        drawCenteredString(mc.fontRenderer, "Full Map - Click to Pan, Right Click to Mark, Click Dot to Edit", width / 2, 10, 0xFFFFFF);
    }

    @Override
    protected void mouseClicked(int x, int y, int button) {
        ScaledResolution res = new ScaledResolution(mc.gameSettings, mc.displayWidth, mc.displayHeight);
        int worldX = (int) (offsetX + (x - res.getScaledWidth() / 2.0) / mapScale);
        int worldZ = (int) (offsetZ + (y - res.getScaledHeight() / 2.0) / mapScale);

        if (button == 0) { // Left click
            // Check if clicking near existing bookmark to edit it
            Bookmark clickedBookmark = null;
            for (Bookmark b : Holder.bookmarks) {
                double dx = b.x - worldX;
                double dz = b.z - worldZ;
                if (Math.abs(dx) < 10 / mapScale && Math.abs(dz) < 10 / mapScale) {
                    clickedBookmark = b;
                    break;
                }
            }

            if (clickedBookmark != null) {
                mc.displayGuiScreen(new GuiBookmarkEditor(this, clickedBookmark));
            } else {
                draggingX = x;
                draggingY = y;
            }
        } else if (button == 1) { // Right click
            // Check if clicking near existing bookmark to edit it
            Bookmark clickedBookmark = null;
            for (Bookmark b : Holder.bookmarks) {
                double dx = b.x - worldX;
                double dz = b.z - worldZ;
                if (Math.abs(dx) < 10 / mapScale && Math.abs(dz) < 10 / mapScale) {
                    clickedBookmark = b;
                    break;
                }
            }

            if (clickedBookmark != null) {
                mc.displayGuiScreen(new GuiBookmarkEditor(this, clickedBookmark));
            } else {
                mc.displayGuiScreen(new GuiBookmarkEditor(this, worldX, worldZ));
            }
        }
    }

    @Override
    protected void mouseMovedOrUp(int x, int y, int button) {
        if (button == 0) {
            draggingX = -1;
            draggingY = -1;
        }
    }

    @Override
    public void handleMouseInput() {
        super.handleMouseInput();
        int wheel = Mouse.getEventDWheel();
        if (wheel != 0) {
            if (wheel > 0)
                mapScale *= 1.1f;
            else
                mapScale /= 1.1f;
        }

        if (draggingX != -1) {
            int dx = Mouse.getEventX() * this.width / this.mc.displayWidth;
            int dy = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;

            offsetX -= (dx - draggingX) / mapScale;
            offsetZ -= (dy - draggingY) / mapScale;

            draggingX = dx;
            draggingY = dy;
        }
    }

    @Override
    protected void keyTyped(char c, int key) {
        if (key == Keyboard.KEY_M || key == Keyboard.KEY_ESCAPE) {
            mc.displayGuiScreen(null);
        }
    }

    private void drawRectInner(int x1, int y1, int x2, int y2, int color) {
        Tessellator tessellator = Tessellator.instance;
        float a = (float) (color >> 24 & 255) / 255.0F;
        float r = (float) (color >> 16 & 255) / 255.0F;
        float g = (float) (color >> 8 & 255) / 255.0F;
        float b = (float) (color & 255) / 255.0F;
        GL11.glColor4f(r, g, b, a);
        tessellator.startDrawingQuads();
        tessellator.addVertex(x1, y2, 0.0);
        tessellator.addVertex(x2, y2, 0.0);
        tessellator.addVertex(x2, y1, 0.0);
        tessellator.addVertex(x1, y1, 0.0);
        tessellator.draw();
    }
}
