package petrolpark.mc.destroy.core.explosion.mixedexplosive;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.component.DyedItemColor;
import java.util.List;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * You must call {@link IDyeableMixedExplosiveBlockEntity#onPlace} when the block associated with this Block Entity gets placed,
 * and {@link IDyeableMixedExplosiveBlockEntity#getFilledItemStack} for pick-block and the drop.
 * It's also good to call {@link IDyeableMixedExplosiveBlockEntity#tryDye} in the useOn method of the Block.
 */
public interface IDyeableMixedExplosiveBlockEntity extends IMixedExplosiveBlockEntity {
    
    public void setColor(int color);

    @OnlyIn(Dist.CLIENT)
    public static void reRender(Level level, BlockPos blockPos) {
        SectionPos pos = SectionPos.of(blockPos);
        if (level instanceof ClientLevel clientLevel) clientLevel.setSectionDirtyWithNeighbors(pos.x(), pos.y(), pos.z());
    };

    public int getColor();

    @Override
    public default void onPlace(ItemStack blockItemStack, HolderLookup.Provider registries) {
        IMixedExplosiveBlockEntity.super.onPlace(blockItemStack, registries);
        if (blockItemStack.getItem() instanceof DyeableMixedExplosiveBlockItem dyeableItem) setColor(dyeableItem.getColor(blockItemStack));
    };

    @Override
    public default ItemStack getFilledItemStack(ItemStack emptyItemStack, HolderLookup.Provider registries) {
        if (emptyItemStack.getItem() instanceof DyeableMixedExplosiveBlockItem dyeableItem) dyeableItem.setColor(emptyItemStack, getColor());
        return IMixedExplosiveBlockEntity.super.getFilledItemStack(emptyItemStack, registries);
    };

    public default InteractionResult tryDye(ItemStack dyeStack, HitResult target, Level level, BlockPos pos, Player player) {
        if (!(dyeStack.getItem() instanceof DyeItem dyeItem)) return InteractionResult.PASS;
        ItemStack stack = level.getBlockState(pos).getCloneItemStack(target, level, pos, player);
        if (stack.getItem() instanceof DyeableMixedExplosiveBlockItem dyeableItem) {
            setColor(dyeableItem.getColor(DyedItemColor.applyDyes(stack, List.of(dyeItem))));
            if (!player.isCreative()) dyeStack.shrink(1);
            return InteractionResult.sidedSuccess(level.isClientSide());
        };
        return InteractionResult.PASS;
    };

    @Override
    default boolean readFromClipboard(HolderLookup.Provider registries, CompoundTag tag, Player player, Direction side, boolean simulate) {
        boolean invCopied = IMixedExplosiveBlockEntity.super.readFromClipboard(registries, tag, player, side, simulate);
        if (tag.contains("Color", Tag.TAG_INT)) {
            if (!simulate) setColor(tag.getInt("Color"));
            return true;
        };
        return invCopied;
    };

    @Override
    default boolean writeToClipboard(HolderLookup.Provider registries, CompoundTag tag, Direction side) {
        IMixedExplosiveBlockEntity.super.writeToClipboard(registries, tag, side);
        tag.putInt("Color", getColor());
        return true;
    };
    
};
