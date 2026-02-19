package io.github.tofithepuppycat.pouches.network;

import io.github.tofithepuppycat.pouches.client.ClientPouchData;
import io.github.tofithepuppycat.pouches.client.SelectionWheelHudOverlay;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class SyncPouchPacket {
    private final UUID playerId;
    private final ItemStack[] items;
    private final int[] slotCounts;

    public SyncPouchPacket(UUID playerId, ItemStack[] items, int[] slotCounts) {
        this.playerId = playerId;
        this.items = items;
        this.slotCounts = slotCounts;
    }

    public static void encode(SyncPouchPacket packet, FriendlyByteBuf buf) {
        buf.writeUUID(packet.playerId);
        buf.writeInt(packet.items.length);
        for (ItemStack item : packet.items) {
            buf.writeItem(item);
        }
        buf.writeInt(packet.slotCounts.length);
        for (int count : packet.slotCounts) {
            buf.writeInt(count);
        }
    }

    public static SyncPouchPacket decode(FriendlyByteBuf buf) {
        UUID playerId = buf.readUUID();
        int length = buf.readInt();
        ItemStack[] items = new ItemStack[length];
        for (int i = 0; i < length; i++) {
            items[i] = buf.readItem();
        }
        int slotCountsLength = buf.readInt();
        int[] slotCounts = new int[slotCountsLength];
        for (int i = 0; i < slotCountsLength; i++) {
            slotCounts[i] = buf.readInt();
        }
        return new SyncPouchPacket(playerId, items, slotCounts);
    }

    public static void handle(SyncPouchPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // Update client-side cache
            ClientPouchData.updatePouchData(packet.playerId, packet.items, packet.slotCounts);
            
            // Adjust the current pouch index if it's out of range (e.g., pouch was unequipped)
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null && packet.playerId.equals(mc.player.getUUID())) {
                int availablePouches = packet.slotCounts.length;
                int currentIndex = SelectionWheelHudOverlay.getCurrentPouchIndex();
                
                // If the current index is invalid, reset to the first pouch (or 0 if no pouches)
                if (currentIndex >= availablePouches) {
                    SelectionWheelHudOverlay.setCurrentPouchIndex(Math.max(0, availablePouches - 1));
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
