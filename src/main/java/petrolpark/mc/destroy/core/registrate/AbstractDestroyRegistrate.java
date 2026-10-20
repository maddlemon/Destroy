package petrolpark.mc.destroy.core.registrate;

import java.util.function.Supplier;

import com.mojang.datafixers.util.Function3;
import com.simibubi.create.api.behaviour.display.DisplaySource;
import com.simibubi.create.api.registry.CreateRegistries;
import com.simibubi.create.api.registry.registrate.SimpleBuilder;
import com.simibubi.create.content.fluids.VirtualFluid;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.data.VirtualFluidBuilder;
import com.simibubi.create.foundation.gui.AllIcons;
import com.tterrag.registrate.builders.BlockEntityBuilder.BlockEntityFactory;
import com.tterrag.registrate.builders.FluidBuilder;
import com.tterrag.registrate.builders.FluidBuilder.FluidTypeFactory;
import com.tterrag.registrate.util.entry.RegistryEntry;
import com.tterrag.registrate.util.nullness.NonNullFunction;

import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import petrolpark.mc.destroy.DestroyRegistries;
import petrolpark.mc.destroy.core.pollution.PollutionType;
import petrolpark.mc.destroy.core.registrate.builder.DestroyBlockEntityBuilder;
import petrolpark.mc.library.core.registrate.AbstractPetrolparkRegistrate;

public abstract class AbstractDestroyRegistrate<R extends AbstractPetrolparkRegistrate<R>> extends AbstractPetrolparkRegistrate<R> {

    protected AbstractDestroyRegistrate(String modid) {
        super(modid);
    };

    public <POLLUTION_TYPE extends PollutionType<Level>> RegistryEntry<PollutionType<Level>, POLLUTION_TYPE> levelPollutionType(String name, AllIcons icon, Function3<Boolean, AllIcons, String, POLLUTION_TYPE> factory) {
        return simple(name, DestroyRegistries.Keys.LEVEL_POLLUTION_TYPE, () -> factory.apply(false, icon, Util.makeDescriptionId("pollution_type", ResourceLocation.fromNamespaceAndPath(getModid(), name))));
    };

    public <POLLUTION_TYPE extends PollutionType<ChunkAccess>> RegistryEntry<PollutionType<ChunkAccess>, POLLUTION_TYPE> chunkPollutionType(String name, AllIcons icon, Function3<Boolean, AllIcons, String, POLLUTION_TYPE> factory) {
        return simple(name, DestroyRegistries.Keys.CHUNK_POLLUTION_TYPE, () -> factory.apply(true, icon, Util.makeDescriptionId("pollution_type", ResourceLocation.fromNamespaceAndPath(getModid(), name))));
    };

    // Create 6's fluid/display-source registrate helpers, on Destroy's own registrate (the library dropped them)

    public <T extends BaseFlowingFluid> FluidBuilder<T, R> virtualFluid(String name, ResourceLocation still, ResourceLocation flow, FluidTypeFactory typeFactory, NonNullFunction<BaseFlowingFluid.Properties, T> sourceFactory, NonNullFunction<BaseFlowingFluid.Properties, T> flowingFactory) {
        return (FluidBuilder<T, R>) this.entry(name, c -> new VirtualFluidBuilder<>(this.self(), this.self(), name, c, still, flow, typeFactory, sourceFactory, flowingFactory));
    };

    public FluidBuilder<VirtualFluid, R> virtualFluid(String name, ResourceLocation still, ResourceLocation flow) {
        return (FluidBuilder<VirtualFluid, R>) this.entry(name, c -> new VirtualFluidBuilder<>(this.self(), this.self(), name, c, still, flow, CreateRegistrate::defaultFluidType, VirtualFluid::createSource, VirtualFluid::createFlowing));
    };

    public <T extends DisplaySource> SimpleBuilder<DisplaySource, T, R> displaySource(String name, Supplier<T> supplier) {
        return (SimpleBuilder<DisplaySource, T, R>) this.entry(name, callback -> new SimpleBuilder<DisplaySource, T, R>(this, this.self(), name, callback, CreateRegistries.DISPLAY_SOURCE, supplier).byBlock(DisplaySource.BY_BLOCK).byBlockEntity(DisplaySource.BY_BLOCK_ENTITY));
    };

    @Override
    public <T extends BlockEntity> DestroyBlockEntityBuilder<T, R> blockEntity(String name, BlockEntityFactory<T> factory) {
        return this.blockEntity(this.self(), name, factory);
    };

    public <T extends BlockEntity, P> DestroyBlockEntityBuilder<T, P> blockEntity(P parent, String name, BlockEntityFactory<T> factory) {
        return (DestroyBlockEntityBuilder<T, P>) this.entry(name, callback -> new DestroyBlockEntityBuilder<>(this, parent, name, callback, factory));
    };
    
};
