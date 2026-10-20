package petrolpark.mc.destroy.core.block;

import java.util.function.Supplier;

import petrolpark.mc.destroy.DestroyVoxelShapes;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class FullyGrownCropBlock extends BushBlock {

    public static final MapCodec<FullyGrownCropBlock> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
        propertiesCodec(),
        BuiltInRegistries.ITEM.byNameCodec().fieldOf("seed").forGetter(FullyGrownCropBlock::asItem)
    ).apply(i, (properties, seed) -> new FullyGrownCropBlock(properties, () -> seed)));

    private Supplier<? extends Item> seed;

    public FullyGrownCropBlock(Properties properties, Supplier<? extends Item> seed) {
        super(properties);
        this.seed = seed;
    };

    @Override
    protected MapCodec<? extends FullyGrownCropBlock> codec() {
        return CODEC;
    };

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(Blocks.FARMLAND);
    };

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return (level.getRawBrightness(pos, 0) >= 8 || level.canSeeSky(pos)) && super.canSurvive(state, level, pos);
    };

    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
        return new ItemStack(seed.get());
    };

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext pContext) {
        return DestroyVoxelShapes.CROP;
    };

    @Override
    public Item asItem() {
        return seed.get();
    };
    
};
