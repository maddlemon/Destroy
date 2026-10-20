package petrolpark.mc.destroy.core.event;

import petrolpark.mc.destroy.Destroy;
import petrolpark.mc.destroy.DestroyAttributes;
import petrolpark.mc.destroy.core.item.DecayingItem;
import petrolpark.mc.destroy.content.processing.trypolithography.RegenerateCircuitPatternCommand.CircuitPatternIdArgument;

import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.RegisterEvent;

@EventBusSubscriber(modid = Destroy.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class DestroyCommonModEvents {
    
    @SubscribeEvent
    public static final void onCreateAttributes(EntityAttributeModificationEvent event) {
        event.add(EntityType.PLAYER, DestroyAttributes.EXTRA_INVENTORY_SIZE);
        event.add(EntityType.PLAYER, DestroyAttributes.EXTRA_HOTBAR_SLOTS);
    };

    @SubscribeEvent
    public static final void modifyDefaultComponents(ModifyDefaultComponentsEvent event) {
        DecayingItem.setDefaultComponents(event);
    };

    /**
     * Copied from the {@link com.simibubi.create.events.CommonEvents.ModBusEvents#addPackFinders Create source code}.
     * Add the Schematicannon Tooltip resource pack, which replaces the text of tooltips in Schematicannons
     * to reflect that they can accept any Destroy explosive, not just gunpowder.
     */
    @SubscribeEvent
    public static final void addPackFinders(AddPackFindersEvent event) {
        // Resource packs (always on, as 1.20.1's was "required")
        event.addPackFinders(Destroy.asResource("resourcepacks/create_patches"), PackType.CLIENT_RESOURCES, Component.literal("Destroy Patches For Create"), PackSource.BUILT_IN, true, Pack.Position.TOP);
        // Datapacks
        event.addPackFinders(Destroy.asResource("datapacks/tfmg_compat"), PackType.SERVER_DATA, Component.literal("Destroy Compat With Create: TFMG"), PackSource.DEFAULT, false, Pack.Position.TOP);
    };
    @SubscribeEvent
    public static final void registerCommandArgumentTypes(RegisterEvent event) {
        event.register(Registries.COMMAND_ARGUMENT_TYPE, Destroy.asResource("circuit_pattern_resource_location"), () -> {
            return ArgumentTypeInfos.registerByClass(CircuitPatternIdArgument.class, SingletonArgumentInfo.contextFree(CircuitPatternIdArgument::create));
        });
    };
};
