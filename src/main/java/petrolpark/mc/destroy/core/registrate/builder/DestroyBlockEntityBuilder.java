package petrolpark.mc.destroy.core.registrate.builder;

import java.util.function.Predicate;

import org.jetbrains.annotations.Nullable;

import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.builders.BlockEntityBuilder.BlockEntityFactory;
import com.tterrag.registrate.builders.BuilderCallback;
import com.tterrag.registrate.util.OneTimeEventReceiver;
import com.tterrag.registrate.util.nullness.NonNullSupplier;

import dev.engine_room.flywheel.lib.visualization.SimpleBlockEntityVisualizer;
import dev.engine_room.flywheel.lib.visualization.SimpleBlockEntityVisualizer.Factory;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import petrolpark.mc.library.core.registrate.builder.PetrolparkBlockEntityBuilder;

/**
 * Create 6's block-entity visual registration (Flywheel), on top of the library's builder.
 */
public class DestroyBlockEntityBuilder<T extends BlockEntity, P> extends PetrolparkBlockEntityBuilder<T, P> {

    @Nullable
    private NonNullSupplier<Factory<T>> visualFactory;
    private Predicate<T> renderNormally = be -> true;

    public DestroyBlockEntityBuilder(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback, BlockEntityFactory<T> factory) {
        super(owner, parent, name, callback, factory);
    };

    public DestroyBlockEntityBuilder<T, P> visual(NonNullSupplier<Factory<T>> visualFactory) {
        if (this.visualFactory == null) {
            CatnipServices.PLATFORM.executeOnClientOnly(() -> this::registerVisualizer);
        };
        this.visualFactory = visualFactory;
        return this;
    };

    protected void registerVisualizer() {
        OneTimeEventReceiver.addModListener(
            this.getOwner(),
            FMLClientSetupEvent.class,
            $ -> {
                NonNullSupplier<Factory<T>> visualFactory = this.visualFactory;
                if (visualFactory != null) {
                    Predicate<T> renderNormally = this.renderNormally;
                    SimpleBlockEntityVisualizer.builder(this.getEntry())
                        .factory(visualFactory.get())
                        .skipVanillaRender(be -> !renderNormally.test(be))
                        .apply();
                };
            }
        );
    };
};
