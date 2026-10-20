package petrolpark.mc.destroy.core.fluid.gasparticle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import petrolpark.mc.destroy.client.DestroyParticleTypes;
import com.simibubi.create.foundation.particle.ICustomParticleDataWithSprite;

import net.minecraft.client.particle.ParticleEngine.SpriteParticleRegistration;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.fluids.FluidStack;

public class GasParticleData implements ParticleOptions, ICustomParticleDataWithSprite<GasParticleData> {

    private ParticleType<GasParticleData> type; // What type of particle this is (Distillation, etc.)
	private FluidStack fluid; // The fluid stack this of which this Particle is meant to be a gas cloud
    private float blockHeight; // How many blocks upwards this Particle should float (used if it is in a Distillation Tower)

    /**
     * Empty constructor to use in {@link petrolpark.mc.destroy.client.DestroyParticleTypes registration}.
     */
    public GasParticleData() {};

    /**
     * Get the Fluid stack which this gas Particle represents.
     */
    public FluidStack getFluid() {
        return fluid;
    };

    /**
     * How high this Particle should float before disappearing.
     */
    public float getBlockHeight() {
        return blockHeight;
    };

	public GasParticleData(ParticleType<?> type, FluidStack fluid) {
		this(type, fluid, 0);
	};

    /**
     * A Particle with the apperance of a cloud of smoke and the color of a given Fluid.
     * @param type See {@link petrolpark.mc.destroy.client.DestroyParticleTypes here}
     * @param fluid The Fluid of which this Particle should take the appearance
     * @param blockHeight How many blocks upward this Particle should float before disappearing (used for the {@link petrolpark.mc.destroy.content.processing.distillation.BubbleCapBlockEntity#spawnParticles Distillation Tower})
     */
    @SuppressWarnings("unchecked")
    public GasParticleData(ParticleType<?> type, FluidStack fluid, float blockHeight) {
        this.type = (ParticleType<GasParticleData>) type;
        this.fluid = fluid;
        this.blockHeight = blockHeight;
    };

    public static final MapCodec<GasParticleData> DISTILLATION_CODEC = RecordCodecBuilder.mapCodec(i -> i
		.group(
            FluidStack.CODEC.fieldOf("fluid").forGetter(p -> p.fluid),
            Codec.FLOAT.fieldOf("blockHeight").forGetter(p -> p.blockHeight)
        ).apply(i, (fluidStack, blockHeight) -> new GasParticleData(DestroyParticleTypes.DISTILLATION.get(), fluidStack, blockHeight))
    );

    public static final MapCodec<GasParticleData> EVAPORATION_CODEC = RecordCodecBuilder.mapCodec(i -> i
        .group(
            FluidStack.CODEC.fieldOf("fluid").forGetter(p -> p.fluid)
        ).apply(i, (fluidStack) -> new GasParticleData(DestroyParticleTypes.EVAPORATION.get(), fluidStack, 0))
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, GasParticleData> DISTILLATION_STREAM_CODEC = StreamCodec.composite(
        FluidStack.STREAM_CODEC, p -> p.fluid,
        ByteBufCodecs.FLOAT, p -> p.blockHeight,
        (fluidStack, blockHeight) -> new GasParticleData(DestroyParticleTypes.DISTILLATION.get(), fluidStack, blockHeight)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, GasParticleData> EVAPORATION_STREAM_CODEC = FluidStack.STREAM_CODEC
        .map(fluidStack -> new GasParticleData(DestroyParticleTypes.EVAPORATION.get(), fluidStack), p -> p.fluid);

    @Override
    public MapCodec<GasParticleData> getCodec(ParticleType<GasParticleData> type) {
        if (type == DestroyParticleTypes.DISTILLATION.get()) return DISTILLATION_CODEC;
        return EVAPORATION_CODEC;
    };

    @Override
    public StreamCodec<? super RegistryFriendlyByteBuf, GasParticleData> getStreamCodec() {
        if (type == DestroyParticleTypes.DISTILLATION.get()) return DISTILLATION_STREAM_CODEC;
        return EVAPORATION_STREAM_CODEC;
    };

    @Override
    public ParticleType<GasParticleData> getType() {
        return type;
    };

    @Override
    public SpriteParticleRegistration<GasParticleData> getMetaFactory() {
        return GasParticle.Provider::new;
    };

    /**
     * Overridden so the ParticleType's stream codec dispatches on the ParticleType itself, not on this
     * registration instance's {@code type} field (which is unset on the instance used at registration).
     * The two gas particle types have different network shapes, and decoded data must carry the correct
     * type as the {@link net.minecraft.client.particle.ParticleEngine ParticleEngine} looks up providers
     * by {@code data.getType()}.
     */
    @Override
    public ParticleType<GasParticleData> createType() {
        return new ParticleType<GasParticleData>(false) {
            @Override
            public MapCodec<GasParticleData> codec() {
                return getCodec(this);
            };

            @Override
            public StreamCodec<? super RegistryFriendlyByteBuf, GasParticleData> streamCodec() {
                if (this == DestroyParticleTypes.DISTILLATION.get()) return DISTILLATION_STREAM_CODEC;
                return EVAPORATION_STREAM_CODEC;
            };
        };
    };
};
