package petrolpark.mc.destroy.core.event;

import petrolpark.mc.destroy.Destroy;
import petrolpark.mc.destroy.DestroyAttributes;
import petrolpark.mc.destroy.content.processing.trypolithography.RegenerateCircuitPatternCommand.CircuitPatternIdArgument;

import com.simibubi.create.foundation.pack.ModFilePackResources;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.forgespi.language.IModFileInfo;
import net.neoforged.forgespi.locating.IModFile;
import net.neoforged.neoforge.registries.RegisterEvent;

@EventBusSubscriber(modid = Destroy.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class DestroyCommonModEvents {
    
    @SubscribeEvent
    public static final void onCreateAttributes(EntityAttributeModificationEvent event) {
        event.add(EntityType.PLAYER, DestroyAttributes.EXTRA_INVENTORY_SIZE.get());
        event.add(EntityType.PLAYER, DestroyAttributes.EXTRA_HOTBAR_SLOTS.get());
    };

    /**
     * Copied from the {@link com.simibubi.create.events.CommonEvents.ModBusEvents#addPackFinders Create source code}.
     * Add the Schematicannon Tooltip resource pack, which replaces the text of tooltips in Schematicannons
     * to reflect that they can accept any Destroy explosive, not just gunpowder.
     */
    @SubscribeEvent
    public static final void addPackFinders(AddPackFindersEvent event) {
        IModFileInfo modFileInfo = ModList.get().getModFileById(Destroy.MOD_ID);
        if (modFileInfo == null) {
            Destroy.LOGGER.error("Could not find Destroy mod file info; built-in resource packs will be missing!");
            return;
        };
        IModFile modFile = modFileInfo.getFile();
        if (event.getPackType() == PackType.CLIENT_RESOURCES) {
            // Resource packs
            event.addRepositorySource(consumer -> {
                Pack pack = Pack.readMetaAndCreate(Destroy.asResource("create_patches").toString(), Component.literal("Destroy Patches For Create"), true, id -> new ModFilePackResources(id, modFile, "resourcepacks/create_patches"), PackType.CLIENT_RESOURCES, Pack.Position.TOP, PackSource.BUILT_IN);
                if (pack != null) consumer.accept(pack);
            });
        } else {
            // Datapacks
            event.addRepositorySource(consumer -> {
                Pack pack = Pack.readMetaAndCreate(Destroy.asResource("tfmg_compat").toString(), Component.literal("Destroy Compat With Create: TFMG"), false, id -> new ModFilePackResources(id, modFile, "datapacks/tfmg_compat"), PackType.SERVER_DATA, Pack.Position.TOP, PackSource.DEFAULT);
                if (pack != null) consumer.accept(pack);
            });
        };
    };

    @SubscribeEvent
    public static final void registerCommandArgumentTypes(RegisterEvent event) {
        event.register(Registries.COMMAND_ARGUMENT_TYPE, Destroy.asResource("circuit_pattern_resource_location"), () -> {
            return ArgumentTypeInfos.registerByClass(CircuitPatternIdArgument.class, SingletonArgumentInfo.contextFree(CircuitPatternIdArgument::create));
        });
    };
};
