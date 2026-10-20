package petrolpark.mc.destroy.core.pollution.pollutometer;

import petrolpark.mc.destroy.DestroyPollutionTypes;
import net.minecraft.core.HolderLookup;
import java.util.List;
import java.util.function.Supplier;

import petrolpark.mc.destroy.client.DestroyIcons;
import petrolpark.mc.destroy.util.DestroyLang;
import petrolpark.mc.destroy.core.pollution.PollutometerBlock;
import petrolpark.mc.destroy.core.pollution.PollutionType;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.INamedIconOptions;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollOptionBehaviour;

import net.createmod.catnip.lang.Lang;
import net.createmod.catnip.math.AngleHelper;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class PollutometerBlockEntity extends SmartBlockEntity {

    /**
     * The options for the scroll value: 1.20.1's {@code PollutionType} enum (same order, icons and names, and the ordinal is what is saved).
     */
    public static enum DisplayedPollutionType implements INamedIconOptions {

        GREENHOUSE(DestroyIcons.GREENHOUSE, DestroyPollutionTypes.GREENHOUSE),
        OZONE_DEPLETION(DestroyIcons.OZONE_DEPLETION, DestroyPollutionTypes.OZONE_DEPLETION),
        SMOG(DestroyIcons.SMOG, DestroyPollutionTypes.SMOG),
        ACID_RAIN(DestroyIcons.ACID_RAIN, DestroyPollutionTypes.ACID_RAIN),
        RADIOACTIVITY(DestroyIcons.RADIOACTIVITY, DestroyPollutionTypes.RADIOACTIVITY);

        private final AllIcons icon;
        public final Supplier<? extends PollutionType<?>> pollutionType;

        DisplayedPollutionType(AllIcons icon, Supplier<? extends PollutionType<?>> pollutionType) {
            this.icon = icon;
            this.pollutionType = pollutionType;
        };

        @Override
        public AllIcons getIcon() {
            return icon;
        };

        @Override
        public String getTranslationKey() {
            return "destroy.pollution." + Lang.asId(name());
        };
    };

    private DisplayedPollutionType pollutionType;

    protected ScrollOptionBehaviour<DisplayedPollutionType> pollutionTypeDisplay;

    public PollutometerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        pollutionType = DisplayedPollutionType.GREENHOUSE;
    };

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        pollutionTypeDisplay = new ScrollOptionBehaviour<DisplayedPollutionType>(DisplayedPollutionType.class, DestroyLang.translate("tooltip.pollutometer.pollution_type").component(), this, slot);
        pollutionTypeDisplay.withCallback(this::setPollutionType);
        behaviours.add(pollutionTypeDisplay);
    };

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        int pollutionTypeIndex = tag.getInt("PollutionType");
        setPollutionType(pollutionTypeIndex);
        pollutionTypeDisplay.value = pollutionTypeIndex;
        super.read(tag, registries, clientPacket);
    };

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        tag.putInt("PollutionType", pollutionType.ordinal());
        super.write(tag, registries, clientPacket);
    };

    public PollutionType<?> getPollutionType() {
        return pollutionType.pollutionType.get();
    };

    private void setPollutionType(Integer pollutionTypeIndex) {
        this.pollutionType = DisplayedPollutionType.values()[pollutionTypeIndex];
    };

    private static final ValueBoxTransform.Sided slot = new ValueBoxTransform.Sided() {

        @Override
        public Vec3 getLocalOffset(LevelAccessor level, BlockPos pos, BlockState state) {
            return VecHelper.rotateCentered(getSouthLocation(), AngleHelper.horizontalAngle(getSide()), Axis.Y);
        };

        @Override
        protected boolean isSideActive(BlockState state, Direction direction) {
            return direction == state.getValue(PollutometerBlock.DIRECTION);
        };

        @Override
        protected Vec3 getSouthLocation() {
            return VecHelper.voxelSpace(8, 6, 12.75f);
        };
    };
    
};
