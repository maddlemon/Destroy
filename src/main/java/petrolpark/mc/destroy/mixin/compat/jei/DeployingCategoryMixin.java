package petrolpark.mc.destroy.mixin.compat.jei;

import org.spongepowered.asm.mixin.Mixin;

import petrolpark.mc.destroy.DestroyItems;
import petrolpark.mc.destroy.content.processing.discstamping.DiscStamperItem;
import petrolpark.mc.destroy.content.processing.discstamping.DiscStampingRecipe;
import com.simibubi.create.compat.jei.category.CreateRecipeCategory;
import com.simibubi.create.compat.jei.category.DeployingCategory;
import com.simibubi.create.content.kinetics.deployer.DeployerApplicationRecipe;

import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.Tags;

@Mixin(DeployingCategory.class)
public abstract class DeployingCategoryMixin extends CreateRecipeCategory<DeployerApplicationRecipe> {

    public DeployingCategoryMixin(Info<DeployerApplicationRecipe> info) {
        super(info); // Never called
    };

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        super.registerRecipes(registration);
        registration.addRecipes(getRecipeType(), BuiltInRegistries.ITEM.getTag(Tags.Items.MUSIC_DISCS).stream().flatMap(HolderSet.Named::stream).map(Holder::value).filter(item -> !item.equals(DestroyItems.BLANK_MUSIC_DISC.get())).map(item -> DiscStampingRecipe.create(DiscStamperItem.of(new ItemStack(item)))).toList());
    };
};
