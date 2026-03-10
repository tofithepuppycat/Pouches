package io.github.tofithepuppycat.pouches.item;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import java.util.List;

public class PouchItem extends Item implements ICurioItem, DyeableLeatherItem {
    private static final int DEFAULT_SLOTS = 2;
    private int slotsInPouch = 2;

    public PouchItem(Properties properties) {
        this(properties, DEFAULT_SLOTS);
    }

    public PouchItem(Properties properties, int slotsInPouch) {
        super(properties);
        this.slotsInPouch = slotsInPouch;
    }

    public int getSlotsInPouch() {
        return slotsInPouch;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltipComponents, @NotNull TooltipFlag isAdvanced) {
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);
        tooltipComponents.add(Component.literal("Provides " + slotsInPouch + " extra inventory slots when equipped.").withStyle(net.minecraft.ChatFormatting.GRAY));

        // Show stored items
        if (stack.hasTag() && stack.getTag().contains("Items")) {
            ListTag items = stack.getTag().getList("Items", 10);
            if (items.size() > 0) {
                tooltipComponents.add(Component.literal("Contents:").withStyle(net.minecraft.ChatFormatting.GRAY));
                for (int i = 0; i < items.size(); i++) {
                    CompoundTag itemTag = items.getCompound(i);
                    ItemStack storedItem = ItemStack.of(itemTag);
                    if (!storedItem.isEmpty()) {
                        String itemName = storedItem.getHoverName().getString();
                        int count = storedItem.getCount();
                        tooltipComponents.add(Component.literal("  • " + itemName + " x" + count)
                                .withStyle(net.minecraft.ChatFormatting.DARK_GRAY));
                    }
                }
            }
        }
    }

    @Override
    public boolean canEquipFromUse(SlotContext slotContext, ItemStack stack) {
        return true;
    }

    /**
     * Gets an item from a specific slot in this pouch's NBT data
     */
    public static ItemStack getItemInSlot(ItemStack pouchStack, int slot) {
        if (pouchStack.isEmpty() || !(pouchStack.getItem() instanceof PouchItem pouchItem)) {
            return ItemStack.EMPTY;
        }
        
        if (slot < 0 || slot >= pouchItem.getSlotsInPouch()) {
            return ItemStack.EMPTY;
        }
        
        CompoundTag tag = pouchStack.getOrCreateTag();
        if (!tag.contains("Items")) {
            return ItemStack.EMPTY;
        }
        
        ListTag items = tag.getList("Items", 10); // 10 = CompoundTag
        for (int i = 0; i < items.size(); i++) {
            CompoundTag itemTag = items.getCompound(i);
            if (itemTag.getInt("Slot") == slot) {
                return ItemStack.of(itemTag);
            }
        }
        
        return ItemStack.EMPTY;
    }

    /**
     * Sets an item in a specific slot in this pouch's NBT data
     */
    public static void setItemInSlot(ItemStack pouchStack, int slot, ItemStack item) {
        if (pouchStack.isEmpty() || !(pouchStack.getItem() instanceof PouchItem pouchItem)) {
            return;
        }
        
        if (slot < 0 || slot >= pouchItem.getSlotsInPouch()) {
            return;
        }
        
        CompoundTag tag = pouchStack.getOrCreateTag();
        ListTag items;
        
        if (!tag.contains("Items")) {
            items = new ListTag();
        } else {
            items = tag.getList("Items", 10);
        }
        
        // Remove existing item in this slot
        for (int i = 0; i < items.size(); i++) {
            CompoundTag itemTag = items.getCompound(i);
            if (itemTag.getInt("Slot") == slot) {
                items.remove(i);
                break;
            }
        }
        
        // Add new item if not empty
        if (!item.isEmpty()) {
            CompoundTag itemTag = new CompoundTag();
            itemTag.putInt("Slot", slot);
            item.save(itemTag);
            items.add(itemTag);
        }
        
        tag.put("Items", items);
    }

    /**
     * Gets all items stored in this pouch
     */
    public static ItemStack[] getAllItems(ItemStack pouchStack) {
        if (pouchStack.isEmpty() || !(pouchStack.getItem() instanceof PouchItem pouchItem)) {
            return new ItemStack[0];
        }
        
        int slotCount = pouchItem.getSlotsInPouch();
        ItemStack[] result = new ItemStack[slotCount];
        
        for (int i = 0; i < slotCount; i++) {
            result[i] = getItemInSlot(pouchStack, i);
        }
        
        return result;
    }
}