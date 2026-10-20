package petrolpark.mc.destroy.core.player;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import petrolpark.mc.destroy.Destroy;
import petrolpark.mc.destroy.DestroyAdvancementTrigger;
import petrolpark.mc.destroy.DestroyBlocks;
import petrolpark.mc.destroy.DestroyFluids;
import petrolpark.mc.destroy.DestroyMobEffects;
import petrolpark.mc.destroy.DestroySoundEvents;
import com.simibubi.create.content.fluids.FluidFX;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capability;
import net.neoforged.neoforge.capabilities.CapabilityManager;
import net.neoforged.neoforge.capabilities.CapabilityToken;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.common.util.LazyOptional;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = Destroy.MOD_ID)
public class PlayerCrouchingCapability {

    public static final Capability<PlayerCrouchingCapability> CAPABILITY = CapabilityManager.get(new CapabilityToken<PlayerCrouchingCapability>() {});

    public int ticksCrouching; // How long the Player has been crouching
    public int ticksUrinating; // How long the Player has been urinating

    public static class Provider implements ICapabilityProvider, INBTSerializable<CompoundTag> {

        private PlayerCrouchingCapability playerCrouching = null;
        private final LazyOptional<PlayerCrouchingCapability> optional = LazyOptional.of(this::createPlayerCrouching);

        private PlayerCrouchingCapability createPlayerCrouching() {
            if (playerCrouching == null) {
                playerCrouching = new PlayerCrouchingCapability();
            };
            return playerCrouching;
        };

        @Override
        public CompoundTag serializeNBT() {
            CompoundTag tag = new CompoundTag();
            PlayerCrouchingCapability playerCrouching = createPlayerCrouching();
            tag.putInt("Crouching", playerCrouching.ticksCrouching);
            tag.putInt("Urinating", playerCrouching.ticksUrinating);
            return tag;
        };

        @Override
        public void deserializeNBT(CompoundTag tag) {
            PlayerCrouchingCapability playerCrouching = createPlayerCrouching();
            playerCrouching.ticksCrouching = tag.getInt("Crouching");
            playerCrouching.ticksUrinating = tag.getInt("Urinating");
        };

        @Override
        public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
            if(cap == CAPABILITY) {
                return optional.cast();
            };
            return LazyOptional.empty();
        };
    };

    @SubscribeEvent
    public static final void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();

        BlockPos posOn = player.getOnPos();
        BlockState stateOn = player.level().getBlockState(posOn);
        boolean urinating = (stateOn.getBlock() == Blocks.WATER_CAULDRON || stateOn.getBlock() == Blocks.CAULDRON) && player.hasEffect(DestroyMobEffects.FULL_BLADDER);
        if (player.isCrouching()) {
            player.getCapability(PlayerCrouchingCapability.CAPABILITY).ifPresent(crouchingCap -> {
                crouchingCap.ticksCrouching++;
                if (urinating) {crouchingCap.ticksUrinating++;} else crouchingCap.ticksUrinating = 0;
            });
        } else {
            player.getCapability(PlayerCrouchingCapability.CAPABILITY).ifPresent(crouchingCap -> {
                crouchingCap.ticksCrouching = 0;
                crouchingCap.ticksUrinating = 0;
            });
        };

        int ticksUrinating = player.getCapability(PlayerCrouchingCapability.CAPABILITY).map(crouchingCap -> crouchingCap.ticksUrinating).orElse(0);
        if (ticksUrinating > 0) {
            Vec3 pos = player.position();
            if (player.level().isClientSide()) player.level().addParticle(FluidFX.getFluidParticle(new FluidStack(DestroyFluids.URINE.get(), 1000)), pos.x, pos.y + 0.5f, pos.z, 0d, -0.07d, 0d);
            if (ticksUrinating % 40 == 0) DestroySoundEvents.URINATE.playOnServer(player.level(), posOn);
            if (ticksUrinating == 119) {
                DestroyMobEffects.increaseEffectLevel(player, DestroyMobEffects.FULL_BLADDER, -1, 0);
                DestroyAdvancementTrigger.URINATE.award(player.level(), player);
                player.level().setBlockAndUpdate(posOn, DestroyBlocks.URINE_CAULDRON.getDefaultState());
            };
        };
    };
};
