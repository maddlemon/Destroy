package petrolpark.mc.destroy;

import petrolpark.mc.destroy.core.chemistry.vat.material.SingleBlockIngredient;
import petrolpark.mc.library.core.data.recipe.ingredient.BlockIngredient;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import petrolpark.mc.destroy.chemistry.api.Chemistry;
import petrolpark.mc.destroy.chemistry.forge.event.ForgeChemistryEventFirer;
import petrolpark.mc.destroy.chemistry.legacy.index.DestroyGenericReactions;
import petrolpark.mc.destroy.chemistry.legacy.index.DestroyGroupFinder;
import petrolpark.mc.destroy.chemistry.legacy.index.DestroyMolecules;
import petrolpark.mc.destroy.chemistry.legacy.index.DestroyReactions;
import petrolpark.mc.destroy.chemistry.legacy.index.DestroyTopologies;
import petrolpark.mc.destroy.client.DestroyMenuTypes;
import petrolpark.mc.destroy.client.DestroyParticleTypes;
import petrolpark.mc.destroy.config.DestroyAllConfigs;
import petrolpark.mc.destroy.content.processing.trypolithography.CircuitPatternHandler;
import petrolpark.mc.destroy.content.processing.trypolithography.CircuitPuncherHandler;
import petrolpark.mc.destroy.core.chemistry.vat.material.VatMaterial;
import petrolpark.mc.destroy.core.data.DestroyTagDatagen;
import com.simibubi.create.content.equipment.goggles.GogglesItem;

import net.minecraft.resources.ResourceLocation;
import java.util.function.Supplier;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

@Mod(Destroy.MOD_ID)
public class Destroy {
    public static final String MOD_ID = "destroy";
    public static final String NAME = "Destroy";

    // Utility
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final boolean datagen = false;

    // Registrate
    public static final DestroyRegistrate REGISTRATE = new DestroyRegistrate();

    // Level-attached managers
    public static final CircuitPuncherHandler CIRCUIT_PUNCHER_HANDLER = new CircuitPuncherHandler();
    public static final CircuitPatternHandler CIRCUIT_PATTERN_HANDLER = new CircuitPatternHandler();

    public static ResourceLocation asResource(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    };

    // Really early stuff
    // TODO re-wire Create-style item tooltips (library has no global tooltip factory on 1.21.1)

    // Initiation
    public Destroy(IEventBus modEventBus, ModContainer modContainer) {
        ModLoadingContext modLoadingContext = ModLoadingContext.get();
        IEventBus forgeEventBus = NeoForge.EVENT_BUS;

        REGISTRATE.registerEventListeners(modEventBus);

        // Mod objects
        if (!datagen) DestroySoundEvents.prepare(); // Sound datagen is broken and I can't be bothered to fix it
        DestroyCreativeModeTabs.register(modEventBus);
        DestroyTags.register();
        DestroyBlockEntityTypes.register();
        DestroyBlocks.register();
        DestroyMobEffects.register(modEventBus);
        DestroyPotions.register(modEventBus);
        DestroyItems.register();
        DestroyMenuTypes.register();
        DestroyRecipeTypes.register(modEventBus);
        DestroyParticleTypes.register(modEventBus);
        DestroyFluids.register();
        DestroyCropMutations.register();
        DestroyEntityTypes.register();
        DestroyVillagers.register(modEventBus);
        DestroyLoot.register(modEventBus);
        DestroyDamageTypes.register();
        DestroyStats.register(modEventBus);
        DestroyAttributes.register(modEventBus);
        DestroyMovementChecks.register();
        DestroyDisplaySources.register();
        DestroyCriteriaTriggers.register();
        // Registries from the author's 1.21.1-neo branch (dropped when the 1.20.1 Destroy.java was restored)
        DestroyAttachmentTypes.register(modEventBus);
        DestroyDataComponentTypes.register(modEventBus);
        DestroyFluidIngredientTypes.register(modEventBus);
        DestroyIngredientTypes.register(modEventBus);
        DestroyNumberProviderTypes.register();
        DestroyPollutionTypes.register();
        DestroySeismologyProviders.register();
        DestroyPackets.register(); // Payload types must exist before RegisterPayloadHandlersEvent, so not in common setup

        // Events
        NeoForge.EVENT_BUS.register(this);

        // Config
        DestroyAllConfigs.register(modLoadingContext, modContainer);

        // Initiation Events
        modEventBus.addListener(Destroy::init);
        modEventBus.addListener(Destroy::onRegister);
        if (!datagen) modEventBus.addListener(DestroySoundEvents::register);
        modEventBus.addListener(DestroyClient::clientInit);
        modEventBus.addListener(EventPriority.LOWEST, Destroy::gatherData);

        // Client
        unsafeRunClient(() -> () -> DestroyClient.clientCtor(modEventBus, forgeEventBus));

    };

    // Initiation Events

    public static void init(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            Chemistry.initiate(new ForgeChemistryEventFirer(), Destroy.LOGGER::info);
            DestroyCompostables.register();
            BlockIngredient.registerType(SingleBlockIngredient.TYPE);
        });
        DestroyStats.register();
        VatMaterial.registerDestroyVatMaterials();
        DestroyOpenEndedPipeEffectHandlers.register();
        DestroyAdvancementTrigger.register();
        DestroyBlockExtrusions.register();

        // Chemistry
        DestroyGroupFinder.register();
        DestroyTopologies.register();
        DestroyMolecules.register();
        DestroyReactions.register();
        DestroyGenericReactions.register();

        // Config
        GogglesItem.addIsWearingPredicate(player -> player.isCreative() && DestroyAllConfigs.SERVER.automaticGoggles.get());
    };


    public static void onRegister(final RegisterEvent event) {
        DestroyItemAttributeTypes.init();
        DestroyPotatoProjectileEntityHitActions.init();
        DestroyPotatoProjectileBlockHitActions.init();
    }

    // Datagen
    public static void gatherData(GatherDataEvent event) {
        DestroyTagDatagen.addGenerators();
    };
    public static final <T> T runForDist(Supplier<Supplier<T>> clientSupplier, Supplier<Supplier<T>> serverSupplier) {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            return clientSupplier.get().get();
        } else {
            return serverSupplier.get().get();
        };
    };

    public static final <T> T unsafeCallClient(Supplier<Supplier<T>> supplier) {
        try {
            if (FMLEnvironment.dist == Dist.CLIENT) supplier.get().get();
        } catch (Exception e) {
            throw new RuntimeException();
        };
        return null;
    };

    public static final void unsafeRunClient(Supplier<Runnable> supplier) {
        try {
            if (FMLEnvironment.dist == Dist.CLIENT) supplier.get().run();
        } catch (Exception e) {
            throw new RuntimeException();
        };
    };

};
