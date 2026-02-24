package net.fabricmc.example.mixin;

import net.minecraft.src.*;
import net.fabricmc.example.Bookmark;
import net.fabricmc.example.Holder;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(GuiIngame.class)
public class MinimapMixin {
    public Minecraft mc = Minecraft.getMinecraft();

    // Minimap configuration
    private static final int MINIMAP_SIZE = 70; // Size of the minimap in pixels
    private static final int MINIMAP_MARGIN = 5; // Distance from screen edge
    private static final int MINIMAP_RANGE = 64; // How many blocks to show in each direction
    private static final int Y_RANGE = 10; // Only show entities/terrain within this many blocks vertically
    private static final int TERRAIN_SAMPLE_INTERVAL = 1; // Sample every N blocks for performance

    @Inject(at = @At("RETURN"), method = "renderGameOverlay(FZII)V")
    private void renderMinimap(CallbackInfo info) {
        if (mc.currentScreen != null)
            return; // Skip if menu open
        if (mc.thePlayer == null || mc.theWorld == null)
            return; // Skip if not in world

        ScaledResolution scaledRes = new ScaledResolution(mc.gameSettings, mc.displayWidth, mc.displayHeight);
        int screenWidth = scaledRes.getScaledWidth();

        // Position minimap in top-right corner
        int mapX = screenWidth - MINIMAP_SIZE - MINIMAP_MARGIN;
        int mapY = MINIMAP_MARGIN;
        int centerX = mapX + MINIMAP_SIZE / 2;
        int centerY = mapY + MINIMAP_SIZE / 2;
        int radius = MINIMAP_SIZE / 2;

        // Get player position
        double playerX = mc.thePlayer.posX;
        double playerY = mc.thePlayer.posY;
        double playerZ = mc.thePlayer.posZ;

        // Enable necessary GL states
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDisable(GL11.GL_TEXTURE_2D);

        // Setup stencil buffer for circular clipping
        GL11.glEnable(GL11.GL_STENCIL_TEST);
        GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT);

