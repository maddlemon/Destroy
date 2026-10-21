package destroytest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

@JeiPlugin
public class JeiHook implements IModPlugin {

    static IJeiRuntime runtime;

    @Override
    public ResourceLocation getPluginUid() {
        return Compat.rl("destroytest:hook");
    };

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        runtime = jeiRuntime;
    };

    static void showItem(Item item, boolean output) {
        runtime.getRecipesGui().show(runtime.getJeiHelpers().getFocusFactory().createFocus(output ? RecipeIngredientRole.OUTPUT : RecipeIngredientRole.INPUT, VanillaTypes.ITEM_STACK, new ItemStack(item)));
    };

    static void showCategory(String uid) {
        RecipeType<?> type = runtime.getRecipeManager().getRecipeType(Compat.rl(uid)).orElseThrow(() -> new IllegalArgumentException("no category " + uid));
        runtime.getRecipesGui().showTypes(List.of(type));
    };

    private static List<IRecipeCategory<?>> categories() {
        return runtime.getRecipeManager().createRecipeCategoryLookup().includeHidden().get().collect(Collectors.toList());
    };

    /** uid, title, recipe count, hidden count, catalysts */
    static void dumpCategories(List<String> rows) {
        IRecipeManager rm = runtime.getRecipeManager();
        for (IRecipeCategory<?> c : categories()) {
            RecipeType<?> t = c.getRecipeType();
            long visible = rm.createRecipeLookup(t).get().count();
            long all = rm.createRecipeLookup(t).includeHidden().get().count();
            String catalysts = rm.createRecipeCatalystLookup(t).get().map(i -> describe(i)).collect(Collectors.joining(","));
            rows.add(t.getUid() + "\t" + Dumps.clean(c.getTitle().getString()) + "\t" + visible + "\t" + all + "\t" + c.getWidth() + "x" + c.getHeight() + "\t" + catalysts);
        };
    };

    /** Ingredient types and their counts, then every non-item/fluid ingredient (e.g. molecules) */
    static void dumpIngredients(List<String> rows) {
        IIngredientManager im = runtime.getIngredientManager();
        for (IIngredientType<?> type : im.getRegisteredIngredientTypes()) {
            Collection<?> all = im.getAllIngredients(type);
            rows.add("TYPE\t" + type.getUid() + "\t" + all.size());
            if (type == VanillaTypes.ITEM_STACK) continue;
            for (Object o : all) rows.add(type.getUid() + "\t" + describeRaw(type, o));
        };
    };

    /** Every recipe in every category containing a Destroy ingredient or belonging to Destroy, as its slot contents */
    static void dumpRecipes(List<String> rows) {
        IRecipeManager rm = runtime.getRecipeManager();
        IFocusGroup empty = runtime.getJeiHelpers().getFocusFactory().getEmptyFocusGroup();
        for (IRecipeCategory<?> c : categories()) dumpCategory(rm, c, empty, rows);
    };

    private static <T> void dumpCategory(IRecipeManager rm, IRecipeCategory<T> c, IFocusGroup empty, List<String> rows) {
        boolean destroyCategory = c.getRecipeType().getUid().getNamespace().equals("destroy");
        List<T> recipes = rm.createRecipeLookup(c.getRecipeType()).get().collect(Collectors.toList());
        for (T recipe : recipes) {
            IRecipeLayoutDrawable<T> layout;
            try {
                layout = rm.createRecipeLayoutDrawable(c, recipe, empty).orElse(null);
            } catch (Throwable e) {
                rows.add(c.getRecipeType().getUid() + "\t" + c.getRegistryName(recipe) + "\tLAYOUT_ERROR " + e);
                continue;
            };
            if (layout == null) { rows.add(c.getRecipeType().getUid() + "\t" + c.getRegistryName(recipe) + "\tNO_LAYOUT"); continue; };
            List<String> slots = new ArrayList<>();
            boolean hasDestroy = destroyCategory;
            for (IRecipeSlotView slot : layout.getRecipeSlotsView().getSlotViews()) {
                List<String> ings = slot.getAllIngredients().map(JeiHook::describe).collect(Collectors.toList());
                if (ings.stream().anyMatch(s -> s.contains("destroy:"))) hasDestroy = true;
                String shown = ings.size() > 4 ? String.join(",", ings.subList(0, 4)) + ",+" + (ings.size() - 4) : String.join(",", ings);
                slots.add(slot.getRole().name().charAt(0) + "[" + shown + "]");
            };
            if (!hasDestroy) continue;
            rows.add(c.getRecipeType().getUid() + "\t" + c.getRegistryName(recipe) + "\t" + String.join(" ", slots));
        };
    };

    static String describe(ITypedIngredient<?> i) {
        return describeRaw(i.getType(), i.getIngredient());
    };

    @SuppressWarnings("unchecked")
    static <V> String describeRaw(IIngredientType<V> type, Object o) {
        IIngredientHelper<V> h = runtime.getIngredientManager().getIngredientHelper(type);
        V v = (V) o;
        String rl;
        try { rl = String.valueOf(h.getResourceLocation(v)); } catch (Throwable t) { rl = "?"; };
        return rl + "(" + Dumps.clean(h.getDisplayName(v)) + ")" + Compat.amount(o);
    };
}
