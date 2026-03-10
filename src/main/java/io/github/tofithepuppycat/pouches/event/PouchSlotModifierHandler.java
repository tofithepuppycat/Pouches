package io.github.tofithepuppycat.pouches.event;

import io.github.tofithepuppycat.pouches.Pouches;
import io.github.tofithepuppycat.pouches.Registration;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.UUID;

@Mod.EventBusSubscriber(modid = Pouches.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PouchSlotModifierHandler {

    private static final UUID POUCH_SLOTS_UUID_CHEST = UUID.fromString("12345678-1234-1234-1234-123456789abc");
    private static final UUID POUCH_SLOTS_UUID_LEGS = UUID.fromString("12345678-1234-1234-1234-123456789abd");

    @SubscribeEvent
    public static void onEquipmentChange(LivingEquipmentChangeEvent event) {
        LivingEntity entity = event.getEntity();
        EquipmentSlot slot = event.getSlot();
        
        if (slot == EquipmentSlot.CHEST || slot == EquipmentSlot.LEGS) {
            CuriosApi.getCuriosInventory(entity).ifPresent(handler -> {
                // Determine which UUID to use based on equipment slot
                UUID uuid = slot == EquipmentSlot.CHEST ? POUCH_SLOTS_UUID_CHEST : POUCH_SLOTS_UUID_LEGS;
                String name = slot == EquipmentSlot.CHEST ? "Pouch Slots (Chestplate)" : "Pouch Slots (Leggings)";
                
                // Remove existing modifier first
                handler.removeSlotModifier("pouch", uuid);
                
                // Add new modifier if the new item has the enchantment
                ItemStack newItem = event.getTo();
                if (!newItem.isEmpty() && EnchantmentHelper.getItemEnchantmentLevel(Registration.POUCH_SLOTS.get(), newItem) > 0) {
                    handler.addTransientSlotModifier("pouch", uuid, name, 1, AttributeModifier.Operation.ADDITION);
                }
            });
        }
    }
}
