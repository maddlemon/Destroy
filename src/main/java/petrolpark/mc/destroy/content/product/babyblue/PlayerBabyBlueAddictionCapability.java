package petrolpark.mc.destroy.content.product.babyblue;

import net.minecraft.world.entity.player.Player;
import petrolpark.mc.destroy.DestroyAttachmentTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.core.HolderLookup;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import petrolpark.mc.destroy.config.DestroyAllConfigs;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.common.util.INBTSerializable;

public class PlayerBabyBlueAddictionCapability implements INBTSerializable<CompoundTag> {

    /**
     * Players only (as the old capability was).
     */
    public static Optional<PlayerBabyBlueAddictionCapability> of(Entity entity) {
        return entity instanceof Player ? Optional.of(entity.getData(DestroyAttachmentTypes.PLAYER_BABY_BLUE_ADDICTION)) : Optional.empty();
    };

    private int babyBlueAddiction;

    public void copyFrom(PlayerBabyBlueAddictionCapability source) {
        this.babyBlueAddiction = source.babyBlueAddiction;
    };

    public int getBabyBlueAddiction() {
        return this.babyBlueAddiction;
    };

    public void setBabyBlueAddiction(int babyBlueAddiction) {
        this.babyBlueAddiction = Mth.clamp(babyBlueAddiction, 0, getMaxBabyBlueAddiction());
    };

    public static final int getMaxBabyBlueAddiction() {
        return DestroyAllConfigs.SERVER.substances.babyBlueMaxAddictionLevel.get();
    };

    public void addBabyBlueAddiction(int change) {
        this.babyBlueAddiction = Mth.clamp(this.babyBlueAddiction + change, 0, getMaxBabyBlueAddiction());
    };

    public void saveNBTData(CompoundTag nbt) {
        nbt.putInt("BabyBlueAddiction", this.babyBlueAddiction);
    };

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider registries) {
        CompoundTag nbt = new CompoundTag();
        saveNBTData(nbt);
        return nbt;
    };

    @Override
    public void deserializeNBT(HolderLookup.Provider registries, CompoundTag nbt) {
        loadNBTData(nbt);
    };

    public void loadNBTData(CompoundTag nbt) {
        this.babyBlueAddiction = nbt.getInt("BabyBlueAddiction");
    };
}
