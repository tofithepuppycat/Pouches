package io.github.tofithepuppycat.pouches.client;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.tofithepuppycat.pouches.Pouches;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

public final class ClientEvents {

    @Mod.EventBusSubscriber(modid = Pouches.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static final class ModBus {
        public static final KeyMapping SHOW_IMAGE_KEY = new KeyMapping(
                "key." + Pouches.MODID + ".show_image",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_C,
                "key.categories." + Pouches.MODID
        );

        @SubscribeEvent
        public static void onRegisterKeys(RegisterKeyMappingsEvent event) {
            event.register(SHOW_IMAGE_KEY);
        }

        @SubscribeEvent
        public static void onRegisterOverlays(RegisterGuiOverlaysEvent event) {
            event.registerAboveAll("wheel", SelectionWheelHudOverlay.HUD_IMAGE);
        }
    }

    private ClientEvents() {
    }
}