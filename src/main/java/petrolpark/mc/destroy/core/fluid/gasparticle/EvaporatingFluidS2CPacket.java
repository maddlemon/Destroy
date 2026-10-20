package petrolpark.mc.destroy.core.fluid.gasparticle;

import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.api.distmarker.Dist;
import petrolpark.mc.destroy.DestroyPackets;
import petrolpark.mc.destroy.client.DestroyParticleTypes;

import net.createmod.catnip.math.VecHelper;
import net.createmod.catnip.net.base.ClientboundPacketPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidStack;

/**
 * Summons evaporating Fluid Particles to simulate the emission of a gas.
 */
public class EvaporatingFluidS2CPacket implements ClientboundPacketPayload {

    public static final StreamCodec<RegistryFriendlyByteBuf, EvaporatingFluidS2CPacket> STREAM_CODEC = StreamCodec.ofMember(EvaporatingFluidS2CPacket::toBytes, EvaporatingFluidS2CPacket::new);

    private BlockPos blockPos;
    private FluidStack fluidStack;

    public EvaporatingFluidS2CPacket(BlockPos pos, FluidStack stack) {
        this.blockPos = pos;
        this.fluidStack = stack;
    };

    public EvaporatingFluidS2CPacket(RegistryFriendlyByteBuf buffer) {
        blockPos = buffer.readBlockPos();
        fluidStack = FluidStack.OPTIONAL_STREAM_CODEC.decode(buffer);
    };

    public void toBytes(RegistryFriendlyByteBuf buffer) {
        buffer.writeBlockPos(blockPos);
        FluidStack.OPTIONAL_STREAM_CODEC.encode(buffer, fluidStack);
    };

    @Override
    @SuppressWarnings("resource")
    @OnlyIn(Dist.CLIENT)
    public void handle(LocalPlayer player) {
        ClientLevel level = Minecraft.getInstance().level;
        Vec3 center = VecHelper.getCenterOf(blockPos);
        if (level == null || fluidStack.isEmpty()) return;
        GasParticleData particleData = new GasParticleData(DestroyParticleTypes.EVAPORATION.get(), fluidStack);
        for (int i = 0; i < 5; i++) {
            level.addParticle(particleData, center.x, center.y, center.z, 0, 0.07D, 0);
        };
    };

    @Override
    public PacketTypeProvider getTypeProvider() {
        return DestroyPackets.EVAPORATING_FLUID;
    };
    
};
