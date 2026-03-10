package io.github.tofithepuppycat.pouches.util;

import java.util.concurrent.atomic.AtomicReference;

import io.github.tofithepuppycat.pouches.item.PouchItem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;

public class PouchHelper {
    /**
     * Gets an item from a specific pouch slot for a given pouch index
     */
    public static ItemStack getItemInPouchSlot(Player player, int pouchIndex, int pouchSlot) {
        ItemStack pouchStack = getEquippedPouch(player, pouchIndex);
        if (pouchStack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        return PouchItem.getItemInSlot(pouchStack, pouchSlot);
    }

    /**
     * Gets number of slots for a specific pouch index on the player
     */
    public static int getSlotsInPouch(Player player, int pouchIndex) {
        ItemStack pouchStack = getEquippedPouch(player, pouchIndex);
        if (pouchStack.isEmpty() || !(pouchStack.getItem() instanceof PouchItem pouchItem)) {
            return 2; // Default
        }
        return pouchItem.getSlotsInPouch();
    }

    /**
     * Sets an item in a specific pouch slot for a given pouch index.
     * pouchIndex is the Nth actual PouchItem, skipping empty Curios slots.
     */
    public static void setItemInPouchSlot(Player player, int pouchIndex, int pouchSlot, ItemStack stack) {
        try {
            CuriosApi.getCuriosInventory(player).ifPresent(handler -> {
                handler.getStacksHandler("pouch").ifPresent(stacksHandler -> {
                    int found = 0;
                    for (int i = 0; i < stacksHandler.getSlots(); i++) {
                        ItemStack pouchStack = stacksHandler.getStacks().getStackInSlot(i);
                        if (!pouchStack.isEmpty() && pouchStack.getItem() instanceof PouchItem) {
                            if (found == pouchIndex) {
                                PouchItem.setItemInSlot(pouchStack, pouchSlot, stack);
                                stacksHandler.getStacks().setStackInSlot(i, pouchStack);
                                return;
                            }
                            found++;
                        }
                    }
                });
            });
        } catch (Exception e) {
            // Fallback - no action
        }
    }

    /**
     * Gets the equipped pouch ItemStack at the given index.
     * pouchIndex is the Nth actual PouchItem, skipping empty Curios slots
     * (which may exist from enchantment-granted extra slots).
     */
    public static ItemStack getEquippedPouch(Player player, int pouchIndex) {
        AtomicReference<ItemStack> result = new AtomicReference<>(ItemStack.EMPTY);
        
        try {
            CuriosApi.getCuriosInventory(player).ifPresent(handler -> {
                handler.getStacksHandler("pouch").ifPresent(stacksHandler -> {
                    int found = 0;
                    for (int i = 0; i < stacksHandler.getSlots(); i++) {
                        ItemStack stack = stacksHandler.getStacks().getStackInSlot(i);
                        if (!stack.isEmpty() && stack.getItem() instanceof PouchItem) {
                            if (found == pouchIndex) {
                                result.set(stack);
                                return;
                            }
                            found++;
                        }
                    }
                });
            });
        } catch (Exception e) {
            // Fallback to empty if Curios not available
        }
        
        return result.get();
    }

    /**
     * Gets the number of available pouches based on equipped Curios
     */
    public static int getAvailablePouches(Player player) {
        try {
            // Use Curios API to check equipped pouches
            return (int) top.theillusivec4.curios.api.CuriosApi.getCuriosInventory(player)
                    .map(handler -> handler.getStacksHandler("pouch")
                            .map(stacksHandler -> {
                                int count = 0;
                                for (int i = 0; i < stacksHandler.getSlots(); i++) {
                                    ItemStack stack = stacksHandler.getStacks().getStackInSlot(i);
                                    if (!stack.isEmpty() && stack.getItem() instanceof PouchItem) {
                                        count++;
                                    }
                                }
                                return count;
                            })
                            .orElse(0))
                    .orElse(0);
        } catch (Exception e) {
            // Fallback if Curios is not available
            return 0;
        }
    }

    /**
     * Gets all items from all equipped pouches for syncing
     * Returns an array where each element represents items from one pouch
     */
    public static ItemStack[][] getAllPouchItems(Player player) {
        int pouchCount = getAvailablePouches(player);
        ItemStack[][] allItems = new ItemStack[pouchCount][];
        
        for (int i = 0; i < pouchCount; i++) {
            ItemStack pouchStack = getEquippedPouch(player, i);
            if (!pouchStack.isEmpty()) {
                allItems[i] = PouchItem.getAllItems(pouchStack);
            } else {
                allItems[i] = new ItemStack[0];
            }
        }
        
        return allItems;
    }

    /**
     * Gets the slot counts for all equipped pouches
     */
    public static int[] getAllPouchSlotCounts(Player player) {
        int pouchCount = getAvailablePouches(player);
        int[] slotCounts = new int[pouchCount];
        
        for (int i = 0; i < pouchCount; i++) {
            slotCounts[i] = getSlotsInPouch(player, i);
        }
        
        return slotCounts;
    }

    /**
     * Gets the color of a pouch at the given index.
     * Returns -1 if the pouch is not dyed, so callers can skip tinting.
     */
    public static int getPouchColor(Player player, int pouchIndex) {
        ItemStack pouchStack = getEquippedPouch(player, pouchIndex);
        if (!pouchStack.isEmpty() && pouchStack.getItem() instanceof DyeableLeatherItem dyeable
                && dyeable.hasCustomColor(pouchStack)) {
            return dyeable.getColor(pouchStack);
        }
        return -1; // Not dyed
    }
}
