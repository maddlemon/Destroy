package petrolpark.mc.destroy.content.processing.trypolithography.recipe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import petrolpark.mc.destroy.DestroyRecipeTypes;
import petrolpark.mc.destroy.content.processing.trypolithography.CircuitPatternItem;
import petrolpark.mc.destroy.content.processing.trypolithography.recipe.CircuitDeployerApplicationRecipe.ExampleMaskIngredient;
import petrolpark.mc.library.core.world.item.crafting.ManualOnlyCraftingRecipe;

import net.createmod.catnip.data.Iterate;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.level.Level;

/**
 * A shaped recipe in which every {@code #} is a Circuit Mask, all of which must have the same pattern; the result gets that pattern.
 * Wraps a {@link ShapedRecipe} (whose mask slots are {@link ExampleMaskIngredient}s) so the Petrolpark Library's JEI category shows it,
 * like the 1.20.1 {@code ManualOnlyShapedRecipe} did. As in 1.20.1, {@link #matches} does not apply the manual-only restriction.
 */
public class ManualCircuitBoardRecipe extends ManualOnlyCraftingRecipe {

    protected final Item maskItem;
    protected final ShapedRecipePattern.Data patternData;
    protected final int[] maskPositions;

    private ManualCircuitBoardRecipe(ShapedRecipe wrappedRecipe, Item maskItem, ShapedRecipePattern.Data patternData, int[] maskPositions) {
        super(wrappedRecipe);
        this.maskItem = maskItem;
        this.patternData = patternData;
        this.maskPositions = maskPositions;
    };

    /**
     * @param patternData The key (not including the {@code #} mask symbol) and pattern
     */
    public static DataResult<ManualCircuitBoardRecipe> create(Item maskItem, ShapedRecipePattern.Data patternData, ItemStack result) {
        if (!(maskItem instanceof CircuitPatternItem)) return DataResult.error(() -> "Mask ingredient must be able to support circuit patterns.");
        if (!(result.getItem() instanceof CircuitPatternItem)) return DataResult.error(() -> "Result item must be able to support circuit patterns.");
        if (patternData.key().containsKey('#')) return DataResult.error(() -> "Invalid key entry: '#' is reserved for the Circuit Mask(s).");

        Map<Character, Ingredient> key = new HashMap<>(patternData.key());
        key.put('#', new ExampleMaskIngredient(Ingredient.of(maskItem)).toVanilla());
        ShapedRecipePattern pattern;
        try {
            pattern = ShapedRecipePattern.of(key, patternData.pattern());
        } catch (IllegalStateException e) {
            return DataResult.error(e::getMessage);
        };

        // Mask Ingredient positions
        List<Integer> maskPositions = new ArrayList<>(pattern.width() * pattern.height());
        for (int i = 0; i < pattern.width() * pattern.height(); i++) if (isMask(pattern.ingredients().get(i))) maskPositions.add(i);
        if (maskPositions.isEmpty()) return DataResult.error(() -> "Recipe must contain at least one mask");

        ShapedRecipe wrappedRecipe = new ShapedRecipe("", CraftingBookCategory.MISC, pattern, result);
        return DataResult.success(new ManualCircuitBoardRecipe(wrappedRecipe, maskItem, patternData, maskPositions.stream().mapToInt(i -> i).toArray()));
    };

    private static boolean isMask(Ingredient ingredient) {
        return ingredient.getCustomIngredient() instanceof ExampleMaskIngredient;
    };

    protected ShapedRecipe getShapedRecipe() {
        return (ShapedRecipe)wrappedRecipe;
    };

    public int getWidth() {
        return getShapedRecipe().getWidth();
    };

    public int getHeight() {
        return getShapedRecipe().getHeight();
    };

    @Override
    public RecipeSerializer<ManualOnlyCraftingRecipe> getSerializer() {
        return DestroyRecipeTypes.CIRCUIT_BOARD_MANUAL_CRAFTING.getSerializer();
    };

    @Override
    public boolean isSpecial() {
        return true; // As 1.20.1's ManualOnlyShapedRecipe
    };

    @Override
    public ItemStack getExampleResult(HolderLookup.Provider registries) {
        ItemStack stack = getResultItem(registries).copy();
        CircuitPatternItem.putPattern(stack, CircuitDeployerApplicationRecipe.EXAMPLE_PATTERN);
        return stack;
    };

