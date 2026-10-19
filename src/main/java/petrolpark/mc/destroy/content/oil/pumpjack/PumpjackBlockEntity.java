package petrolpark.mc.destroy.content.oil.pumpjack;

import java.lang.ref.WeakReference;
import java.util.List;

import javax.annotation.Nullable;

import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import org.jetbrains.annotations.NotNull;

import petrolpark.mc.library.compat.create.core.world.block.entity.behaviour.AbstractRememberPlacerBehaviour;
import petrolpark.mc.destroy.DestroyAdvancementTrigger;
import petrolpark.mc.destroy.DestroyBlocks;
import petrolpark.mc.destroy.DestroyFluids;
import petrolpark.mc.destroy.DestroySoundEvents;
import petrolpark.mc.destroy.config.DestroyAllConfigs;
import petrolpark.mc.destroy.content.oil.ChunkCrudeOil;
import petrolpark.mc.destroy.core.data.advancement.DestroyAdvancementBehaviour;
import petrolpark.mc.destroy.core.pollution.PollutingBehaviour;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BehaviourType;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.capabilities.Capability;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.util.LazyOptional;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.fml.DistExecutor;

public class PumpjackBlockEntity extends SmartBlockEntity implements IHaveGoggleInformation {

    public SmartFluidTankBehaviour tank;
    protected PollutingBehaviour pollutionBehaviour;
    protected RememberPlacerForOilDiscoveryBehaviour prospectingBehaviour;
    protected DestroyAdvancementBehaviour advancementBehaviour;

    public WeakReference<PumpjackCamBlockEntity> source;

    private boolean upsqueak; // Client-only

