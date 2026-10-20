package petrolpark.mc.destroy.content.processing.glassblowing;

import petrolpark.mc.destroy.util.ItemStackNBT;
import petrolpark.mc.destroy.DestroyCapabilities;
import java.util.List;
import java.util.function.Consumer;

import com.mojang.serialization.JsonOps;

import net.createmod.catnip.data.Iterate;
import net.createmod.catnip.gui.ScreenOpener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import petrolpark.mc.destroy.DestroyAdvancementTrigger;
import petrolpark.mc.destroy.util.DestroyLang;
import petrolpark.mc.destroy.core.block.IPickUpPutDownBlock;
import petrolpark.mc.destroy.core.chemistry.hazard.ChemistryHazardHelper;
import com.simibubi.create.content.kinetics.deployer.DeployerFakePlayer;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import com.simibubi.create.foundation.item.CustomArmPoseItem;
import com.simibubi.create.foundation.item.render.SimpleCustomRenderer;

import net.minecraft.client.model.HumanoidModel.ArmPose;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import petrolpark.mc.destroy.Destroy;

public class BlowpipeItem extends BlockItem implements CustomArmPoseItem {

    public static final int TIME_TO_MOVE_TO_MOUTH = 10;

    public BlowpipeItem(BlowpipeBlock block, Properties properties) {
        super(block, properties.stacksTo(1));
    };

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        CompoundTag tag = ItemStackNBT.read(stack);
        int progress = tag.getInt("Progress");

        GlassblowingRecipe recipe = BlowpipeBlockEntity.readRecipe(level, tag);

        // Choosing a recipe
        if (!(player instanceof FakePlayer) && ((player.isShiftKeyDown() && progress == 0) || recipe == null)) {
            Destroy.unsafeRunClient(() -> () -> openScreen(hand));
            return InteractionResultHolder.success(stack);
        };

        // Default behaviour
        InteractionResultHolder<ItemStack> result = super.use(level, player, hand);
        
