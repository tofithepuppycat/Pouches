package io.github.tofithepuppycat.pouches.network;

import io.github.tofithepuppycat.pouches.Pouches;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class NetworkHandler {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(Pouches.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int packetId = 0;

    public static void register() {
        INSTANCE.registerMessage(packetId++, TransferToPouchPacket.class,
                TransferToPouchPacket::encode,
                TransferToPouchPacket::decode,
                TransferToPouchPacket::handle);

        INSTANCE.registerMessage(packetId++, SyncPouchPacket.class,
                SyncPouchPacket::encode,
                SyncPouchPacket::decode,
                SyncPouchPacket::handle);
    }

    private static int id() {
        return packetId++;
    }
}
