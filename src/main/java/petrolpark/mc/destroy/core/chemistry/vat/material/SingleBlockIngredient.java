package petrolpark.mc.destroy.core.chemistry.vat.material;

import petrolpark.mc.destroy.Destroy;
import petrolpark.mc.library.core.data.recipe.ingredient.BlockIngredient;

import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * A single Block. The Petrolpark Library had this in 1.20.1 ({@code BlockIngredient.SingleBlockIngredient}) but no longer does.
 * Like the original it has no {@code equals}, so two instances for the same Block are different map keys.
 * Its type must be registered with {@link BlockIngredient#registerType} (done in {@link Destroy#init}) so it can be synced.
 */
public class SingleBlockIngredient implements BlockIngredient<SingleBlockIngredient> {

    public static final Type TYPE = new Type();

    public final Block block;

    public SingleBlockIngredient(Block block) {
        this.block = block;
    };

    @Override
    public BlockIngredientType<SingleBlockIngredient> getType() {
        return TYPE;
    };

    @Override
    public boolean isValid(BlockState state) {
        return state.is(block);
    };

    @Override
    public NonNullList<ItemStack> getDisplayedItemStacks() {
        return NonNullList.of(ItemStack.EMPTY, new ItemStack(block.asItem()));
    };

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeResourceLocation(BuiltInRegistries.BLOCK.getKey(block));
    };

    public static class Type implements BlockIngredientType<SingleBlockIngredient> {

        public static final ResourceLocation ID = Destroy.asResource("single_block");

        @Override
        public SingleBlockIngredient read(FriendlyByteBuf buffer) {
            return new SingleBlockIngredient(BuiltInRegistries.BLOCK.get(buffer.readResourceLocation()));
        };

        @Override
        public ResourceLocation getId() {
            return ID;
        };
    };
};
