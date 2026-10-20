package petrolpark.mc.destroy.core.chemistry.storage.testtube;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import petrolpark.mc.destroy.DestroyItems;
import petrolpark.mc.destroy.core.chemistry.hazard.ChemistryHazardHelper;
import petrolpark.mc.destroy.core.chemistry.storage.IMixtureStorageItem;
import petrolpark.mc.destroy.core.item.ILayerTintsWithAlphaItem;
import com.simibubi.create.foundation.item.render.SimpleCustomRenderer;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.fluids.FluidStack;

public class TestTubeItem extends Item implements ILayerTintsWithAlphaItem, IMixtureStorageItem {

    public static final int CAPACITY = 200;

    public TestTubeItem(Properties properties) {
        super(properties);
    };

    public ItemStack of(FluidStack fluidStack) {
        ItemStack stack = DestroyItems.TEST_TUBE.asStack(1);
        setContents(stack, fluidStack);
        return stack;
    };

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        Optional<FluidStack> contentsOptional = getContents(stack);
        if (entity instanceof LivingEntity livingEntity && contentsOptional.isPresent()) {
            ChemistryHazardHelper.damage(level, livingEntity, contentsOptional.get(), false);
        };
    };

    @Override
    public InteractionResult useOn(UseOnContext context) {
        return IMixtureStorageItem.defaultUseOn(this, context);
    };

    @Override
	public boolean canAttackBlock(BlockState state, Level level, BlockPos pos, Player player) {
		return getTank(level, pos, state, null, player, InteractionHand.MAIN_HAND, player.getItemInHand(InteractionHand.MAIN_HAND), false) == null;
	};

    @Override
    public Component getNameRegardlessOfFluid(ItemStack stack) {
        return super.getName(stack);
    };

    @Override
    public Component getName(ItemStack itemStack) {
        return getNameWithFluid(itemStack);
    };

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, Item.TooltipContext tooltipContext, List<Component> tooltip, TooltipFlag isAdvanced) {
        super.appendHoverText(stack, tooltipContext, tooltip, isAdvanced);
        addContentsDescription(stack, tooltip);
    };

    @Override
    @OnlyIn(Dist.CLIENT)
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(SimpleCustomRenderer.create(this, new TestTubeItemRenderer()));
    };

    @Override
    public int getCapacity(ItemStack stack) {
        return CAPACITY;
    };

};
