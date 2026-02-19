package io.github.tofithepuppycat.pouches.inventory;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;

public class PouchInventory extends SimpleContainer {
    private static final int DEFAULT_SLOTS = 2;
    private static final String TAG_ITEMS = "Items";
    private static final String TAG_SLOT = "Slot";

    public PouchInventory(int size) {
        super(size);
    }

    /**
     * Loads the inventory from NBT data
     */
    public void load(CompoundTag tag) {
        ListTag itemsList = tag.getList(TAG_ITEMS, 10);
        for (int i = 0; i < itemsList.size(); i++) {
            CompoundTag itemTag = itemsList.getCompound(i);
            int slot = itemTag.getInt(TAG_SLOT);
            if (slot >= 0 && slot < this.getContainerSize()) {
                this.setItem(slot, ItemStack.of(itemTag));
            }
        }
    }

    /**
     * Saves the inventory to NBT data
     */
    public CompoundTag save(CompoundTag tag) {
        ListTag itemsList = new ListTag();
        for (int i = 0; i < this.getContainerSize(); i++) {
            ItemStack stack = this.getItem(i);
            if (!stack.isEmpty()) {
                CompoundTag itemTag = new CompoundTag();
                itemTag.putInt(TAG_SLOT, i);
                stack.save(itemTag);
                itemsList.add(itemTag);
            }
        }
        tag.put(TAG_ITEMS, itemsList);
        return tag;
    }
}
