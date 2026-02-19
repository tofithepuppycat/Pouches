package io.github.tofithepuppycat.pouches.client;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.tofithepuppycat.pouches.Pouches;
import io.github.tofithepuppycat.pouches.util.PouchHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

public final class SelectionWheelHudOverlay {
    private static final ResourceLocation WHEEL = new ResourceLocation(
            Pouches.MODID,
            "textures/gui/wheel.png"
    );

    private static final ResourceLocation SLOT_0_SELECTED = new ResourceLocation(
            Pouches.MODID,
            "textures/gui/wheel_cap2_slot_0_selected.png"
    );

    private static final ResourceLocation SLOT_1_SELECTED = new ResourceLocation(
            Pouches.MODID,
            "textures/gui/wheel_cap2_slot_1_selected.png"
    );

    private static final ResourceLocation[] SLOT_3_SELECTED = new ResourceLocation[] {
        new ResourceLocation(Pouches.MODID, "textures/gui/wheel_cap3_slot_0_selected.png"),
        new ResourceLocation(Pouches.MODID, "textures/gui/wheel_cap3_slot_1_selected.png"),
        new ResourceLocation(Pouches.MODID, "textures/gui/wheel_cap3_slot_2_selected.png")
    };

    private static final ResourceLocation[] SLOT_4_SELECTED = new ResourceLocation[] {
        new ResourceLocation(Pouches.MODID, "textures/gui/wheel_cap4_slot_0_selected.png"),
        new ResourceLocation(Pouches.MODID, "textures/gui/wheel_cap4_slot_1_selected.png"),
        new ResourceLocation(Pouches.MODID, "textures/gui/wheel_cap4_slot_2_selected.png"),
        new ResourceLocation(Pouches.MODID, "textures/gui/wheel_cap4_slot_3_selected.png")
    };

    private static final int TEX_W = 256;
    private static final int TEX_H = 256;

    private static final float SCREEN_FRACTION = 0.35f;

    private static int selectedSlot = -1;
    private static int previousSelectedSlot = -1;
    private static int currentPouchIndex = 0;
    
    // State tracking for key press and mouse position reset
    private static boolean wasKeyDown = false;
    private static double initialMouseX = 0;
    private static double initialMouseY = 0;

    public static final IGuiOverlay HUD_IMAGE = (ForgeGui gui, GuiGraphics gfx, float partialTick, int width, int height) -> {
        boolean isKeyDown = ClientEvents.ModBus.SHOW_IMAGE_KEY.isDown();
        
        if (!isKeyDown) {
            // Reset state when key is released
            wasKeyDown = false;
            previousSelectedSlot = -1;
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;

        // Don't show overlay if player doesn't have any pouch curios equipped
        if (player == null || PouchHelper.getAvailablePouches(player) <= 0) {
            return;
        }

        int minDim = Math.min(width, height);
        float targetPx = minDim * SCREEN_FRACTION;
        float scale = targetPx / (float) TEX_W;

        float scaledW = TEX_W * scale;
        float scaledH = TEX_H * scale;

        float x = (width - scaledW) / 2.0f;
        float y = (height - scaledH) / 2.0f;

        float centerX = width / 2.0f;
        float centerY = height / 2.0f;

        // Calculate mouse position (scaled coordinates)
        double mouseX = mc.mouseHandler.xpos() * (double)mc.getWindow().getGuiScaledWidth() / (double)mc.getWindow().getScreenWidth();
        double mouseY = mc.mouseHandler.ypos() * (double)mc.getWindow().getGuiScaledHeight() / (double)mc.getWindow().getScreenHeight();
        
        // Detect key press (transition from not pressed to pressed) and reset mouse position
        if (!wasKeyDown && isKeyDown) {
            initialMouseX = mouseX;
            initialMouseY = mouseY;
            wasKeyDown = true;
        }

        // Calculate delta from initial mouse position (where key was pressed)
        float deltaX = (float)(mouseX - initialMouseX);
        float deltaY = (float)(mouseY - initialMouseY);
        float distanceFromCenter = (float)Math.sqrt(deltaX * deltaX + deltaY * deltaY);

        // Dead zone in the center - no selection if mouse is too close to center
        // This makes selection easier for both low and high sensitivity users
        float deadZoneRadius = Math.min(scaledW, scaledH) * 0.16f;
        
        // Calculate angle from center to mouse
        double angle = Math.toDegrees(Math.atan2(deltaY, deltaX));
        if (angle < 0) angle += 360;
        angle = (angle + 90) % 360; // Rotate so top is 0°

        // Determine how many slots the current pouch has
        int slotCount = 2;
        if (player != null) {
            slotCount = Math.max(1, ClientPouchData.getSlotsInPouch(player.getUUID(), currentPouchIndex));
        }

        // Calculate which slot is selected based on angle
        // Only select if mouse is outside the dead zone
        if (distanceFromCenter > deadZoneRadius) {
            // For 4-slot pouches, add 45-degree offset to align with diagonal slots
            double adjustedAngle = angle;
            if (slotCount == 4) {
                // Slot positions: 0=upper-left, 1=upper-right, 2=lower-right, 3=lower-left
                adjustedAngle = (angle + 45) % 360;
            }
            float degreesPerSlot = 360.0f / slotCount;
            int newSelectedSlot = (int)((adjustedAngle + degreesPerSlot / 2) % 360 / degreesPerSlot) % slotCount;
            
            // Play sound if selection changed
            if (newSelectedSlot != previousSelectedSlot && newSelectedSlot >= 0 && mc.level != null && mc.player != null) {
                mc.level.playLocalSound(mc.player.getX(), mc.player.getY(), mc.player.getZ(),
                    SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.PLAYERS, 0.3f, 2.5f, false);
                previousSelectedSlot = newSelectedSlot;
            }
            selectedSlot = newSelectedSlot;
        } else {
            // Mouse is in dead zone - no selection
            if (selectedSlot != -1) {
                previousSelectedSlot = -1;
            }
            selectedSlot = -1;
        }

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, WHEEL);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        // Set semi-transparent (70% opacity)
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 0.7f);

