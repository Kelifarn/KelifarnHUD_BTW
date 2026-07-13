package net.fabricmc.example;

import net.minecraft.src.*;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.util.List;

public class GuiBookmarkEditor extends GuiScreen {
    private final GuiScreen parentScreen;
    private final Bookmark editingBookmark;
    private final int bookmarkX;
    private final int bookmarkZ;
    private GuiTextField nameField;
    private String bookmarkName;
    private int selectedColor;
    private boolean isPermanent;

    private static final int[] COLORS = {
        0xFFFF3030, // Red
        0xFF30FF30, // Green
        0xFF5050FF, // Blue
        0xFFFFFF30, // Yellow
        0xFFFF30FF, // Purple
        0xFF30FFFF, // Cyan
        0xFFFF9930, // Orange
        0xFFFFFFFF  // White
    };

    public GuiBookmarkEditor(GuiScreen parentScreen, int x, int z) {
        this.parentScreen = parentScreen;
        this.editingBookmark = null;
        this.bookmarkX = x;
        this.bookmarkZ = z;
        this.bookmarkName = "Waypoint";
        this.selectedColor = COLORS[0];
        this.isPermanent = true;
    }

    public GuiBookmarkEditor(GuiScreen parentScreen, Bookmark bookmark) {
        this.parentScreen = parentScreen;
        this.editingBookmark = bookmark;
        this.bookmarkX = bookmark.x;
        this.bookmarkZ = bookmark.z;
        this.bookmarkName = bookmark.name;
        this.selectedColor = bookmark.color;
        this.isPermanent = bookmark.permanent;
    }

    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        this.buttonList.clear();

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        // Save and Cancel buttons
        this.buttonList.add(new GuiButton(0, centerX - 100, centerY + 50, 95, 20, "Save"));
        this.buttonList.add(new GuiButton(1, centerX + 5, centerY + 50, 95, 20, "Cancel"));

        // Permanent toggle button
        this.buttonList.add(new GuiButton(3, centerX - 100, centerY + 20, 200, 20, getPermanentButtonText()));

        if (editingBookmark != null) {
            this.buttonList.add(new GuiButton(2, centerX - 100, centerY + 75, 200, 20, "Delete"));
        }

        this.nameField = new GuiTextField(this.fontRenderer, centerX - 100, centerY - 25, 200, 20);
        this.nameField.setFocused(true);
        this.nameField.setMaxStringLength(32);
        this.nameField.setText(bookmarkName);
    }

    private String getPermanentButtonText() {
        return "Show on Minimap Edge: " + (isPermanent ? "ON" : "OFF");
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == 0) { // Save
            String name = nameField.getText().trim();
            if (name.isEmpty()) {
                name = "Waypoint";
            }
            if (editingBookmark != null) {
                editingBookmark.name = name;
                editingBookmark.color = selectedColor;
                editingBookmark.permanent = isPermanent;
            } else {
                Holder.bookmarks.add(new Bookmark(bookmarkX, bookmarkZ, name, selectedColor, isPermanent));
            }
            Holder.saveBookmarks();
            mc.displayGuiScreen(parentScreen);
        } else if (button.id == 1) { // Cancel
            mc.displayGuiScreen(parentScreen);
        } else if (button.id == 2) { // Delete
            if (editingBookmark != null) {
                Holder.bookmarks.remove(editingBookmark);
                Holder.saveBookmarks();
            }
            mc.displayGuiScreen(parentScreen);
        } else if (button.id == 3) { // Toggle Permanent
            isPermanent = !isPermanent;
            button.displayString = getPermanentButtonText();
        }
    }

    @Override
    protected void keyTyped(char character, int key) {
        this.nameField.textboxKeyTyped(character, key);

        if (key == Keyboard.KEY_RETURN) {
            for (Object obj : this.buttonList) {
                GuiButton btn = (GuiButton) obj;
                if (btn.id == 0) {
                    actionPerformed(btn);
                    break;
                }
            }
        } else if (key == Keyboard.KEY_ESCAPE) {
            for (Object obj : this.buttonList) {
                GuiButton btn = (GuiButton) obj;
                if (btn.id == 1) {
                    actionPerformed(btn);
                    break;
                }
            }
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int button) {
        super.mouseClicked(mouseX, mouseY, button);
        this.nameField.mouseClicked(mouseX, mouseY, button);

        int centerX = this.width / 2;
        int centerY = this.height / 2;
        int swatchWidth = 15;
        int swatchHeight = 15;
        int swatchSpacing = 8;
        int startX = centerX - 100;
        int startY = centerY + 1;

        for (int i = 0; i < COLORS.length; i++) {
            int x = startX + i * (swatchWidth + swatchSpacing);
            int y = startY;
            if (mouseX >= x && mouseX <= x + swatchWidth && mouseY >= y && mouseY <= y + swatchHeight) {
                selectedColor = COLORS[i];
                break;
            }
        }
    }

    @Override
    public void updateScreen() {
        this.nameField.updateCursorCounter();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        drawCenteredString(this.fontRenderer, editingBookmark != null ? "Edit Bookmark" : "Add Bookmark", centerX, centerY - 80, 0xFFFFFF);

        drawString(this.fontRenderer, "Coordinates: X: " + bookmarkX + ", Z: " + bookmarkZ, centerX - 100, centerY - 60, 0xA0A0A0);
        drawString(this.fontRenderer, "Name:", centerX - 100, centerY - 40, 0xA0A0A0);

        this.nameField.drawTextBox();

        drawString(this.fontRenderer, "Color:", centerX - 100, centerY - 10, 0xA0A0A0);

        // Draw color swatches
        int swatchWidth = 15;
        int swatchHeight = 15;
        int swatchSpacing = 8;
        int startX = centerX - 100;
        int startY = centerY + 1;

        for (int i = 0; i < COLORS.length; i++) {
            int x = startX + i * (swatchWidth + swatchSpacing);
            int y = startY;

            // Draw border: white if selected, black if not
            int borderCol = (COLORS[i] == selectedColor) ? 0xFFFFFFFF : 0xFF000000;
            drawRectInner(x - 1, y - 1, x + swatchWidth + 1, y + swatchHeight + 1, borderCol);
            drawRectInner(x, y, x + swatchWidth, y + swatchHeight, COLORS[i]);
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
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
