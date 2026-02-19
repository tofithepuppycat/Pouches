package io.github.tofithepuppycat.pouches.client;

import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Client-side cache for pouch inventory data
 */
public class ClientPouchData {
    private static final Map<UUID, ItemStack[]> pouchCache = new HashMap<>();
    private static final Map<UUID, int[]> slotCountsCache = new HashMap<>();

    public static void updatePouchData(UUID playerId, ItemStack[] items, int[] slotCounts) {
        pouchCache.put(playerId, items);
        slotCountsCache.put(playerId, slotCounts);
    }

    public static ItemStack getItem(UUID playerId, int slot) {
        ItemStack[] items = pouchCache.get(playerId);
        if (items != null && slot >= 0 && slot < items.length) {
            return items[slot];
        }
        return ItemStack.EMPTY;
    }

    /**
     * Converts a pouch index and pouch slot to an absolute inventory index
     */
    private static int getAbsoluteSlotIndex(UUID playerId, int pouchIndex, int pouchSlot) {
        int[] slotCounts = slotCountsCache.get(playerId);
        if (slotCounts == null || pouchIndex < 0 || pouchIndex >= slotCounts.length) {
            return -1;
        }
        if (pouchSlot < 0 || pouchSlot >= slotCounts[pouchIndex]) {
            return -1;
        }
        int idx = 0;
        for (int i = 0; i < pouchIndex; i++) {
            idx += slotCounts[i];
        }
        return idx + pouchSlot;
    }

    /**
     * Gets an item from a specific pouch slot for a given pouch index
     */
    public static ItemStack getItemInPouchSlot(UUID playerId, int pouchIndex, int pouchSlot) {
        int absoluteSlot = getAbsoluteSlotIndex(playerId, pouchIndex, pouchSlot);
        if (absoluteSlot < 0) {
            return ItemStack.EMPTY;
        }
        return getItem(playerId, absoluteSlot);
    }

    /**
     * Gets the number of slots for a specific pouch index
     */
    public static int getSlotsInPouch(UUID playerId, int pouchIndex) {
        int[] slotCounts = slotCountsCache.get(playerId);
        if (slotCounts == null || pouchIndex < 0 || pouchIndex >= slotCounts.length) {
            return 2; // Default to 2 slots
        }
        return slotCounts[pouchIndex];
    }

    public static void clear(UUID playerId) {
        pouchCache.remove(playerId);
        slotCountsCache.remove(playerId);
    }
}
