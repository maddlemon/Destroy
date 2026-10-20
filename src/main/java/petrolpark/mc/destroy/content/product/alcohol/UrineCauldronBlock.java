package petrolpark.mc.destroy.content.product.alcohol;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;

public class UrineCauldronBlock extends AbstractCauldronBlock {

    public static final MapCodec<UrineCauldronBlock> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
        propertiesCodec(),
        CauldronInteraction.CODEC.fieldOf("interactions").forGetter(block -> block.interactions)
    ).apply(i, UrineCauldronBlock::new));

    public UrineCauldronBlock(Properties properties, CauldronInteraction.InteractionMap interactions) {
        super(properties, interactions);
    };

    @Override
    protected MapCodec<? extends UrineCauldronBlock> codec() {
        return CODEC;
    };

    @Override
    public boolean isFull(BlockState state) {
        return true;
    };

    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pPos) {
        return 2;
    };

};