package io.github.tofithepuppycat.pouches;

import static io.github.tofithepuppycat.pouches.Pouches.MODID;
import io.github.tofithepuppycat.pouches.item.PouchItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class Registration {

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    //public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

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

    public static void init(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
    }

    static void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(POUCH_ITEM);
            event.accept(IRON_POUCH_ITEM);
            event.accept(DIAMOND_POUCH_ITEM);
        }
    }
}
