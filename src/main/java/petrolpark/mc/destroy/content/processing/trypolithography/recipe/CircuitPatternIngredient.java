package petrolpark.mc.destroy.content.processing.trypolithography.recipe;

import java.util.Objects;
import java.util.stream.Stream;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import petrolpark.mc.destroy.Destroy;
import petrolpark.mc.destroy.DestroyIngredientTypes;
import petrolpark.mc.destroy.content.processing.trypolithography.CircuitPatternItem;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;

/**
 * An Item with a specific circuit pattern. JSON: {@code {"type": "destroy:circuit_pattern_item", "item": ..., "pattern": ...}}
 * (same as 1.20.1). Use as a vanilla Ingredient with {@link #toVanilla()}.
 */
public class CircuitPatternIngredient implements ICustomIngredient {

    public static final MapCodec<CircuitPatternIngredient> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        BuiltInRegistries.ITEM.byNameCodec().validate(item -> item instanceof CircuitPatternItem ? DataResult.success(item) : DataResult.error(() -> "Circuit pattern item ingredients must be able to have circuit patterns")).fieldOf("item").forGetter(i -> i.item),
        ResourceLocation.CODEC.fieldOf("pattern").forGetter(i -> i.patternRL)
    ).apply(instance, CircuitPatternIngredient::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, CircuitPatternIngredient> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.registry(net.minecraft.core.registries.Registries.ITEM), i -> i.item,
        ResourceLocation.STREAM_CODEC, i -> i.patternRL,
        CircuitPatternIngredient::new
    );

    protected final CircuitPatternItem item;
    protected final ResourceLocation patternRL;

    public CircuitPatternIngredient(Item item, ResourceLocation patternRL) {
        if (!(item instanceof CircuitPatternItem patternItem)) throw new IllegalArgumentException("Circuit pattern item ingredients must be able to have circuit patterns");
        this.item = patternItem;
        this.patternRL = patternRL;
    };

    @Override
    public boolean test(ItemStack stack) {
        if (!stack.getItem().equals(item)) return false;
        Integer pattern = Destroy.CIRCUIT_PATTERN_HANDLER.getPattern(patternRL);
        return (pattern != null && pattern == CircuitPatternItem.getPattern(stack));
    };

    @Override
    public Stream<ItemStack> getItems() {
        Integer pattern = Destroy.CIRCUIT_PATTERN_HANDLER.getPattern(patternRL);
        if (pattern == null) return Stream.empty();
        ItemStack stack = new ItemStack(item);
        CircuitPatternItem.putPattern(stack, pattern);
        return Stream.of(stack);
    };

    @Override
    public boolean isSimple() {
        return false;
    };

    @Override
    public IngredientType<?> getType() {
        return DestroyIngredientTypes.CIRCUIT_PATTERN_ITEM.get();
    };

    @Override
    public boolean equals(Object obj) {
        return obj instanceof CircuitPatternIngredient other && other.item == item && other.patternRL.equals(patternRL);
    };

    @Override
    public int hashCode() {
        return Objects.hash(item, patternRL);
    };

};
