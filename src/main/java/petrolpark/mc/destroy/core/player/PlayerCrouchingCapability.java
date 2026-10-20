package petrolpark.mc.destroy.core.player;

import petrolpark.mc.destroy.DestroyAttachmentTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.core.HolderLookup;
import java.util.Optional;
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
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = Destroy.MOD_ID)
public class PlayerCrouchingCapability implements INBTSerializable<CompoundTag> {

    /**
     * Players only (as the old capability was).
     */
    public static Optional<PlayerCrouchingCapability> of(Entity entity) {
        return entity instanceof Player ? Optional.of(entity.getData(DestroyAttachmentTypes.PLAYER_CROUCHING)) : Optional.empty();
    };

    public int ticksCrouching; // How long the Player has been crouching
    public int ticksUrinating; // How long the Player has been urinating

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("Crouching", ticksCrouching);
        tag.putInt("Urinating", ticksUrinating);
        return tag;
    };

    @Override
    public void deserializeNBT(HolderLookup.Provider registries, CompoundTag tag) {
        ticksCrouching = tag.getInt("Crouching");
        ticksUrinating = tag.getInt("Urinating");
    };


    @SubscribeEvent
    public static final void onPlayerTickPre(PlayerTickEvent.Pre event) {
        onPlayerTick(event.getEntity());
    };

    // Forge fired this tick event at both START and END and the handler ignored the phase, so it ran twice per tick. Kept that way to match 1.20.1.
    @SubscribeEvent
    public static final void onPlayerTickPost(PlayerTickEvent.Post event) {
        onPlayerTick(event.getEntity());
    };

    public static final void onPlayerTick(Player player) {

        BlockPos posOn = player.getOnPos();
        BlockState stateOn = player.level().getBlockState(posOn);
        boolean urinating = (stateOn.getBlock() == Blocks.WATER_CAULDRON || stateOn.getBlock() == Blocks.CAULDRON) && player.hasEffect(DestroyMobEffects.FULL_BLADDER);
        if (player.isCrouching()) {
            PlayerCrouchingCapability.of(player).ifPresent(crouchingCap -> {
                crouchingCap.ticksCrouching++;
                if (urinating) {crouchingCap.ticksUrinating++;} else crouchingCap.ticksUrinating = 0;
            });
        } else {
            PlayerCrouchingCapability.of(player).ifPresent(crouchingCap -> {
                crouchingCap.ticksCrouching = 0;
                crouchingCap.ticksUrinating = 0;
            });
        };

        int ticksUrinating = PlayerCrouchingCapability.of(player).map(crouchingCap -> crouchingCap.ticksUrinating).orElse(0);
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
