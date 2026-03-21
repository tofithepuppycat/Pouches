package io.github.tofithepuppycat.pouches.network;

import io.github.tofithepuppycat.pouches.client.ClientPouchData;
import io.github.tofithepuppycat.pouches.client.SelectionWheelHudOverlay;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

public class SyncPouchPacket {
    private final UUID playerId;
    private final ItemStack[] items;
    private final int[] slotCounts;

    public SyncPouchPacket(UUID playerId, ItemStack[] items, int[] slotCounts) {
        this.playerId = Objects.requireNonNull(playerId, "playerId");
        this.items = Objects.requireNonNull(items, "items");
        this.slotCounts = Objects.requireNonNull(slotCounts, "slotCounts");
    }

    public static void encode(SyncPouchPacket packet, FriendlyByteBuf buf) {
        UUID playerId = Objects.requireNonNull(packet.playerId, "playerId");
        buf.writeUUID(playerId);
        buf.writeInt(packet.items.length);
        for (ItemStack item : packet.items) {
            writeCompactItem(buf, item);
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
            items[i] = readCompactItem(buf);
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
            if (mc.player == null) {
                return;
            }
            var clientPlayer = mc.player;

            if (packet.playerId.equals(clientPlayer.getUUID())) {
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

    private static void writeCompactItem(FriendlyByteBuf buf, ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            buf.writeBoolean(false);
            return;
        }

        buf.writeBoolean(true);
        ResourceLocation key = ForgeRegistries.ITEMS.getKey(stack.getItem());
        buf.writeResourceLocation(key != null ? key : Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(Items.AIR)));
        buf.writeVarInt(stack.getCount());
    }

    private static ItemStack readCompactItem(FriendlyByteBuf buf) {
        if (!buf.readBoolean()) {
            return ItemStack.EMPTY;
        }

        ResourceLocation itemKey = buf.readResourceLocation();
        int count = Math.max(1, buf.readVarInt());
        Item item = ForgeRegistries.ITEMS.getValue(itemKey);
        if (item == null || item == Items.AIR) {
            return ItemStack.EMPTY;
        }

        return new ItemStack(item, count);
    }
}
