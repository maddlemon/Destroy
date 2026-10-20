package petrolpark.mc.destroy.content.product.periodictable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class TankPeriodicTableBlock extends PeriodicTableBlock {

    public static final MapCodec<TankPeriodicTableBlock> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
        propertiesCodec(),
        Codec.INT.fieldOf("color").forGetter(block -> block.color)
    ).apply(i, TankPeriodicTableBlock::new));

    public final int color;

    public TankPeriodicTableBlock(Properties properties, int color) {
        super(properties);
        this.color = color;
    };

    @Override
    protected MapCodec<? extends TankPeriodicTableBlock> codec() {
        return CODEC;
    };

    public VoxelShape getVisualShape(BlockState pState, BlockGetter pReader, BlockPos pPos, CollisionContext pContext) {
        return Shapes.empty();
    };

    public float getShadeBrightness(BlockState pState, BlockGetter pLevel, BlockPos pPos) {
        return 1.0F;
    };

    public boolean propagatesSkylightDown(BlockState pState, BlockGetter pReader, BlockPos pPos) {
        return true;
    };
    
};