    @Override
    public boolean matches(CraftingInput inv, Level level) {
        return !assemble(inv, level.registryAccess()).isEmpty();
    };

    @Override
    public ItemStack assemble(CraftingInput inv, HolderLookup.Provider registries) {
        ItemStack result = super.assemble(inv, registries).copy();
        // Check each position for the recipe within the container
        for (int i = 0; i <= inv.width() - getWidth(); i++) {
            tryEachPosition: for (int j = 0; j <= inv.height() - getHeight(); j++) {
                // Try both mirrored and non-mirrored
                for (boolean mirrored : Iterate.trueAndFalse) {
                    int thisPattern = -1;
                    // Test each item
                    for (int column = 0; column < inv.width(); column++) {
                        for (int row = 0; row < inv.height(); row++) {
                            int x = column - i;
                            int y = row - j;
                            Ingredient ingredient = Ingredient.EMPTY;
                            if (x >= 0 && y >= 0 && x < getWidth() && y < getHeight()) {
                                if (mirrored) ingredient = getIngredients().get(getWidth() - x - 1 + y * getWidth());
                                else ingredient = getIngredients().get(x + y * getWidth());
                            };

                            ItemStack stack = inv.getItem(column + row * inv.width());
                            if (isMask(ingredient)) {
                                if (!stack.getItem().equals(maskItem)) continue tryEachPosition;
                                int pattern = CircuitPatternItem.getPattern(stack);
                                if (thisPattern == -1) thisPattern = pattern;
                                else if (thisPattern != pattern) continue tryEachPosition;
                            } else {
                                if (!ingredient.test(stack)) continue tryEachPosition;
                            };
                        };
                    };
                    if (thisPattern == -1) continue tryEachPosition;
                    CircuitPatternItem.putPattern(result, thisPattern);
                    return result;
                };
            };
        };
        return ItemStack.EMPTY;
    };

    public static class Serializer implements RecipeSerializer<ManualCircuitBoardRecipe> {

        private static final StreamCodec<RegistryFriendlyByteBuf, ShapedRecipePattern.Data> PATTERN_DATA_STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.<RegistryFriendlyByteBuf, Character, Ingredient, Map<Character, Ingredient>>map(HashMap::new, ByteBufCodecs.STRING_UTF8.map(s -> s.charAt(0), String::valueOf), Ingredient.CONTENTS_STREAM_CODEC), ShapedRecipePattern.Data::key,
            ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()), ShapedRecipePattern.Data::pattern,
            ShapedRecipePattern.Data::new
        );

        public static final MapCodec<ManualCircuitBoardRecipe> CODEC = RecordCodecBuilder.<Definition>mapCodec(instance -> instance.group(
            BuiltInRegistries.ITEM.byNameCodec().fieldOf("mask").forGetter(Definition::maskItem),
            ShapedRecipePattern.Data.MAP_CODEC.forGetter(Definition::patternData),
            ItemStack.STRICT_CODEC.fieldOf("result").forGetter(Definition::result)
        ).apply(instance, Definition::new)).flatXmap(Definition::build, recipe -> DataResult.success(Definition.of(recipe)));

        public static final StreamCodec<RegistryFriendlyByteBuf, ManualCircuitBoardRecipe> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.registry(Registries.ITEM), Definition::maskItem,
            PATTERN_DATA_STREAM_CODEC, Definition::patternData,
            ItemStack.STREAM_CODEC, Definition::result,
            Definition::new
        ).map(definition -> definition.build().getOrThrow(), Definition::of);

        @Override
        public MapCodec<ManualCircuitBoardRecipe> codec() {
            return CODEC;
        };

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, ManualCircuitBoardRecipe> streamCodec() {
            return STREAM_CODEC;
        };

        private static record Definition(Item maskItem, ShapedRecipePattern.Data patternData, ItemStack result) {

            static Definition of(ManualCircuitBoardRecipe recipe) {
                return new Definition(recipe.maskItem, recipe.patternData, recipe.getShapedRecipe().getResultItem(null));
            };

            DataResult<ManualCircuitBoardRecipe> build() {
                return create(maskItem, patternData, result);
            };
        };
    };

};
