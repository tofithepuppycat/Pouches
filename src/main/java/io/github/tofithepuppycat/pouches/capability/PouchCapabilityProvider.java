package io.github.tofithepuppycat.pouches.capability;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PouchCapabilityProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {
    public static final Capability<PouchCapability> POUCH_CAPABILITY =
            CapabilityManager.get(new CapabilityToken<>() {});

    private PouchCapability pouchCapability = null;
    private final LazyOptional<PouchCapability> optional = LazyOptional.of(this::createPouchCapability);

    private PouchCapability createPouchCapability() {
        if (pouchCapability == null) {
            pouchCapability = new PouchCapability();
        }
        return pouchCapability;
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == POUCH_CAPABILITY) {
            return optional.cast();
        }
        return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        createPouchCapability().saveNBTData(tag);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        createPouchCapability().loadNBTData(tag);
    }
}
