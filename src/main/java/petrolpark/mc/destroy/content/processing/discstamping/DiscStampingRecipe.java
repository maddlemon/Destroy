package petrolpark.mc.destroy.content.processing.discstamping;

import com.simibubi.create.content.kinetics.deployer.ItemApplicationRecipe;
import java.util.Optional;

import javax.annotation.Nullable;

import petrolpark.mc.destroy.Destroy;
import petrolpark.mc.destroy.DestroyItems;
import com.simibubi.create.content.kinetics.deployer.DeployerApplicationRecipe;
import com.simibubi.create.content.kinetics.deployer.DeployerRecipeSearchEvent;
import com.simibubi.create.content.kinetics.deployer.ItemApplicationRecipeParams;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.common.crafting.DataComponentIngredient;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.items.wrapper.RecipeWrapper;

@EventBusSubscriber(modid = Destroy.MOD_ID)
public class DiscStampingRecipe extends DeployerApplicationRecipe {

    public DiscStampingRecipe(ItemApplicationRecipeParams params) {
        super(params);
    };

    @Nullable
    public static RecipeHolder<DeployerApplicationRecipe> create(ItemStack discStamper) {
        if (!(discStamper.getItem() instanceof DiscStamperItem)) return null;
        ItemStack disc = DiscStamperItem.getDisc(discStamper);
        if (disc.isEmpty()) return null;
        ResourceLocation id = Destroy.asResource("disc_stamping_"+Item.getId(disc.getItem()));
        DeployerApplicationRecipe recipe = new ItemApplicationRecipe.Builder<>(DiscStampingRecipe::new, id)
            .require(Ingredient.of(DestroyItems.BLANK_MUSIC_DISC))
            .require(DataComponentIngredient.of(true, discStamper)) // Was StrictNBTIngredient
            .output(disc)
            .toolNotConsumed()
            .build();
        return new RecipeHolder<>(id, recipe);
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
