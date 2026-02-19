package io.github.tofithepuppycat.pouches.client;

import io.github.tofithepuppycat.pouches.Pouches;
import io.github.tofithepuppycat.pouches.network.NetworkHandler;
import io.github.tofithepuppycat.pouches.network.TransferToPouchPacket;
import io.github.tofithepuppycat.pouches.util.PouchHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Pouches.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PouchItemStorage {
    private static boolean prevSneaking = false;
    private static boolean initialized = false;
    private static long lastCycleTime = 0L;
    private static final long CYCLE_COOLDOWN_MS = 200; // debounce crouch toggles

    @SubscribeEvent
    public static void onKeyRelease(InputEvent.Key event) {
        if (event.getAction() != org.lwjgl.glfw.GLFW.GLFW_RELEASE) {
            return;
        }

        // Check if the released key matches the configured keybind
        if (event.getKey() != ClientEvents.ModBus.SHOW_IMAGE_KEY.getKey().getValue()) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;

        if (player == null) {
            return;
        }

        // Get the selected pouch slot from the wheel overlay
        int pouchSlot = SelectionWheelHudOverlay.getSelectedSlot();

        // Only proceed if a valid slot is selected
        if (pouchSlot < 0) {
            return;
        }

        // Get the currently selected hotbar slot
        int hotbarSlot = player.getInventory().selected;

        // Get the current pouch index
        int pouchIndex = SelectionWheelHudOverlay.getCurrentPouchIndex();

        // Play swap sound
        player.playSound(SoundEvents.ARMOR_EQUIP_LEATHER, 0.6f, 1.6f);

        // Allow transferring even if hotbar is empty (for swapping back)
        // Send packet to server to perform the transfer
        NetworkHandler.INSTANCE.sendToServer(new TransferToPouchPacket(hotbarSlot, pouchIndex, pouchSlot));
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        // Ensure player starts with pouch 0 once when they appear on client
        if (!initialized) {
            SelectionWheelHudOverlay.setCurrentPouchIndex(0);
            initialized = true;
        }

        // Only allow crouch cycling when the overlay is visible (key is pressed)
        if (ClientEvents.ModBus.SHOW_IMAGE_KEY.isDown()) {
            boolean sneaking = mc.player.isCrouching();
            long now = System.currentTimeMillis();

            // Detect edge: just started sneaking -> cycle to next pouch
            if (sneaking && !prevSneaking && now - lastCycleTime >= CYCLE_COOLDOWN_MS) {
                int available = PouchHelper.getAvailablePouches(mc.player);
                if (available > 0) {
                    int next = (SelectionWheelHudOverlay.getCurrentPouchIndex() + 1) % available;
                    SelectionWheelHudOverlay.setCurrentPouchIndex(next);
                    lastCycleTime = now;
                }
            }

            prevSneaking = sneaking;
        } else {
            // Reset sneaking state when overlay is not visible
            prevSneaking = false;
        }
    }
}
