package petrolpark.mc.destroy;

import net.minecraft.world.item.component.ItemContainerContents;
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

    /**
     * On the example Fluid Stacks of a {@link petrolpark.mc.destroy.core.recipe.ingredient.fluid.MixtureFluidIngredient}: which
     * ingredient Sub-Type it is and its parameters, for its description in recipe viewers (was extra Fluid Stack NBT in 1.20.1).
     */
    public static final DataComponentType<CompoundTag> MIXTURE_INGREDIENT_DISPLAY = register("mixture_ingredient_display", b -> b
        .persistent(CompoundTag.CODEC)
        .networkSynchronized(ByteBufCodecs.COMPOUND_TAG)
    );

    /**
     * The music disc a Disc Stamper stamps (was the {@code "Disc"} NBT tag in 1.20.1). Held as one-slot container contents, as those
     * (unlike Item Stacks) compare by value, so Disc Stampers with the same disc still stack and compare equal.
     */
    public static final DataComponentType<ItemContainerContents> DISC = register("disc", b -> b
        .persistent(ItemContainerContents.CODEC)
        .networkSynchronized(ItemContainerContents.STREAM_CODEC)
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
