package io.github.tofithepuppycat.pouches;

import static io.github.tofithepuppycat.pouches.Pouches.MODID;
import io.github.tofithepuppycat.pouches.enchantment.PouchSlotsEnchantment;
import io.github.tofithepuppycat.pouches.item.PouchItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class Registration {

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    public static final DeferredRegister<Enchantment> ENCHANTMENTS = DeferredRegister.create(ForgeRegistries.ENCHANTMENTS, MODID);

    public static final RegistryObject<Item> POUCH_ITEM = ITEMS.register("pouch",
            () -> new PouchItem(
                    new Item.Properties()
                            .stacksTo(1)
            ));

    public static final RegistryObject<Item> IRON_POUCH_ITEM = ITEMS.register("iron_pouch",
            () -> new PouchItem(
                    new Item.Properties()
                            .stacksTo(1),
                    3
            ));

    public static final RegistryObject<Item> DIAMOND_POUCH_ITEM = ITEMS.register("diamond_pouch",
            () -> new PouchItem(
                    new Item.Properties()
                            .stacksTo(1)
                            .rarity(Rarity.RARE),
                    4
            ));

    public static final RegistryObject<Item> NETHERITE_POUCH_ITEM = ITEMS.register("netherite_pouch",
            () -> new PouchItem(
                    new Item.Properties()
                            .stacksTo(1)
                            .rarity(Rarity.EPIC),
                    6
            ));

    public static final RegistryObject<Enchantment> POUCH_SLOTS = ENCHANTMENTS.register("pouch_slots",
            PouchSlotsEnchantment::new);

    public static void init(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
        ENCHANTMENTS.register(modEventBus);
    }

    static void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(POUCH_ITEM);
            event.accept(IRON_POUCH_ITEM);
            event.accept(DIAMOND_POUCH_ITEM);
            event.accept(NETHERITE_POUCH_ITEM);
        }
    }
}
