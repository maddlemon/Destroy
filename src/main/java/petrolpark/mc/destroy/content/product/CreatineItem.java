package petrolpark.mc.destroy.content.product;


import javax.annotation.Nonnull;

import petrolpark.mc.destroy.Destroy;
import petrolpark.mc.destroy.DestroyAttributes;
import petrolpark.mc.destroy.config.DestroyAllConfigs;

import net.minecraft.network.protocol.game.ClientboundUpdateAttributesPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class CreatineItem extends Item {

    public static final ResourceLocation EXTRA_INVENTORY_ATTRIBUTE_MODIFIER = Destroy.asResource("creatine_extra_inventory"); // Was a UUID
    public static final ResourceLocation EXTRA_HOTBAR_ATTRIBUTE_MODIFIER = Destroy.asResource("creatine_extra_hotbar");
    
    public CreatineItem(Properties properties) {
        super(properties);
    }

    @Override
    @Nonnull
    public InteractionResultHolder<ItemStack> use(@Nonnull Level level, @Nonnull Player player, @Nonnull InteractionHand hand) {
        if (!player.isCreative() && player.getFoodData().getFoodLevel() > 6f) {
            return InteractionResultHolder.fail(player.getItemInHand(hand));
        }
        return super.use(level, player, hand);
    }

    @Override
    @Nonnull
    public ItemStack finishUsingItem(@Nonnull ItemStack stack, @Nonnull Level level, @Nonnull LivingEntity livingEntity) {
        if (!level.isClientSide) { // Ensure this logic runs only on the server
            if (livingEntity.getAttributes().hasAttribute(DestroyAttributes.EXTRA_INVENTORY_SIZE)) {
                AttributeInstance extraInventory = livingEntity.getAttribute(DestroyAttributes.EXTRA_INVENTORY_SIZE);
                if (extraInventory != null) {
                    extraInventory.removeModifier(EXTRA_INVENTORY_ATTRIBUTE_MODIFIER);
                    extraInventory.addPermanentModifier(new AttributeModifier(EXTRA_INVENTORY_ATTRIBUTE_MODIFIER, DestroyAllConfigs.SERVER.substances.creatineExtraInventorySize.get(), AttributeModifier.Operation.ADD_VALUE));
                }
            }
            if (livingEntity.getAttributes().hasAttribute(DestroyAttributes.EXTRA_HOTBAR_SLOTS)) {
                AttributeInstance extraHotbar = livingEntity.getAttribute(DestroyAttributes.EXTRA_HOTBAR_SLOTS);
                if (extraHotbar != null) {
                    extraHotbar.removeModifier(EXTRA_HOTBAR_ATTRIBUTE_MODIFIER);
                    extraHotbar.addPermanentModifier(new AttributeModifier(EXTRA_HOTBAR_ATTRIBUTE_MODIFIER, DestroyAllConfigs.SERVER.substances.creatineExtraHotbarSlots.get(), AttributeModifier.Operation.ADD_VALUE));
                }
            }

            // Sync the updated attributes with the client
            if (livingEntity instanceof ServerPlayer serverPlayer) {
                serverPlayer.connection.send(new ClientboundUpdateAttributesPacket(serverPlayer.getId(), serverPlayer.getAttributes().getSyncableAttributes()));
            }
        }
        return super.finishUsingItem(stack, level, livingEntity);
    }

}
