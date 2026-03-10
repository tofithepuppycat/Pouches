package io.github.tofithepuppycat.pouches;

import io.github.tofithepuppycat.pouches.network.NetworkHandler;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

@Mod(Pouches.MODID)
public class Pouches
{
    public static final String MODID = "pouches";

    public Pouches(FMLJavaModLoadingContext context)
    {
        IEventBus modEventBus = context.getModEventBus();
        Registration.init(modEventBus);

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(Registration::addCreative);
    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {
        event.enqueueWork(NetworkHandler::register);
        event.enqueueWork(() -> {
            CuriosApi.registerCurio(Registration.POUCH_ITEM.get(), (ICurioItem) Registration.POUCH_ITEM.get());
            CuriosApi.registerCurio(Registration.IRON_POUCH_ITEM.get(), (ICurioItem) Registration.IRON_POUCH_ITEM.get());
            CuriosApi.registerCurio(Registration.DIAMOND_POUCH_ITEM.get(), (ICurioItem) Registration.DIAMOND_POUCH_ITEM.get());
            CuriosApi.registerCurio(Registration.NETHERITE_POUCH_ITEM.get(), (ICurioItem) Registration.NETHERITE_POUCH_ITEM.get());

            CauldronInteraction.WATER.put(Registration.POUCH_ITEM.get(), WASH_POUCH);
            CauldronInteraction.WATER.put(Registration.IRON_POUCH_ITEM.get(), WASH_POUCH);
            CauldronInteraction.WATER.put(Registration.DIAMOND_POUCH_ITEM.get(), WASH_POUCH);
            CauldronInteraction.WATER.put(Registration.NETHERITE_POUCH_ITEM.get(), WASH_POUCH);
        });
    }

    private static final CauldronInteraction WASH_POUCH = (state, level, pos, player, hand, stack) -> {
        if (!(stack.getItem() instanceof DyeableLeatherItem dyeable) || !dyeable.hasCustomColor(stack)) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide) {
            dyeable.clearColor(stack);
            LayeredCauldronBlock.lowerFillLevel(state, level, pos);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    };
}
