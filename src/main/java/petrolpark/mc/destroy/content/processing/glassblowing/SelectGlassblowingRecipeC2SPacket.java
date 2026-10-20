package petrolpark.mc.destroy.content.processing.glassblowing;

import petrolpark.mc.destroy.DestroyPackets;
import petrolpark.mc.destroy.util.ItemStackNBT;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import com.simibubi.create.foundation.recipe.RecipeFinder;

import net.createmod.catnip.net.base.ServerboundPacketPayload;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

public class SelectGlassblowingRecipeC2SPacket implements ServerboundPacketPayload {

    public static final StreamCodec<RegistryFriendlyByteBuf, SelectGlassblowingRecipeC2SPacket> STREAM_CODEC = StreamCodec.ofMember(SelectGlassblowingRecipeC2SPacket::toBytes, SelectGlassblowingRecipeC2SPacket::new);

    private static final Object recipeCacheKey = new Object();

    private final InteractionHand hand;
    private final ResourceLocation recipeId;

    public SelectGlassblowingRecipeC2SPacket(InteractionHand hand, ResourceLocation recipeId) {
        this.hand = hand;
        this.recipeId = recipeId;
    };

    public SelectGlassblowingRecipeC2SPacket(RegistryFriendlyByteBuf buffer) {
        hand = buffer.readBoolean() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
        recipeId = buffer.readResourceLocation();
    };

    public void toBytes(RegistryFriendlyByteBuf buffer) {
        buffer.writeBoolean(hand == InteractionHand.MAIN_HAND);
        buffer.writeResourceLocation(recipeId);
    };

    @Override
    public void handle(ServerPlayer sender) {
        ItemStack stack = sender.getItemInHand(hand);
        CompoundTag tag = ItemStackNBT.read(stack);
        GlassblowingRecipe recipe = RecipeFinder.get(recipeCacheKey, sender.level(), r -> r.value() instanceof GlassblowingRecipe).stream().filter(r -> r.id().equals(recipeId)).findFirst().map(r -> (GlassblowingRecipe)r.value()).orElse(null);
        if (recipe == null) return;
        if (!tag.contains("Recipe")) { // If no recipe was set to begin with

        } else { // If there is an existing Recipe being replaced
            ResourceLocation existingRecipe = ResourceLocation.parse(tag.getString("Recipe"));
            if (existingRecipe.equals(recipeId)) return;
            FluidTank tank = new FluidTank(BlowpipeBlockEntity.TANK_CAPACITY);
            tank.readFromNBT(sender.registryAccess(), tag.getCompound("Tank"));
            if (!tank.isEmpty()) { // If there is Fluid, it may need to be voided if it doesn't match the new Recipe
                SizedFluidIngredient ingredient = recipe.getFluidIngredients().get(0);
                if (!ingredient.test(tank.getFluid()) || tank.getFluid().getAmount() < ingredient.amount()) {
                    tag.put("Tank", new FluidTank(BlowpipeBlockEntity.TANK_CAPACITY).writeToNBT(sender.registryAccess(), new CompoundTag())); // Drain the tank
                };
            };
            tag.putInt("Progress", 0);
            tag.putInt("LastProgress", 0);
        };
        tag.putString("Recipe", recipeId.toString());
        BlowpipeItem.putFluidIngredient(tag, recipe.getFluidIngredients().get(0));
        ItemStackNBT.set(stack, tag); // 1.20.1 mutated the stack's own tag
    };

    @Override
    public PacketTypeProvider getTypeProvider() {
        return DestroyPackets.SELECT_GLASSBLOWING_RECIPE;
    };
    
};
