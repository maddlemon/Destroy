package petrolpark.mc.destroy.core.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;

/**
 * 1.20.1 Create's {@code CombustibleItem}, which Create 6 no longer has (the Item version of {@link CombustibleBlockItem}).
 */
public class CombustibleItem extends Item {

    private int burnTime = -1;

    public CombustibleItem(Properties properties) {
        super(properties);
    };

    public void setBurnTime(int burnTime) {
		this.burnTime = burnTime;
	};

	@Override
	public int getBurnTime(ItemStack itemStack, RecipeType<?> recipeType) {
		return this.burnTime;
	};
    
};
