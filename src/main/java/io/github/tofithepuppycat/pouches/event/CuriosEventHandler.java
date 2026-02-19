package io.github.tofithepuppycat.pouches.event;

import io.github.tofithepuppycat.pouches.Pouches;
import io.github.tofithepuppycat.pouches.network.NetworkHandler;
import io.github.tofithepuppycat.pouches.network.SyncPouchPacket;
import io.github.tofithepuppycat.pouches.util.PouchHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import top.theillusivec4.curios.api.event.CurioChangeEvent;

@Mod.EventBusSubscriber(modid = Pouches.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CuriosEventHandler {

    @SubscribeEvent
    public static void onCurioChange(CurioChangeEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            // Sync pouch data from equipped curios to client
            try {
                ItemStack[][] allPouchItems = PouchHelper.getAllPouchItems(player);
                int[] slotCounts = PouchHelper.getAllPouchSlotCounts(player);
                
                // Flatten the 2D array for packet transfer
                int totalSlots = 0;
                for (int count : slotCounts) {
                    totalSlots += count;
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
            } catch (Exception e) {
                // If there's an error, send empty data
                NetworkHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player),
                        new SyncPouchPacket(player.getUUID(), new ItemStack[0], new int[0]));
            }
        }
    }
}
