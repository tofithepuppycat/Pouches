package io.github.tofithepuppycat.pouches.capability;

import io.github.tofithepuppycat.pouches.inventory.PouchInventory;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;

public class PouchCapability {
    private PouchInventory inventory;
    // pouchSlotCounts stores the slot count for each equipped pouch in order
    // Default to three pouches with 2 slots each
    private int[] pouchSlotCounts = new int[] {2, 2, 2};

    public PouchCapability() {
        this.inventory = new PouchInventory(getTotalSlots());
    }
    /**
     * Converts a pouch index and pouch slot to an absolute inventory index
     */
    public int getAbsoluteSlotIndex(int pouchIndex, int pouchSlot) {
        if (pouchIndex < 0 || pouchIndex >= pouchSlotCounts.length) return -1;
        if (pouchSlot < 0 || pouchSlot >= pouchSlotCounts[pouchIndex]) return -1;
        int idx = 0;
        for (int i = 0; i < pouchIndex; i++) idx += pouchSlotCounts[i];
        return idx + pouchSlot;
    }

    /**
     * Gets the number of slots for a specific pouch
     */
    public int getSlotsForPouch(int pouchIndex) {
        if (pouchIndex < 0 || pouchIndex >= pouchSlotCounts.length) return 0;
        return pouchSlotCounts[pouchIndex];
    }

    public int getPouchCount() {
        return pouchSlotCounts.length;
    }

    public PouchInventory getInventory() {
        return inventory;
    }

    public int getTotalSlots() {
        int sum = 0;
        for (int s : pouchSlotCounts) sum += s;
        return sum;
    }

    /**
     * Replace pouch slot counts (e.g., when curios change) and resize inventory accordingly.
     */
    public void setPouchSlotCounts(int[] counts) {
        if (counts == null) counts = new int[0];
        int newSize = 0;
        for (int c : counts) newSize += Math.max(0, c);

        // Preserve existing items as much as possible by linear copy
        PouchInventory newInventory = new PouchInventory(newSize);
        int limit = Math.min(inventory.getContainerSize(), newSize);
        for (int i = 0; i < limit; i++) {
            newInventory.setItem(i, inventory.getItem(i));
        }

        this.pouchSlotCounts = counts.clone();
        this.inventory = newInventory;
    }

    private void resizeInventory() {
        int newSize = getTotalSlots();
        if (newSize != inventory.getContainerSize()) {
            PouchInventory newInventory = new PouchInventory(newSize);
            for (int i = 0; i < Math.min(inventory.getContainerSize(), newSize); i++) {
                newInventory.setItem(i, inventory.getItem(i));
            }
            this.inventory = newInventory;
        }
    }

    public void saveNBTData(CompoundTag tag) {
        CompoundTag inventoryTag = new CompoundTag();
        inventory.save(inventoryTag);
        tag.put("Inventory", inventoryTag);
        // Save pouch slot counts
        ListTag counts = new ListTag();
        for (int c : pouchSlotCounts) {
            CompoundTag t = new CompoundTag();
            t.putInt("Slots", c);
            counts.add(t);
        }
        tag.put("PouchSlotCounts", counts);
    }

    public void loadNBTData(CompoundTag tag) {
        if (tag.contains("PouchSlotCounts")) {
            ListTag counts = tag.getList("PouchSlotCounts", 10);
            int[] arr = new int[counts.size()];
            for (int i = 0; i < counts.size(); i++) {
                arr[i] = counts.getCompound(i).getInt("Slots");
            }
            setPouchSlotCounts(arr);
        }
        if (tag.contains("Inventory")) {
            inventory.load(tag.getCompound("Inventory"));
        }
    }
}
