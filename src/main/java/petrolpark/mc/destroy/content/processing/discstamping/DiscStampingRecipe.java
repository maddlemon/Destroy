package petrolpark.mc.destroy.content.processing.discstamping;

import java.util.Optional;

import javax.annotation.Nullable;

import petrolpark.mc.destroy.Destroy;
import petrolpark.mc.destroy.DestroyItems;
import com.simibubi.create.content.kinetics.deployer.DeployerApplicationRecipe;
import com.simibubi.create.content.kinetics.deployer.DeployerRecipeSearchEvent;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeBuilder;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeParams;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.crafting.StrictNBTIngredient;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.items.wrapper.RecipeWrapper;

@EventBusSubscriber(modid = Destroy.MOD_ID)
public class DiscStampingRecipe extends DeployerApplicationRecipe {

    public DiscStampingRecipe(ProcessingRecipeParams params) {
        super(params);
    };

    @Nullable
    public static DeployerApplicationRecipe create(ItemStack discStamper) {
        if (!(discStamper.getItem() instanceof DiscStamperItem)) return null;
        ItemStack disc = DiscStamperItem.getDisc(discStamper);
        if (disc.isEmpty()) return null;
        return new ProcessingRecipeBuilder<>(DiscStampingRecipe::new, Destroy.asResource("disc_stamping_"+Item.getId(disc.getItem())))
            .require(Ingredient.of(DestroyItems.BLANK_MUSIC_DISC))
            .require(StrictNBTIngredient.of(discStamper))
            .output(disc)
            .toolNotConsumed()
            .build();
    };

    @Override
    public boolean supportsAssembly() {
        return false;
    };

    @SubscribeEvent
    public static void onDeployerRecipeSearch(DeployerRecipeSearchEvent event) {
        RecipeWrapper inv = event.getInventory();
        ItemStack appliedStack = inv.getItem(1);
        if (appliedStack.getItem() instanceof DiscStamperItem && inv.getItem(0).is(DestroyItems.BLANK_MUSIC_DISC.get())) event.addRecipe(() -> Optional.ofNullable(DiscStampingRecipe.create(appliedStack)), 75);
    };
    
};
