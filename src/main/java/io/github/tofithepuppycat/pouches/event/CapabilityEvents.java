package io.github.tofithepuppycat.pouches.event;

import io.github.tofithepuppycat.pouches.Pouches;
import io.github.tofithepuppycat.pouches.capability.PouchCapability;
import io.github.tofithepuppycat.pouches.capability.PouchCapabilityProvider;
import io.github.tofithepuppycat.pouches.network.NetworkHandler;
import io.github.tofithepuppycat.pouches.network.SyncPouchPacket;
import io.github.tofithepuppycat.pouches.util.PouchHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

@Mod.EventBusSubscriber(modid = Pouches.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CapabilityEvents {

    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player) {
            if (!event.getObject().getCapability(PouchCapabilityProvider.POUCH_CAPABILITY).isPresent()) {
                event.addCapability(new ResourceLocation(Pouches.MODID, "pouch_inventory"),
                        new PouchCapabilityProvider());
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (event.isWasDeath()) {
            event.getOriginal().getCapability(PouchCapabilityProvider.POUCH_CAPABILITY).ifPresent(oldStore -> {
                event.getEntity().getCapability(PouchCapabilityProvider.POUCH_CAPABILITY).ifPresent(newStore -> {
                    // Copy inventory on death/respawn
                    CompoundTag tag = new CompoundTag();
                    oldStore.saveNBTData(tag);
                    newStore.loadNBTData(tag);
                });
            });
        }
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            // Sync pouch data from equipped curios to client when player logs in
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

    @Mod.EventBusSubscriber(modid = Pouches.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ModBusEvents {
        @SubscribeEvent
        public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
            event.register(PouchCapability.class);
        }
    }
}
