package petrolpark.mc.destroy.core.explosion.mixedexplosive;

import net.minecraft.core.component.DataComponents;
import java.util.function.Consumer;

import petrolpark.mc.destroy.DestroyBlocks;
import petrolpark.mc.destroy.config.DestroyAllConfigs;
import petrolpark.mc.destroy.core.explosion.mixedexplosive.ExplosiveProperties.ExplosivePropertyCondition;
import com.simibubi.create.foundation.item.render.SimpleCustomRenderer;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

public class MixedExplosiveBlockItem extends DyeableMixedExplosiveBlockItem {

    public MixedExplosiveBlockItem(Block block, Properties properties) {
        super(block, properties);
    };

    public static ItemStack getExampleItemStack() {
        ItemStack stack = DestroyBlocks.CUSTOM_EXPLOSIVE_MIX.asStack();
        if (stack.getItem() instanceof DyeableMixedExplosiveBlockItem item) item.setColor(stack, 0x85B09A);
        stack.set(DataComponents.CUSTOM_NAME, Component.literal("MIX"));
        return stack;
    };

    @Override
    public int getExplosiveInventorySize() {
        return DestroyAllConfigs.SERVER.blocks.customExplosiveMixSize.get();
    };

    @Override
    public ExplosivePropertyCondition[] getApplicableExplosionConditions() {
        return MixedExplosiveBlockEntity.EXPLOSIVE_PROPERTY_CONDITIONS;
    };

    @OnlyIn(Dist.CLIENT)
    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(SimpleCustomRenderer.create(this, new MixedExplosiveBlockItemRenderer()));
    };
    
};
