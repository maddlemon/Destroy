package petrolpark.mc.destroy.content.processing.phytomining;

import java.util.function.Supplier;

import petrolpark.mc.destroy.DestroyVoxelShapes;
import petrolpark.mc.destroy.core.block.FullyGrownCropBlock;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class HeftyBeetrootBlock extends FullyGrownCropBlock {

    public static final MapCodec<HeftyBeetrootBlock> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
        propertiesCodec(),
        BuiltInRegistries.ITEM.byNameCodec().fieldOf("seed").forGetter(HeftyBeetrootBlock::asItem)
    ).apply(i, (properties, seed) -> new HeftyBeetrootBlock(properties, () -> seed)));

    public HeftyBeetrootBlock(Properties properties, Supplier<? extends Item> seed) {
        super(properties, seed);
    };

    @Override
    protected MapCodec<? extends HeftyBeetrootBlock> codec() {
        return CODEC;
    };

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext pContext) {
        return DestroyVoxelShapes.HEFTY_BEETROOT;
    };
    
};
