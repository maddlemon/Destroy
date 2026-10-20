package petrolpark.mc.destroy.util;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;

public final class BlockUseHelper {

    private BlockUseHelper() {};

    /**
     * 1.20.1's {@code Block#use} became {@code useItemOn} (called for each hand, whatever is held, just like {@code use} was) and
     * {@code useWithoutItem}. Blocks keep their 1.20.1 {@code use} and override {@code useItemOn} to return its result through this.
     * {@code PASS} becomes {@link ItemInteractionResult#PASS_TO_DEFAULT_BLOCK_INTERACTION} so that, as before, the Item is used next.
     */
    public static ItemInteractionResult asItemInteractionResult(InteractionResult result) {
        return switch (result) {
            case SUCCESS, SUCCESS_NO_ITEM_USED -> ItemInteractionResult.SUCCESS;
            case CONSUME -> ItemInteractionResult.CONSUME;
            case CONSUME_PARTIAL -> ItemInteractionResult.CONSUME_PARTIAL;
            case PASS -> ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
            case FAIL -> ItemInteractionResult.FAIL;
        };
    };
};
