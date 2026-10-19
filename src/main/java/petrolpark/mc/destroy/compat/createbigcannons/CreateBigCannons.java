package petrolpark.mc.destroy.compat.createbigcannons;

import petrolpark.mc.destroy.compat.createbigcannons.block.CreateBigCannonsBlocks;
import petrolpark.mc.destroy.compat.createbigcannons.block.entity.CreateBigCannonBlockEntityTypes;
import petrolpark.mc.destroy.compat.createbigcannons.entity.CreateBigCannonsEntityTypes;
import petrolpark.mc.destroy.compat.createbigcannons.event.CreateBigCannonsClientModEvents;
import petrolpark.mc.destroy.compat.createbigcannons.ponder.CreateBigCannonsPonderPlugin;

import net.createmod.ponder.foundation.PonderIndex;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.DistExecutor;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

public class CreateBigCannons {
    
    public static void init(IEventBus modEventBus, IEventBus forgeEventBus) {
        DestroyMunitionPropertiesHandlers.init();
        CreateBigCannonsBlocks.register();
        CreateBigCannonBlockEntityTypes.register();
        CreateBigCannonsEntityTypes.register();

        // Initiation events
        modEventBus.addListener(CreateBigCannons::onClientSetup);
        forgeEventBus.addListener(CreateBigCannons::onCommonSetup);
        
        // Client
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> modEventBus.register(CreateBigCannonsClientModEvents.class));
    };

    public static void onCommonSetup(FMLCommonSetupEvent event) {
        DestroyBlobEffects.registerBlobEffects();
    };

    public static void onClientSetup(FMLClientSetupEvent event) {
        //CreateBigCannonsPonderScenes.register();
        //CreateBigCannonsPonderScenes.registerTags();
        PonderIndex.addPlugin(new CreateBigCannonsPonderPlugin());
    };
};