        // Draw the stencil mask (circular area)
        GL11.glStencilFunc(GL11.GL_ALWAYS, 1, 0xFF);
        GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_REPLACE);
        GL11.glStencilMask(0xFF);
        GL11.glColorMask(false, false, false, false);

        drawCircle(centerX, centerY, radius, 0xFFFFFFFF);

        // Now only draw where stencil = 1
        GL11.glStencilFunc(GL11.GL_EQUAL, 1, 0xFF);
        GL11.glStencilMask(0x00);
        GL11.glColorMask(true, true, true, true);

        // Draw background
        drawCircle(centerX, centerY, radius, 0xE0202020); // Dark semi-transparent background

        // Render terrain (no rotation, Y-level filtered)
        renderTerrain(centerX, centerY, playerX, playerY, playerZ, radius);

        // Render entities (no rotation, Y-level filtered)
        renderEntities(centerX, centerY, playerX, playerY, playerZ, radius);

        // Render bookmarks
        renderBookmarks(centerX, centerY, playerX, playerY, playerZ, radius);

        // Track explored chunk
        Holder.exploredChunks.add(Holder.getChunkKey((int) playerX, (int) playerZ));

        // Draw player direction cursor (pointy triangle)
        float playerYaw = mc.thePlayer.rotationYaw;
        drawDirectionalCursor(centerX, centerY, playerYaw);

        // Disable stencil test
        GL11.glDisable(GL11.GL_STENCIL_TEST);

        // Draw border
        drawCircleBorder(centerX, centerY, radius, 3, 0xFF000000);

        // Draw cardinal directions (fixed - North always up)
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        drawCardinalDirections(centerX, centerY, radius + 2);

        GL11.glDisable(GL11.GL_BLEND);
    }

    /**
     * Renders terrain blocks on the minimap (Y-level filtered, circular clipping)
     */
    private void renderTerrain(int centerX, int centerY, double playerX, double playerY, double playerZ, int radius) {
        GL11.glDisable(GL11.GL_TEXTURE_2D);

        for (int dx = -MINIMAP_RANGE; dx <= MINIMAP_RANGE; dx += TERRAIN_SAMPLE_INTERVAL) {
            for (int dz = -MINIMAP_RANGE; dz <= MINIMAP_RANGE; dz += TERRAIN_SAMPLE_INTERVAL) {
                // Check if this point is within the circular radius
                double distance = Math.sqrt(dx * dx + dz * dz);
                if (distance >= MINIMAP_RANGE) {
                    continue; // Skip points outside the circle
                }

                int blockX = (int) playerX + dx;
                int blockZ = (int) playerZ + dz;

                // Find blocks near player's Y level
                int searchY = (int) playerY;
                int blockId = 0;
                int blockY = searchY;

                // Search for solid block within Y_RANGE
                for (int y = searchY + Y_RANGE; y >= searchY - Y_RANGE; y--) {
                    int testId = mc.theWorld.getBlockId(blockX, y, blockZ);
                    if (testId != 0) { // Found a non-air block
                        blockId = testId;
                        blockY = y;
                        break;
                    }
                }

                // Skip if no block found or block is too far vertically
                if (blockId == 0 || Math.abs(blockY - playerY) > Y_RANGE) {
                    continue;
                }

                // Get block color
                int color = getBlockColor(blockId, blockY, (int) playerY);

                // Convert world coordinates to screen coordinates (no rotation)
                float screenX = centerX + (dx / (float) MINIMAP_RANGE) * radius;
                float screenY = centerY + (dz / (float) MINIMAP_RANGE) * radius;

                // Draw a small pixel/rect for this terrain sample
                int size = TERRAIN_SAMPLE_INTERVAL + 1;
                drawRect((int) screenX - size / 2, (int) screenY - size / 2,
                        (int) screenX + size / 2, (int) screenY + size / 2, color);
            }
        }
    }

    /**
     * Gets the representative color for a block type
     */
    private int getBlockColor(int blockId, int blockY, int playerY) {
        // Height-based shading
        int heightDiff = blockY - playerY;
        float shadeFactor = 1.0f - Math.min(Math.abs(heightDiff) / 10.0f, 0.3f);

        int baseColor;

        // Map common block IDs to colors (Minecraft 1.6.4 block IDs)
        switch (blockId) {
            case 0:
                return 0x00000000; // Air (transparent)
            case 1:
                baseColor = 0x808080;
                break; // Stone (gray)
            case 2:
                baseColor = 0x7CBD6B;
                break; // Grass (green)
            case 3:
                baseColor = 0x8B7355;
                break; // Dirt (brown)
            case 4:
                baseColor = 0x737373;
                break; // Cobblestone (dark gray)
            case 5:
                baseColor = 0x9C7F4E;
                break; // Wood planks (tan)
            case 7:
                baseColor = 0x4A4A4A;
                break; // Bedrock (very dark gray)
            case 8:
            case 9:
                baseColor = 0x2E5FCC;
                break; // Water (blue)
            case 10:
            case 11:
                baseColor = 0xFF6600;
                break; // Lava (orange)
            case 12:
                baseColor = 0xE0D8A8;
                break; // Sand (yellow-tan)
            case 13:
                baseColor = 0x8E8E86;
                break; // Gravel (light gray)
            case 17:
                baseColor = 0x6E5230;
                break; // Wood (brown)
            case 18:
                baseColor = 0x6BA34C;
                break; // Leaves (dark green)
            case 24:
                baseColor = 0xDED29C;
                break; // Sandstone (light tan)
            case 31:
                baseColor = 0x5E8F3C;
                break; // Tall grass (green)
            case 37:
            case 38:
                baseColor = 0x8FBE3C;
                break; // Flowers (yellowish green)
            case 48:
                baseColor = 0x4B6B4B;
                break; // Mossy cobblestone (mossy gray)
            case 60:
                baseColor = 0x5C4633;
                break; // Farmland (dark brown)
            case 78:
            case 80:
                baseColor = 0xF0F0F0;
                break; // Snow (white)
            case 79:
                baseColor = 0xA5C6E8;
                break; // Ice (light blue)
            case 82:
                baseColor = 0xA6A6A6;
                break; // Clay (gray)
            case 110:
                baseColor = 0x5E3A29;
                break; // Mycelium (purple-brown)
            default:
                baseColor = 0x888888; // Unknown (medium gray)
        }

        // Apply height shading
        int r = (int) (((baseColor >> 16) & 0xFF) * shadeFactor);
        int g = (int) (((baseColor >> 8) & 0xFF) * shadeFactor);
        int b = (int) ((baseColor & 0xFF) * shadeFactor);

        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    /**
     * Renders entities on the minimap (Y-level filtered, no rotation)
     */
    private void renderEntities(int centerX, int centerY, double playerX, double playerY, double playerZ, int radius) {
        @SuppressWarnings("unchecked")
        List<Entity> entities = mc.theWorld.loadedEntityList;

        for (Object obj : entities) {
            if (!(obj instanceof Entity))
                continue;
            Entity entity = (Entity) obj;

            // Skip the player itself
            if (entity == mc.thePlayer)
                continue;

            // Skip if EXP orb
            if (entity instanceof EntityXPOrb)
                continue;

            // Check Y-level distance (only show entities within Y_RANGE blocks vertically)
            double yDiff = Math.abs(entity.posY - playerY);
            if (yDiff > Y_RANGE)
                continue;

            // Calculate relative position (no rotation applied)
            double relX = entity.posX - playerX;
            double relZ = entity.posZ - playerZ;

            // Skip entities outside minimap range
            double distance = Math.sqrt(relX * relX + relZ * relZ);
            if (distance >= MINIMAP_RANGE) {
                continue;
            }
            // Convert world coordinates to screen coordinates (no rotation)
            int dotX = centerX + (int) ((relX / MINIMAP_RANGE) * radius);
            int dotY = centerY + (int) ((relZ / MINIMAP_RANGE) * radius);

            // Determine entity color
            int color = getEntityColor(entity);

            // Draw entity with border for better visibility
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            drawRect(dotX - 2, dotY - 2, dotX + 2, dotY + 2, 0xFF000000); // Black border
            drawRect(dotX - 1, dotY - 1, dotX + 1, dotY + 1, color); // Colored center
        }
    }

    /**
     * Determines the color for an entity on the minimap
     */
    private int getEntityColor(Entity entity) {
        if (entity instanceof EntityPlayer) {
            return 0xFFFFFFFF; // White for players
        } else if (entity instanceof EntityMob) {
            return 0xFFFF3030; // Bright red for hostile mobs
        } else if (entity instanceof btw.entity.mob.BTWSquidEntity) {
            return 0xFF800080; // Bright red for hostile mobs
        } else if (entity instanceof EntityAnimal) {
            return 0xFF30FF30; // Bright green for passive animals
        } else if (entity instanceof EntityItem) {
            return 0xFFFFFF30; // Yellow for items
        } else {
            return 0xFFAAAAAA; // Gray for other entities
        }
    }

    /**
     * Draws cardinal direction labels around the minimap (fixed positions)
     */
    private void drawCardinalDirections(int centerX, int centerY, int distance) {
        // North (top)
        String north = "N";
        int nWidth = mc.fontRenderer.getStringWidth(north);
        mc.fontRenderer.drawStringWithShadow(north, centerX - nWidth / 2, centerY - distance - 4, 0xFFFFFF);

        // South (bottom)
        String south = "S";
        int sWidth = mc.fontRenderer.getStringWidth(south);
        mc.fontRenderer.drawStringWithShadow(south, centerX - sWidth / 2, centerY + distance - 4, 0xFFFFFF);

        // East (right)
        String east = "E";
        int eWidth = mc.fontRenderer.getStringWidth(east);
        mc.fontRenderer.drawStringWithShadow(east, centerX + distance - eWidth / 2, centerY - 4, 0xFFFFFF);

        // West (left)
        String west = "W";
        int wWidth = mc.fontRenderer.getStringWidth(west);
        mc.fontRenderer.drawStringWithShadow(west, centerX - distance - wWidth / 2, centerY - 4, 0xFFFFFF);
    }

    /**
     * Draws a directional cursor (triangle) showing player's viewing direction
     */
    private void drawDirectionalCursor(int centerX, int centerY, float yaw) {
        GL11.glDisable(GL11.GL_TEXTURE_2D);

        // Convert yaw to radians (yaw 0 = South in Minecraft, but we want North = up)
        // Minecraft yaw: 0=South, 90=West, 180=North, 270=East
        // We need: 0 degrees = up (North)
        double angle = Math.toRadians(yaw + 180); // Adjust so North points up

        // Triangle dimensions
        int arrowLength = 6; // Length from center to tip
        int arrowWidth = 2; // Half-width of the base

        // Calculate triangle points
        // Tip of arrow (pointing in view direction)
        int tipX = centerX + (int) (Math.sin(angle) * arrowLength);
        int tipY = centerY - (int) (Math.cos(angle) * arrowLength);

        // Base left corner (perpendicular to direction)
        double perpAngle = angle + Math.PI / 2;
        int baseLeftX = centerX + (int) (Math.sin(perpAngle) * arrowWidth);
        int baseLeftY = centerY - (int) (Math.cos(perpAngle) * arrowWidth);

        // Base right corner
        int baseRightX = centerX - (int) (Math.sin(perpAngle) * arrowWidth);
        int baseRightY = centerY + (int) (Math.cos(perpAngle) * arrowWidth);

        // Draw the triangle
        Tessellator tessellator = Tessellator.instance;

        // Draw border (black outline)
        GL11.glColor4f(0, 0, 0, 1);
        tessellator.startDrawing(GL11.GL_LINE_LOOP);
        tessellator.addVertex(tipX, tipY, 0.0D);
        tessellator.addVertex(baseLeftX, baseLeftY, 0.0D);
        tessellator.addVertex(baseRightX, baseRightY, 0.0D);
        tessellator.draw();

        // Draw filled triangle (white)
        GL11.glColor4f(1, 1, 1, 1);
        tessellator.startDrawing(GL11.GL_TRIANGLES);
        tessellator.addVertex(tipX, tipY, 0.0D);
        tessellator.addVertex(baseLeftX, baseLeftY, 0.0D);
        tessellator.addVertex(baseRightX, baseRightY, 0.0D);
        tessellator.draw();
    }

    /**
     * Draws a filled circle
     */
    private void drawCircle(int centerX, int centerY, int radius, int color) {
        float a = (float) (color >> 24 & 255) / 255.0F;
        float r = (float) (color >> 16 & 255) / 255.0F;
        float g = (float) (color >> 8 & 255) / 255.0F;
        float b = (float) (color & 255) / 255.0F;

        GL11.glColor4f(r, g, b, a);

        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawing(GL11.GL_TRIANGLE_FAN);
        tessellator.addVertex(centerX, centerY, 0.0D);

        int segments = 64;
        for (int i = 0; i <= segments; i++) {
            double angle = 2 * Math.PI * i / segments;
            double x = centerX + radius * Math.cos(angle);
            double y = centerY + radius * Math.sin(angle);
            tessellator.addVertex(x, y, 0.0D);
        }

        tessellator.draw();
    }

    /**
     * Draws a circle border
     */
    private void drawCircleBorder(int centerX, int centerY, int radius, int thickness, int color) {
        float a = (float) (color >> 24 & 255) / 255.0F;
        float r = (float) (color >> 16 & 255) / 255.0F;
        float g = (float) (color >> 8 & 255) / 255.0F;
        float b = (float) (color & 255) / 255.0F;

        GL11.glColor4f(r, g, b, a);
        GL11.glLineWidth(thickness);

        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawing(GL11.GL_LINE_LOOP);

        int segments = 64;
        for (int i = 0; i < segments; i++) {
            double angle = 2 * Math.PI * i / segments;
            double x = centerX + radius * Math.cos(angle);
            double y = centerY + radius * Math.sin(angle);
            tessellator.addVertex(x, y, 0.0D);
        }

        tessellator.draw();
        GL11.glLineWidth(1.0f);
    }

    /**
     * Renders bookmarks on the minimap
     */
    private void renderBookmarks(int centerX, int centerY, double playerX, double playerY, double playerZ, int radius) {
        for (Bookmark b : Holder.bookmarks) {
            double relX = b.x - playerX;
            double relZ = b.z - playerZ;
            double distance = Math.sqrt(relX * relX + relZ * relZ);

            if (distance < MINIMAP_RANGE) {
                // Inside minimap range
                int dotX = centerX + (int) ((relX / MINIMAP_RANGE) * radius);
                int dotY = centerY + (int) ((relZ / MINIMAP_RANGE) * radius);
                drawRect(dotX - 2, dotY - 2, dotX + 2, dotY + 2, 0xFF000000); // Border
                drawRect(dotX - 1, dotY - 1, dotX + 1, dotY + 1, b.color); // Color
            } else if (b.permanent) {
                // Outside range but permanent (stick to edge)
                double angle = Math.atan2(relZ, relX);
                int dotX = centerX + (int) (Math.cos(angle) * radius);
                int dotY = centerY + (int) (Math.sin(angle) * radius);

                // Draw a small indicator on the edge
                drawRect(dotX - 2, dotY - 2, dotX + 2, dotY + 2, 0xFF000000);
                drawRect(dotX - 1, dotY - 1, dotX + 1, dotY + 1, b.color);
            }
        }
    }

    private void drawRect(int x1, int y1, int x2, int y2, int color) {
        int temp;
        if (x1 < x2) {
            temp = x1;
            x1 = x2;
            x2 = temp;
        }

        if (y1 < y2) {
            temp = y1;
            y1 = y2;
            y2 = temp;
        }

        float a = (float) (color >> 24 & 255) / 255.0F;
        float r = (float) (color >> 16 & 255) / 255.0F;
        float g = (float) (color >> 8 & 255) / 255.0F;
        float b = (float) (color & 255) / 255.0F;

        Tessellator tessellator = Tessellator.instance;
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glColor4f(r, g, b, a);
        tessellator.startDrawingQuads();
        tessellator.addVertex((double) x1, (double) y2, 0.0D);
        tessellator.addVertex((double) x2, (double) y2, 0.0D);
        tessellator.addVertex((double) x2, (double) y1, 0.0D);
        tessellator.addVertex((double) x1, (double) y1, 0.0D);
        tessellator.draw();
    }
}
