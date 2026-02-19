package io.github.tofithepuppycat.pouches.network;

import io.github.tofithepuppycat.pouches.util.PouchHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Supplier;

public class TransferToPouchPacket {
    private final int hotbarSlot;
    private final int pouchIndex;
    private final int pouchSlot;

    public TransferToPouchPacket(int hotbarSlot, int pouchIndex, int pouchSlot) {
        this.hotbarSlot = hotbarSlot;
        this.pouchIndex = pouchIndex;
        this.pouchSlot = pouchSlot;
    }

    public static void encode(TransferToPouchPacket packet, FriendlyByteBuf buf) {
        buf.writeInt(packet.hotbarSlot);
        buf.writeInt(packet.pouchIndex);
        buf.writeInt(packet.pouchSlot);
    }

    public static TransferToPouchPacket decode(FriendlyByteBuf buf) {
        return new TransferToPouchPacket(buf.readInt(), buf.readInt(), buf.readInt());
    }

    public static void handle(TransferToPouchPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            // Get the item from the hotbar
            ItemStack hotbarItem = player.getInventory().getItem(packet.hotbarSlot);

            // Get the current item in the pouch slot for the selected pouch
            ItemStack pouchItem = PouchHelper.getItemInPouchSlot(player, packet.pouchIndex, packet.pouchSlot);

            // Swap the items in the selected pouch (this now saves to pouch NBT)
            PouchHelper.setItemInPouchSlot(player, packet.pouchIndex, packet.pouchSlot, hotbarItem.copy());
            player.getInventory().setItem(packet.hotbarSlot, pouchItem.copy());

            // Mark inventory as changed
            player.inventoryMenu.broadcastChanges();

            // Sync pouch data to client - now reading from equipped pouch items
            ItemStack[][] allPouchItems = PouchHelper.getAllPouchItems(player);
            int[] slotCounts = PouchHelper.getAllPouchSlotCounts(player);
            
            // Flatten the 2D array for packet transfer
            int totalSlots = 0;
            for (int[] count : new int[][]{slotCounts}) {
                for (int c : count) {
                    totalSlots += c;
                }
            }
            
            ItemStack[] flattenedItems = new ItemStack[totalSlots];
            int index = 0;
            for (ItemStack[] pouchItems : allPouchItems) {
                for (ItemStack item : pouchItems) {
                    flattenedItems[index++] = item;
                }
            }
            
            NetworkHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), 
                    new SyncPouchPacket(player.getUUID(), flattenedItems, slotCounts));
        });
        ctx.get().setPacketHandled(true);
    }
}