    public PumpjackBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        source = new WeakReference<>(null);
        upsqueak = false;
    };

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        tank = new SmartFluidTankBehaviour(SmartFluidTankBehaviour.OUTPUT, this, 1, getTankCapacity(), false)
            .forbidInsertion();
        behaviours.add(tank);

        advancementBehaviour = new DestroyAdvancementBehaviour(this, DestroyAdvancementTrigger.USE_PUMPJACK);
        behaviours.add(advancementBehaviour);

        pollutionBehaviour = new PollutingBehaviour(this);
        behaviours.add(pollutionBehaviour);

        prospectingBehaviour = new RememberPlacerForOilDiscoveryBehaviour(this);
        behaviours.add(prospectingBehaviour);
    };

    @Override
    @SuppressWarnings("null")
    public void tick() {
        super.tick();
        PumpjackCamBlockEntity cam = getCam();

        // Updating cam
        if (!hasLevel()) return; // Don't do anything if we're not in a level
        if (cam == null) return; // Don't do anything if our cam is missing
        if (!cam.getBlockPos() // Don't do anything if our cam is pointing to the wrong place
            .subtract(getBlockPos())
            .equals(cam.pumpjackPos))
        {
            cam.update(getBlockPos());
            sendData();
            return;
        };
        Direction facing = PumpjackBlock.getFacing(getBlockState());
        if (getLevel().isLoaded(getBlockPos().relative(facing.getOpposite()))) cam.update(getBlockPos()); // It thinks getLevel() might be null (it's not)

        // Sounds
        if (getLevel().isClientSide()) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> playClientSound());
            return;
        };

        // Pumping oil
        if (cam.getSpeed() == 0) return; // Don't go any further if we're not pumping
        BlockPos pipePos = getBlockPos().relative(facing);
        LevelChunk chunk = level.getChunkAt(pipePos);
        LazyOptional<ChunkCrudeOil> crudeOilOptional = chunk.getCapability(ChunkCrudeOil.Provider.CHUNK_CRUDE_OIL);
        if (!crudeOilOptional.isPresent()) return; // Don't go any further if there's somehow no capability
        int oilAmount = crudeOilOptional.map(crudeOilCap -> {
            crudeOilCap.generate(chunk, prospectingBehaviour.getPlayer());
            return crudeOilCap.getAmount();
        }).orElse(0);
        if (oilAmount == 0) return; // Don't go any further if there's no oil
        advancementBehaviour.awardDestroyAdvancement(DestroyAdvancementTrigger.USE_PUMPJACK);
        // Add the oil to the Pumpjack's internal tank
        tank.allowInsertion();
        int amountPumped = tank.getPrimaryHandler().fill(new FluidStack(DestroyFluids.CRUDE_OIL.get(), (int)Math.min(oilAmount, DestroyAllConfigs.SERVER.blocks.pumpjackExtractionSpeed.getF() * Math.abs(cam.getSpeed() / 16f))), FluidAction.EXECUTE);
        tank.forbidInsertion();
        crudeOilOptional.ifPresent(crudeOilCap -> crudeOilCap.decreaseAmount(amountPumped));
    };

    @Override
    public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == Capabilities.FluidHandler.BLOCK && (side == null || (PumpjackBlock.getFacing(getBlockState()).getAxis() != side.getAxis() && side.getAxis() != Axis.Y))) {
            return tank.getCapability().cast();
        };
        return super.getCapability(cap, side);
    };

    public Float getTargetAngle() {
        float angle = 0;
		BlockState blockState = getBlockState();
		if (!DestroyBlocks.PUMPJACK.has(blockState)) return null;

		PumpjackCamBlockEntity cam = getCam();

		if (cam == null) return null;

        Direction facing = PumpjackBlock.getFacing(blockState);
		Axis axis = KineticBlockEntityRenderer.getRotationAxisOf(cam);
		angle = KineticBlockEntityRenderer.getAngleForBe(cam, cam.getBlockPos(), axis);

        if (axis.isHorizontal() && (facing.getAxis() == Axis.X ^ facing.getAxisDirection() == AxisDirection.NEGATIVE))
			angle *= -1;

		return angle;
    };

    public float getRenderAngle() {
        Float angle = getTargetAngle();
        if(angle == null)
        {
            BlockState blockState = getBlockState();
            Direction facing = PumpjackBlock.getFacing(blockState);
            Direction.Axis axis = facing.getCounterClockWise().getAxis();
            BlockPos pos = getCamPos();
            double d = (((axis == Direction.Axis.X) ? 0 : pos.getX()) + ((axis == Direction.Axis.Y) ? 0 : pos.getY())
                + ((axis == Direction.Axis.Z) ? 0 : pos.getZ())) % 2;
            angle = d == 0 ? 22.5f * Mth.PI / 180.f : 0f;

            if (axis.isHorizontal() && (facing.getAxis() == Axis.X ^ facing.getAxisDirection() == AxisDirection.NEGATIVE))
                angle *= -1;
        }
        return angle;
    };

    @OnlyIn(Dist.CLIENT)
    public void playClientSound() {
        if (getTargetAngle() == null) return;
        Minecraft minecraft = Minecraft.getInstance();
        // Sound is syncronised to Pumpjack movement
        if (Math.abs(getTargetAngle()) <= 0.1f && upsqueak) {
            DestroySoundEvents.PUMPJACK_CREAK_1.play(getLevel(), minecraft.player, getBlockPos());
            upsqueak = false;
        };
        if (Math.abs(Math.abs(getTargetAngle()) - Mth.PI) < 0.1 && !upsqueak) {
            DestroySoundEvents.PUMPJACK_CREAK_2.play(getLevel(), minecraft.player, getBlockPos());
            upsqueak = true;
        };
    };

    public BlockPos getCamPos() {
        Direction facing = PumpjackBlock.getFacing(getBlockState());
        return getBlockPos().relative(facing, 1);
    }

    @Nullable
    @SuppressWarnings("null")
    public PumpjackCamBlockEntity getCam() {
        PumpjackCamBlockEntity cam = source.get();
        if (cam == null || cam.isRemoved() || !cam.canPower(getBlockPos())) {
            if (cam != null) source = new WeakReference<>(null);
            BlockEntity anyCamAt = getLevel().getBlockEntity(getCamPos()); // It thinks getLevel() might be null
            if (anyCamAt instanceof PumpjackCamBlockEntity newCam && newCam.canPower(getBlockPos())) {
                cam = newCam;
				source = new WeakReference<>(cam);
            };
        };
        return cam;
    };

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        return containedFluidTooltip(tooltip, isPlayerSneaking, getCapability(Capabilities.FluidHandler.BLOCK));
    };

    public class RememberPlacerForOilDiscoveryBehaviour extends AbstractRememberPlacerBehaviour {

        public RememberPlacerForOilDiscoveryBehaviour(SmartBlockEntity be) {
            super(be);
        };

        public static BehaviourType<?> TYPE = new BehaviourType<>();

        @Override
        public boolean shouldRememberPlacer(Player placer) {
            LevelChunk chunk = getLevel().getChunkAt(getBlockPos());
            if (chunk == null) return true;
            return chunk.getCapability(ChunkCrudeOil.Provider.CHUNK_CRUDE_OIL).map(ChunkCrudeOil::isGenerated).orElse(true);
        };

        @Override
        public BehaviourType<?> getType() {
            return TYPE;
        };

    };

    public int getTankCapacity() {
        return DestroyAllConfigs.SERVER.blocks.pumpjackCapacity.get();
    };
    
};
