package petrolpark.mc.destroy.util;

import java.util.Arrays;
import java.util.stream.Stream;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

public final class DestroyIngredientHelper {

    private DestroyIngredientHelper() {};

    /**
     * The Items an Ingredient matches, for validating recipes while they are being parsed. In 1.21.1 recipes are parsed before tags are
     * bound and {@link Ingredient#getItems()} caches its result forever, so calling it here would leave tag Ingredients permanently empty.
     * This reads the values without caching; as in 1.20.1 at parse time, tag values are unbound (giving the "Empty Tag" barrier).
     */
    public static Stream<ItemStack> parseTimeItems(Ingredient ingredient) {
        if (ingredient.isCustom()) return ingredient.getCustomIngredient().getItems();
        return Arrays.stream(ingredient.getValues()).flatMap(value -> value.getItems().stream());
    };
};
