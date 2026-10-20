package petrolpark.mc.destroy.core.fluid.gasparticle;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import petrolpark.mc.destroy.client.DestroyParticleTypes;
import com.simibubi.create.foundation.particle.ICustomParticleDataWithSprite;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

public class BoilingFluidBubbleParticleData implements ParticleOptions, ICustomParticleDataWithSprite<BoilingFluidBubbleParticleData> {

    protected ParticleType<BoilingFluidBubbleParticleData> type;
    protected FluidStack fluid; // The fluid stack this of which this Particle is meant to be a gas cloud

    /**
     * Empty constructor to use in {@link petrolpark.mc.destroy.client.DestroyParticleTypes registration}.
     */
    public BoilingFluidBubbleParticleData() {};

    /**
     * Get the Fluid stack which this gas Particle represents.
     */
    public FluidStack getFluid() {
        return fluid;
    };

    /**
     * A Particle with the apperance of a cloud of smoke and the color of a given Fluid.
     * @param type See {@link petrolpark.mc.destroy.client.DestroyParticleTypes here}
     * @param fluid The Fluid of which this Particle should take the appearance
     */
    @SuppressWarnings("unchecked")
    public BoilingFluidBubbleParticleData(ParticleType<?> type, FluidStack fluid) {
        this.type = (ParticleType<BoilingFluidBubbleParticleData>) type;
        this.fluid = fluid;
    };

    public BoilingFluidBubbleParticleData(FluidStack fluid) {
        this(DestroyParticleTypes.BOILING_FLUID_BUBBLE.get(), fluid);
    };

    public static final MapCodec<BoilingFluidBubbleParticleData> CODEC = RecordCodecBuilder.mapCodec(i -> i
            .group(FluidStack.CODEC.fieldOf("fluid").forGetter(p -> p.fluid)).apply(i, fs -> new BoilingFluidBubbleParticleData(DestroyParticleTypes.BOILING_FLUID_BUBBLE.get(), fs))
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, BoilingFluidBubbleParticleData> STREAM_CODEC = FluidStack.STREAM_CODEC
            .map(fs -> new BoilingFluidBubbleParticleData(DestroyParticleTypes.BOILING_FLUID_BUBBLE.get(), fs), p -> p.fluid);

    @Override
    public MapCodec<BoilingFluidBubbleParticleData> getCodec(ParticleType<BoilingFluidBubbleParticleData> type) {
        return CODEC;
    };

    @Override
    public StreamCodec<? super RegistryFriendlyByteBuf, BoilingFluidBubbleParticleData> getStreamCodec() {
        return STREAM_CODEC;
    };

    @Override
    public ParticleType<BoilingFluidBubbleParticleData> getType() {
        return type;
    };

    @Override
    @OnlyIn(Dist.CLIENT)
    public ParticleEngine.SpriteParticleRegistration<BoilingFluidBubbleParticleData> getMetaFactory() {
        return BoilingFluidBubbleParticle.Provider::new;
    };
};