        gfx.pose().pushPose();
        gfx.pose().translate(x, y, 0);
        gfx.pose().scale(scale, scale, 1.0f);

        gfx.blit(WHEEL, 0, 0, 0, 0, TEX_W, TEX_H, TEX_W, TEX_H);

        // Render selection highlight
        if (selectedSlot >= 0) {
            ResourceLocation selectionTexture = null;
            if (slotCount == 2) {
                selectionTexture = selectedSlot == 0 ? SLOT_0_SELECTED : SLOT_1_SELECTED;
            } else if (slotCount == 3 && selectedSlot < SLOT_3_SELECTED.length) {
                selectionTexture = SLOT_3_SELECTED[selectedSlot];
            } else if (slotCount == 4 && selectedSlot < SLOT_4_SELECTED.length) {
                selectionTexture = SLOT_4_SELECTED[selectedSlot];
            }

            if (selectionTexture != null) {
                RenderSystem.setShaderTexture(0, selectionTexture);
                gfx.blit(selectionTexture, 0, 0, 0, 0, TEX_W, TEX_H, TEX_W, TEX_H);
            }
        }

        gfx.pose().popPose();

        // Reset shader color for item rendering
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

        // Render items in pouch slots
        ItemStack selectedStack = ItemStack.EMPTY;
        int selectedSlotX = 0;
        int selectedSlotY = 0;

