package petrolpark.mc.destroy.compat.createbigcannons.block;

import static petrolpark.mc.destroy.Destroy.REGISTRATE;

import petrolpark.mc.destroy.compat.createbigcannons.DestroyMunitionPropertiesHandlers;
import petrolpark.mc.destroy.compat.createbigcannons.item.CustomExplosiveMixChargeBlockItem;
import petrolpark.mc.destroy.compat.createbigcannons.item.CustomExplosiveMixShellBlockItem;
import petrolpark.mc.destroy.compat.jei.DestroyJEISetup;
import com.tterrag.registrate.util.entry.BlockEntry;

import net.minecraft.world.item.ItemStack;
import rbasamoyai.createbigcannons.index.CBCBlocks;
import rbasamoyai.createbigcannons.munitions.config.MunitionPropertiesHandler;

public class CreateBigCannonsBlocks {

    public static final BlockEntry<CustomExplosiveMixChargeBlock> CUSTOM_EXPLOSIVE_MIX_CHARGE = REGISTRATE.block("custom_explosive_mix_charge", CustomExplosiveMixChargeBlock::new)
        .initialProperties(CBCBlocks.POWDER_CHARGE)
        .properties(p -> p
            .noLootTable() // Handled in CustomExplosiveMixChargeBlock class
        ).onRegister(block -> MunitionPropertiesHandler.registerBlockPropellantHandler(block, DestroyMunitionPropertiesHandlers.CUSTOM_EXPLOSIVE_MIX_CHARGE))
        .item(CustomExplosiveMixChargeBlockItem::new)
        .onRegister(item -> DestroyJEISetup.CUSTOM_MIX_EXPLOSIVES.add(() -> new ItemStack(item)))
        .build()
        .register();

    public static final BlockEntry<CustomExplosiveMixShellBlock> CUSTOM_EXPLOSIVE_MIX_SHELL = REGISTRATE.block("custom_explosive_mix_shell", CustomExplosiveMixShellBlock::new)
        .initialProperties(CBCBlocks.FLUID_SHELL)
        .properties(p -> p
            .noLootTable() // Handled in CustomExplosiveMixChargeBlock class
        ).item(CustomExplosiveMixShellBlockItem::new)
        .onRegister(item -> DestroyJEISetup.CUSTOM_MIX_EXPLOSIVES.add(() -> new ItemStack(item)))
        .build()
        .register();
    
    public static void register() {};
};
