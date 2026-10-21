package petrolpark.mc.destroy.core.registrate;

import java.util.function.Function;

import javax.annotation.Nullable;
import java.util.function.Supplier;

import com.mojang.datafixers.util.Function3;
import com.simibubi.create.api.behaviour.display.DisplaySource;
import com.simibubi.create.api.registry.CreateRegistries;
import com.simibubi.create.api.registry.registrate.SimpleBuilder;
import com.simibubi.create.content.fluids.VirtualFluid;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.data.VirtualFluidBuilder;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.item.TooltipModifier;
import com.tterrag.registrate.builders.BlockEntityBuilder.BlockEntityFactory;
import com.tterrag.registrate.builders.Builder;
import com.tterrag.registrate.builders.FluidBuilder;
import com.tterrag.registrate.builders.FluidBuilder.FluidTypeFactory;
import com.tterrag.registrate.util.entry.RegistryEntry;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import com.tterrag.registrate.util.nullness.NonNullSupplier;

import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import petrolpark.mc.destroy.DestroyRegistries;
import petrolpark.mc.destroy.core.pollution.PollutionType;
import petrolpark.mc.destroy.core.registrate.builder.DestroyBlockEntityBuilder;
import petrolpark.mc.library.core.registrate.AbstractPetrolparkRegistrate;
import petrolpark.mc.library.core.registrate.builder.PetrolparkItemBuilder;

public abstract class AbstractDestroyRegistrate<R extends AbstractPetrolparkRegistrate<R>> extends AbstractPetrolparkRegistrate<R> {

    protected Function<Item, TooltipModifier> tooltipModifierFactory;

    protected AbstractDestroyRegistrate(String modid) {
        super(modid);
    };

    // Create's item tooltips (CreateRegistrate#setTooltipModifierFactory; the library's registrate has no equivalent)

    @SuppressWarnings("unchecked")
    public R setTooltipModifierFactory(Function<Item, TooltipModifier> factory) {
        tooltipModifierFactory = factory;
        return (R)this;
    };

    /**
     * Registrate puts every Item in its default Creative Tab (the search tab unless changed), which is how Items not
     * in Destroy's own tab could still be found in creative search in 1.20.1. The library's builder skips that step.
     */
    @Override
    public <T extends Item, P> PetrolparkItemBuilder<T, P> item(P parent, String name, NonNullFunction<Item.Properties, T> factory) {
        PetrolparkItemBuilder<T, P> builder = super.item(parent, name, factory);
        builder.tab(CreativeModeTabs.SEARCH, (item, modifier) -> {
            // NeoForge throws on Stacks already in the tab (Forge ignored them), and Items in Destroy's own tab already are
            ItemStack stack = new ItemStack(item.get());
            if (buildingTab != null && (buildingTab.getSearchEntries().contains(stack) || buildingTab.getParentEntries().contains(stack))) return;
            modifier.accept(stack, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
        });
        return builder;
    };

    @Nullable
    private BuildCreativeModeTabContentsEvent buildingTab;

    @Override
    protected void onBuildCreativeModeTabContents(BuildCreativeModeTabContentsEvent event) {
        buildingTab = event;
        try {
            super.onBuildCreativeModeTabContents(event);
        } finally {
            buildingTab = null;
        };
    };

    @Override
    protected <REG, T extends REG> RegistryEntry<REG, T> accept(String name, ResourceKey<? extends Registry<REG>> type, Builder<REG, T, ?, ?> builder, NonNullSupplier<? extends T> creator, NonNullFunction<DeferredHolder<REG, T>, ? extends RegistryEntry<REG, T>> entryFactory) {
        RegistryEntry<REG, T> entry = super.accept(name, type, builder, creator, entryFactory);
        if (type.equals(Registries.ITEM) && tooltipModifierFactory != null) {
            Function<Item, TooltipModifier> factory = tooltipModifierFactory;
            this.<Item, Item>addRegisterCallback(name, Registries.ITEM, item -> TooltipModifier.REGISTRY.register(item, factory.apply(item)));
        };
        return entry;
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
