package petrolpark.mc.destroy.compat.createbigcannons.block.entity;

import static petrolpark.mc.destroy.Destroy.REGISTRATE;

import petrolpark.mc.destroy.compat.createbigcannons.block.CreateBigCannonsBlocks;
import com.tterrag.registrate.util.entry.BlockEntityEntry;

import rbasamoyai.createbigcannons.munitions.big_cannon.FuzedBlockEntityRenderer;
import rbasamoyai.createbigcannons.munitions.big_cannon.FuzedBlockVisual;

public class CreateBigCannonBlockEntityTypes {

    public static final BlockEntityEntry<CustomExplosiveMixChargeBlockEntity> CUSTOM_EXPLOSIVE_MIX_CHARGE = REGISTRATE
        .blockEntity("custom_explosive_mix_charge", CustomExplosiveMixChargeBlockEntity::new)
        .validBlock(CreateBigCannonsBlocks.CUSTOM_EXPLOSIVE_MIX_CHARGE)
        .register();

    public static final BlockEntityEntry<CustomExplosiveMixShellBlockEntity> CUSTOM_EXPLOSIVE_MIX_SHELL = REGISTRATE
        .blockEntity("custom_explosive_mix_shell", CustomExplosiveMixShellBlockEntity::new)
        .visual(() -> FuzedBlockVisual::new)
		.renderer(() -> FuzedBlockEntityRenderer::new)
        .validBlock(CreateBigCannonsBlocks.CUSTOM_EXPLOSIVE_MIX_SHELL)
        .register();

    public static void register() {};
    
};
