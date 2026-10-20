package petrolpark.mc.destroy.core.item;

import java.util.Objects;
import java.util.function.Supplier;

import net.createmod.catnip.config.ConfigBase.ConfigInt;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;
import petrolpark.mc.library.core.world.item.decay.DecayTime;
import petrolpark.mc.library.core.world.item.decay.ItemDecay;
import petrolpark.mc.library.core.world.item.decay.StackableFlashingDecayingItem;
import petrolpark.mc.library.core.world.item.decay.product.ChangeItemDecayProduct;
import petrolpark.mc.library.registry.PetrolparkDataComponentTypes;

/**
 * 1.20.1's library {@code ConfiguredDecayingItem} on the 1.21.1 library's component-based decay.
 * The Decay Product and Decay Time are default components, set by {@link DecayingItem#setDefaultComponents} once every Item
 * exists, with the config's default lifetime (Server configs are not loaded at startup). Stacks get the configured lifetime
 * whenever they are crafted or ticked in an inventory; 1.20.1 read the config every time it needed the lifetime.
 * The decay tooltip is added to every Item by the library.
 */
public abstract class DecayingItem extends StackableFlashingDecayingItem {

    protected final Supplier<ItemStack> decayProduct;
    protected final Supplier<ConfigInt> lifetime;
    protected final int defaultLifetime;
    protected final String decayTimeTranslationKey;

    public DecayingItem(Properties properties, Supplier<ItemStack> decayProduct, Supplier<ConfigInt> lifetime, int defaultLifetime, String decayTimeTranslationKey) {
        super(properties);
        this.decayProduct = decayProduct;
        this.lifetime = lifetime;
        this.defaultLifetime = defaultLifetime;
        this.decayTimeTranslationKey = decayTimeTranslationKey;
    };

    public static void setDefaultComponents(ModifyDefaultComponentsEvent event) {
        event.getAllItems().forEach(item -> {
            if (item instanceof DecayingItem decayingItem) event.modify(item, builder -> builder
                .set(PetrolparkDataComponentTypes.DECAY_PRODUCT, new ChangeItemDecayProduct(decayingItem.decayProduct.get()))
                .set(PetrolparkDataComponentTypes.DECAY_TIME, new DecayTime(decayingItem.decayTimeTranslationKey, decayingItem.defaultLifetime))
            );
        });
    };

    /**
     * Give the Item Stack the configured lifetime. Server side only, as the Server config might not be loaded on the client.
     * @param stack
     */
    public void refreshLifetime(ItemStack stack) {
        DecayTime decayTime = new DecayTime(decayTimeTranslationKey, lifetime.get().get());
        if (!Objects.equals(stack.get(PetrolparkDataComponentTypes.DECAY_TIME), decayTime)) stack.set(PetrolparkDataComponentTypes.DECAY_TIME, decayTime);
    };

    @Override
    public void onCraftedBy(ItemStack stack, Level level, Player player) {
        if (!level.isClientSide()) refreshLifetime(stack);
        super.onCraftedBy(stack, level, player);
    };

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        if (!level.isClientSide()) refreshLifetime(stack);
    };

    /**
     * Stacking decaying Items averages their remaining times, as in 1.20.1 (the 1.21.1 library no longer does this).
     */
    @Override
    public boolean overrideOtherStackedOnMe(ItemStack stack, ItemStack other, Slot slot, ClickAction action, Player player, SlotAccess access) {
        ItemStack trueStack = ItemDecay.checkDecay(stack);
        ItemStack otherTrueStack = ItemDecay.checkDecay(other);
        if (stack == trueStack && other == otherTrueStack && stack.is(other.getItem()) && areDecayTimesCombineable(stack, other)) {
            Long creationTime = stack.get(PetrolparkDataComponentTypes.DECAY_START_TIME);
            Long otherCreationTime = other.get(PetrolparkDataComponentTypes.DECAY_START_TIME);
            if (creationTime != null && otherCreationTime != null) {
                int transferred = action == ClickAction.PRIMARY ? Math.min(stack.getMaxStackSize() - stack.getCount(), other.getCount()) : 1;
                long totalTime = (stack.getCount() * ItemDecay.getRemainingTime(stack, creationTime)) + (transferred * ItemDecay.getRemainingTime(other, otherCreationTime));

                stack.grow(transferred);
                stack.set(PetrolparkDataComponentTypes.DECAY_START_TIME, ItemDecay.getGameTime() + (totalTime / stack.getCount()) - ItemDecay.getLifetimeOrNone(stack));
                other.shrink(transferred);
                return true;
            };
        };
        return false;
    };

};
