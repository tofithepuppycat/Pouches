package io.github.tofithepuppycat.pouches;

import io.github.tofithepuppycat.pouches.network.NetworkHandler;
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
        });
    }

    /*
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event)
    {
        // Do something when the server starts
        LOGGER.info("HELLO from server starting");
    }
     */
}