        if (player != null) {
            // Calculate item scale with minimum and maximum size constraints
            float itemScale = Math.min(Math.max(scale * 2.0f, 1.0f), 2.0f);
            int scaledItemSize = (int)(16 * itemScale);

            // Radius in texture coordinates where items should be placed
            float radius = TEX_W * 0.28f;

            for (int i = 0; i < slotCount; i++) {
                // Get item from the currently selected pouch
                ItemStack stack = ClientPouchData.getItemInPouchSlot(player.getUUID(), currentPouchIndex, i);

                // Compute slot angle
                // For 4-slot pouches: 0=upper-left(225°), 1=upper-right(315°), 2=lower-right(45°), 3=lower-left(135°)
                double slotAngleDeg;
                if (slotCount == 4) {
                    // Start at 225° (upper-left) and rotate clockwise: 225, 315, 45, 135
                    slotAngleDeg = (225 + i * 90) % 360;
                } else {
                    // Other pouches: start at top (-90°)
                    slotAngleDeg = i * (360.0 / slotCount) - 90.0;
                }
                double slotAngleRad = Math.toRadians(slotAngleDeg);

                // Position relative to wheel center in texture units
                float texPosX = TEX_W / 2.0f + (float)Math.cos(slotAngleRad) * radius;
                float texPosY = TEX_H / 2.0f + (float)Math.sin(slotAngleRad) * radius;

                // Convert texture coords to screen coords and center the item
                float itemX = x + (texPosX * scale) - (scaledItemSize / 2.0f);
                float itemY = y + (texPosY * scale) - (scaledItemSize / 2.0f);

                if (!stack.isEmpty()) {
                    gfx.pose().pushPose();
                    gfx.pose().translate(itemX, itemY, 100);
                    gfx.pose().scale(itemScale, itemScale, 1.0f);
                    gfx.renderItem(stack, 0, 0);
                    gfx.renderItemDecorations(mc.font, stack, 0, 0);
                    gfx.pose().popPose();
                }

                if (i == selectedSlot) {
                    selectedStack = stack;
                    selectedSlotX = (int)itemX;
                    selectedSlotY = (int)itemY;
                }
            }

            if (selectedSlot >= 0 && !selectedStack.isEmpty()) {
                int tooltipX = selectedSlotX + scaledItemSize + 4;
                int tooltipY = selectedSlotY;

                if (slotCount == 4) {
                    if (selectedSlot == 0) {
                        tooltipX = selectedSlotX - 100;
                    }
                    else if (selectedSlot == 1) {
                        tooltipX = selectedSlotX + scaledItemSize + 4;
                    }
                    else if (selectedSlot == 2) {
                        tooltipX = selectedSlotX;
                        tooltipY = selectedSlotY + scaledItemSize + 4;
                    }
                    else if (selectedSlot == 3) {
                        tooltipX = selectedSlotX - 100;
                    }
                }

                gfx.renderTooltip(mc.font, selectedStack, tooltipX, tooltipY);
            }
        }

        // Draw cursor indicator line from center to mouse position
        // Clamp the line endpoint to stay within the wheel radius
        float maxLineLength = Math.min(scaledW, scaledH) * 0.45f;
        float lineLength = Math.min(distanceFromCenter, maxLineLength);
        
        // Calculate line endpoint following mouse direction
        float lineEndX = centerX;
        float lineEndY = centerY;
        if (distanceFromCenter > 0.1f) { // Avoid division by zero
            float normalizedDeltaX = deltaX / distanceFromCenter;
            float normalizedDeltaY = deltaY / distanceFromCenter;
            lineEndX = centerX + normalizedDeltaX * lineLength;
            lineEndY = centerY + normalizedDeltaY * lineLength;
        }
        
        // Draw the indicator line (only if outside dead zone)
        if (distanceFromCenter > deadZoneRadius * 0.5f) {
            gfx.pose().pushPose();
            // Draw center dot
            gfx.fill((int)centerX - 2, (int)centerY - 2, (int)centerX + 2, (int)centerY + 2, 0xFFFFFFFF);
            // Draw line
            drawLine(gfx, centerX, centerY, lineEndX, lineEndY, 0xFFFFFFFF);
            // Draw endpoint dot
            gfx.fill((int)lineEndX - 2, (int)lineEndY - 2, (int)lineEndX + 2, (int)lineEndY + 2, 0xFFFFFFFF);
            gfx.pose().popPose();
        }

        RenderSystem.disableBlend();
    };

    public static int getSelectedSlot() {
        return selectedSlot;
    }

    public static int getCurrentPouchIndex() {
        return currentPouchIndex;
    }

    public static void setCurrentPouchIndex(int index) {
        Minecraft mc = Minecraft.getInstance();
        int available = 0;
        if (mc.player != null) {
            available = PouchHelper.getAvailablePouches(mc.player);
        }

        if (available <= 0) {
            currentPouchIndex = 0;
            return;
        }

        // Clamp index into range [0, available-1]
        if (index < 0) index = (index % available + available) % available;
        if (index >= available) index = index % available;
        currentPouchIndex = index;
    }

    /**
     * Helper method to draw a line using filled rectangles
     */
    private static void drawLine(GuiGraphics gfx, float x1, float y1, float x2, float y2, int color) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        float length = (float)Math.sqrt(dx * dx + dy * dy);
        float angle = (float)Math.atan2(dy, dx);
        
        gfx.pose().pushPose();
        gfx.pose().translate(x1, y1, 0);
        gfx.pose().rotateAround(new org.joml.Quaternionf().rotateZ(angle), 0, 0, 0);
        gfx.fill(0, -1, (int)length, 1, color);
        gfx.pose().popPose();
    }

    private SelectionWheelHudOverlay() {
    }
}