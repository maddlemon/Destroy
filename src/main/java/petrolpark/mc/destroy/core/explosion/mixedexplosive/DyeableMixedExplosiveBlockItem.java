package petrolpark.mc.destroy.core.explosion.mixedexplosive;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
 
/**
 * Dyeable like leather armour: in 1.21 the colour is the {@code minecraft:dyed_color} component, and crafting-grid dyeing comes from the
 * {@code minecraft:dyeable} item tag (the item is tagged in {@code DestroyBlocks}).
 */
public abstract class DyeableMixedExplosiveBlockItem extends BlockItem implements IMixedExplosiveItem {

    public static final int DEFAULT_COLOR = 0xFFFFFF;

    public DyeableMixedExplosiveBlockItem(Block block, Properties properties) {
        super(block, properties);
    };

    /**
     * @return The RGB colour of this Stack (no alpha)
     */
    public int getColor(ItemStack stack) {
        return DyedItemColor.getOrDefault(stack, DEFAULT_COLOR);
    };

    public void setColor(ItemStack stack, int color) {
        stack.set(DataComponents.DYED_COLOR, new DyedItemColor(color, true));
    };

    @Override
    public int getExplosiveInventorySize() {
        return 16;
    };

    public ItemStack fromStructureInfo(StructureBlockInfo info, HolderLookup.Provider registries) {
        ItemStack stack = new ItemStack(this);
        setColor(stack, info.nbt().getInt("Color"));
        MixedExplosiveInventory inv = new MixedExplosiveInventory(getExplosiveInventorySize());
        inv.deserializeNBT(registries, info.nbt().getCompound("ExplosiveMix"));
        setExplosiveInventory(stack, inv, registries);
        return stack;
    };

    public StructureBlockInfo toStructureInfo(BlockPos localPos, BlockState state, ItemStack stack, HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("Color", getColor(stack));
        tag.put("ExplosiveMix", getExplosiveInventory(stack, registries).serializeNBT(registries));
        return new StructureBlockInfo(localPos, state, tag);
    };
    
};
