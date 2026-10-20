package petrolpark.mc.destroy;

import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.nbt.CompoundTag;
import java.util.function.UnaryOperator;

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import petrolpark.mc.destroy.core.seismology.ISeismologyProvider;
import petrolpark.mc.destroy.core.seismology.Seismograph;

public class DestroyDataComponentTypes {
    
    private static final DeferredRegister.DataComponents DATA_COMPONENTS = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, Destroy.MOD_ID);

    public static final DataComponentType<Holder<ISeismologyProvider>> SEISMOLOGY_PROVIDER = register("seismology_provider", b -> b
        .persistent(ISeismologyProvider.CODEC)
        .networkSynchronized(ISeismologyProvider.STREAM_CODEC)
    );
    public static final DataComponentType<Seismograph> SEISMOGRAPH = register("seismograph", b -> b
        .persistent(Seismograph.CODEC)
        .networkSynchronized(Seismograph.STREAM_CODEC)
    );

    /**
     * The serialized {@link petrolpark.mc.destroy.chemistry.legacy.LegacyMixture Mixture} of a Mixture Fluid Stack (was the
     * {@code "Mixture"} child NBT tag in 1.20.1). Treat the stored tag as immutable: always {@code set} a new one.
     * Access through {@link petrolpark.mc.destroy.chemistry.minecraft.MixtureFluid#getMixtureTag} and friends.
     */
    public static final DataComponentType<CompoundTag> MIXTURE = register("mixture", b -> b
        .persistent(CompoundTag.CODEC)
        .networkSynchronized(ByteBufCodecs.COMPOUND_TAG)
    );

    private static <T> DataComponentType<T> register(String name, UnaryOperator<DataComponentType.Builder<T>> builder) {
		DataComponentType<T> type = builder.apply(DataComponentType.builder()).build();
		DATA_COMPONENTS.register(name, () -> type);
		return type;
	};

	@ApiStatus.Internal
	public static final void register(IEventBus modEventBus) {
		DATA_COMPONENTS.register(modEventBus);
	};
};