        // Continuing blowing
        FluidTank tank = new FluidTank(BlowpipeBlockEntity.TANK_CAPACITY);
        readTank(tank, tag.getCompound("Tank"));
        if (result.getResult() == InteractionResult.PASS && recipe != null && !tank.isEmpty()) { // Only begin if we have Fluid - the only Fluid we can have is the right one for the recipe

            if (progress > BlowpipeBlockEntity.BLOWING_DURATION) { // If done
                return InteractionResultHolder.pass(stack); // Nothing left to do
            } else if (progress / BlowpipeBlockEntity.BLOWING_DURATION > BlowpipeBlockEntity.BLOWING_TIME_PROPORTION) { // If waiting to cool
                return InteractionResultHolder.pass(stack); // Wait
            } else { // If still blowing
                if (ChemistryHazardHelper.Protection.MOUTH_COVERED.isProtected(player)) { // Check if the mouth is covered
                    player.displayClientMessage(DestroyLang.translate("tooltip.eating_prevented.mouth_protected").component(), true);
                } else {
                    player.startUsingItem(hand);
                };
                return InteractionResultHolder.fail(stack);
            }
        };
        return result;
    };

    /**
     * Read the Blowpipe's tank from Item NBT, which is in {@link FluidTank#writeToNBT}'s layout (the Block Entity writes it that way)
     * but has to be read without registry access.
     */
    public static void readTank(FluidTank tank, CompoundTag tankTag) {
        tank.setFluid(ItemStackNBT.readFluid(tankTag, "Fluid"));
    };

    public static CompoundTag writeTank(FluidTank tank) {
        CompoundTag tankTag = new CompoundTag();
        if (!tank.isEmpty()) ItemStackNBT.writeFluid(tankTag, "Fluid", tank.getFluid());
        return tankTag;
    };

    public static SizedFluidIngredient getFluidIngredient(CompoundTag tag) {
        if (!tag.contains("RequiredFluid", Tag.TAG_STRING)) return null;
        return SizedFluidIngredient.FLAT_CODEC.parse(JsonOps.INSTANCE, GsonHelper.parse(tag.getString("RequiredFluid"), true)).result().orElse(null);
    };

    /**
     * Stored as a JSON string like in 1.20.1 (now NeoForge's {@link SizedFluidIngredient#FLAT_CODEC} format rather than Create's {@code FluidIngredient} one).
     * No registry context is needed (or available, in the Item's Fluid handler).
     */
    public static void putFluidIngredient(CompoundTag tag, SizedFluidIngredient ingredient) {
        tag.putString("RequiredFluid", SizedFluidIngredient.FLAT_CODEC.encodeStart(JsonOps.INSTANCE, ingredient).getOrThrow().toString());
    };

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        ItemStack stack = context.getItemInHand();
        CompoundTag tag = ItemStackNBT.read(stack);
        SizedFluidIngredient ingredient = getFluidIngredient(tag);
        if (ingredient != null) {
            BlockPos pos = context.getClickedPos();
            Direction face = context.getClickedFace();
            BlockEntity be = level.getBlockEntity(pos);
            if (be != null) {
                FluidTank blowpipeTank = new FluidTank(BlowpipeBlockEntity.TANK_CAPACITY);
                readTank(blowpipeTank, tag.getCompound("Tank"));
                if (blowpipeTank.isEmpty()) {
                    IFluidHandler fh = DestroyCapabilities.fluidHandler(be, face);
                    if (fh != null) {
                        for (boolean simulate : Iterate.trueAndFalse) {
                            FluidStack drained = fh.drain(ingredient.amount(), simulate ? FluidAction.SIMULATE : FluidAction.EXECUTE);
                            if (!ingredient.test(drained) || drained.getAmount() < ingredient.amount()) return InteractionResult.FAIL;
                            if (!simulate) {
                                blowpipeTank.fill(drained, FluidAction.EXECUTE);
                                tag.put("Tank", writeTank(blowpipeTank));
                                ItemStackNBT.set(stack, tag);
                                return InteractionResult.SUCCESS;
                            };
                        };
                    };
                };
            };
        };
        if (context.getPlayer() instanceof DeployerFakePlayer deployer) {
            if (BlowpipeBlock.getDeployerPlacer(level, deployer) == null) return InteractionResult.FAIL; // If on a contraption, don't place
        };
        return super.useOn(context);
    };

    public boolean finishBlowing(ItemStack stack, Level level, Player player) {
        CompoundTag tag = ItemStackNBT.read(stack);
        if (tag.getInt("Progress") < BlowpipeBlockEntity.BLOWING_DURATION) return false;
        tag.put("Tank", writeTank(new FluidTank(1000))); // Empty the Tank
        tag.putInt("Progress", 0); // Reset progress
        tag.putInt("LastProgress", 0);
        ItemStackNBT.set(stack, tag);
        GlassblowingRecipe recipe = BlowpipeBlockEntity.readRecipe(level, tag);
        List<ItemStack> results = recipe.rollResults(level.random);
        level.playSound(null, player.getOnPos(), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS);
        results.forEach(s -> player.getInventory().placeItemBackInInventory(s)); // Grant recipe results
        return true;
    };

    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
        CompoundTag tag = ItemStackNBT.read(stack);
        FluidTank tank = new FluidTank(1000);
        readTank(tank, tag.getCompound("Tank"));
        if (!tank.isEmpty() && (float)ItemStackNBT.read(stack).getInt("Progress") / (float)BlowpipeBlockEntity.BLOWING_DURATION < BlowpipeBlockEntity.BLOWING_TIME_PROPORTION) {
            entity.igniteForSeconds(3);
        };
        return false;
    };

    @OnlyIn(Dist.CLIENT)
    public void openScreen(InteractionHand hand) {
        ScreenOpener.open(new BlowpipeScreen(hand));
    };

    @Override
    public InteractionResult place(BlockPlaceContext pContext) {
        return IPickUpPutDownBlock.removeItemFromInventory(pContext, super.place(pContext));
    };

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged || newStack.getItem() != oldStack.getItem();
    };

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        CompoundTag tag = ItemStackNBT.read(stack);
        if (BlowpipeBlockEntity.readRecipe(level, tag) == null) {
            tag.putInt("Progress", 0);
            tag.putInt("LastProgress", 0);
            tag.putBoolean("Blowing", false);
            ItemStackNBT.set(stack, tag);
            return;
        };

        int progress = tag.getInt("Progress");
        tag.putInt("LastProgress", progress);

        boolean increaseProgress = (float)progress / (float)BlowpipeBlockEntity.BLOWING_DURATION > BlowpipeBlockEntity.BLOWING_TIME_PROPORTION; // If we're at the cooling stage, progress increases even if not actively blowing

        if (entity instanceof Player player && player.isUsingItem() && isSelected) {
            int ticksUsing = player.getTicksUsingItem();
            if (ticksUsing > TIME_TO_MOVE_TO_MOUTH) {
                increaseProgress = true;
                player.setAirSupply(player.getAirSupply() - 10);
            };
            tag.putBoolean("Blowing", true);
        } else {
            tag.putBoolean("Blowing", false);
        };

        if (increaseProgress && progress < BlowpipeBlockEntity.BLOWING_DURATION) {
            tag.putInt("Progress", progress + 1);
            if(progress < BlowpipeBlockEntity.BLOWING_DURATION && progress + 1 >= BlowpipeBlockEntity.BLOWING_DURATION && entity instanceof Player player) {
                DestroyAdvancementTrigger.BLOWPIPE.award(level, player);
            }
        }
        ItemStackNBT.set(stack, tag);
    };

    @Override
    public @Nullable ArmPose getArmPose(ItemStack stack, AbstractClientPlayer player, InteractionHand hand) {
        if (!player.swinging && ItemStackNBT.read(stack).getBoolean("Blowing")) return ArmPose.SPYGLASS;
        return null;
    };

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return BlowpipeBlockEntity.BLOWING_DURATION + TIME_TO_MOVE_TO_MOUTH - ItemStackNBT.read(stack).getInt("Progress");
    };

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {
        super.finishUsingItem(stack, level, livingEntity);
        CompoundTag tag = ItemStackNBT.read(stack);
        if (tag.getInt("Progress") == BlowpipeBlockEntity.BLOWING_DURATION) {
            tag.putInt("Progress", 0);
            tag.putInt("LastProgress", 0);
            tag.put("Tank", writeTank(new FluidTank(BlowpipeBlockEntity.TANK_CAPACITY))); // Empty the Tank
            ItemStackNBT.set(stack, tag);
        };
        return stack;
    };

    @Override
    @OnlyIn(Dist.CLIENT)
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(SimpleCustomRenderer.create(this, new BlowpipeItemRenderer()));
    };

    /**
     * Fluid Handler capability which only exists to allow Blowpipes to be filled by Spouts.
     */
    public static class BlowpipeSpoutFillingFluidHandler implements IFluidHandlerItem {

        protected final ItemStack stack;

        public BlowpipeSpoutFillingFluidHandler(ItemStack stack) {
            this.stack = stack;
        };
        

        @Override
        public int getTanks() {
            return 1;
        };

        @Override
        public @NotNull FluidStack getFluidInTank(int tankNo) {
            FluidTank tank = new FluidTank(getTankCapacity(0));
            readTank(tank, ItemStackNBT.read(stack).getCompound("Tank"));
            return tank.getFluid();
        };

        @Override
        public int getTankCapacity(int tank) {
            return BlowpipeBlockEntity.TANK_CAPACITY;
        };

        @Override
        public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
            return true;
        };

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            CompoundTag tag = ItemStackNBT.read(stack);
            SizedFluidIngredient ingredient = getFluidIngredient(tag);
            if (ingredient != null && ingredient.test(resource) && resource.getAmount() >= ingredient.amount()) {
                FluidTank tank = new FluidTank(getTankCapacity(0));
                readTank(tank, tag.getCompound("Tank"));
                if (!tank.isEmpty()) return 0;
                if (action.execute()) {
                    tank.fill(resource, FluidAction.EXECUTE);
                    tag.put("Tank", writeTank(tank));
                    ItemStackNBT.set(stack, tag);
                };
                return ingredient.amount();
            };
            return 0;
        };

        @Override
        public @NotNull FluidStack drain(FluidStack resource, FluidAction action) {
            return FluidStack.EMPTY;
        };

        @Override
        public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
            return FluidStack.EMPTY;
        };

        @Override
        public @NotNull ItemStack getContainer() {
            return stack;
        };

    };
    
};
