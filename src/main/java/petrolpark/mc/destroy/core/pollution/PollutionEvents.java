package petrolpark.mc.destroy.core.pollution;

import static petrolpark.mc.destroy.DestroyPollutionTypes.ACID_RAIN;
import static petrolpark.mc.destroy.core.pollution.PollutionHelper.changePollution;
import static petrolpark.mc.destroy.core.pollution.PollutionHelper.getPollution;
import static petrolpark.mc.destroy.core.pollution.PollutionHelper.getPollutionProportion;
import static petrolpark.mc.destroy.core.pollution.PollutionHelper.getPollutionTypeProperties;
import static petrolpark.mc.destroy.core.pollution.PollutionHelper.isPollutionEnabled;
import static petrolpark.mc.destroy.core.pollution.PollutionHelper.rollPollutionEffect;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WeatheringCopper;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.BabyEntitySpawnEvent;
import net.neoforged.neoforge.event.entity.player.BonemealEvent;
import net.neoforged.neoforge.event.level.BlockGrowFeatureEvent;
import net.neoforged.neoforge.event.level.block.CropGrowEvent;
import petrolpark.mc.destroy.DestroyDataMapTypes;
import petrolpark.mc.destroy.DestroyTags;
import petrolpark.mc.destroy.config.DestroyAllConfigs;
import petrolpark.mc.destroy.config.DestroyPollutionConfigs;
import petrolpark.mc.library.core.event.CommonEvents;
import petrolpark.mc.library.core.world.block.HandlePrecipitationEvent;
import petrolpark.mc.library.core.world.entity.npc.VillagerUpdateSpecialPricesEvent;

/**
 * Effects of Pollution on the world.
 */
/**
 * Pollution's effects on the world. The rolls, switches and (via the data maps) values are those of 1.20.1, where these lived in
 * {@code DestroyCommonEvents}, {@code VillagerMixin} and {@code BlockMixin}; the Acid Rain block changes are new in this port.
 */
@EventBusSubscriber
public class PollutionEvents {

    private static DestroyPollutionConfigs config() {
        return DestroyAllConfigs.SERVER.pollution;
    };
    
    /**
     * Lightning regenerates ozone.
     * @param event
     */
    @SubscribeEvent
    public static final void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof LightningBolt && isPollutionEnabled() && config().lightningRegeneratesOzone.get()) PollutionType.streamAll().forEach(pt -> {
            final int change = getPollutionTypeProperties(pt).lightningStrikeChange();
            if (change != 0) changePollution(event.getLevel(), event.getEntity().getOnPos(), pt, change);
        });
    };

    /**
     * Animals may fail to breed (infertility is handled by the Petrolpark Library, which cancels the event first).
     * @param event
     */
    @SubscribeEvent
    public static final void onBabyEntitySpawn(BabyEntitySpawnEvent event) {
        if (isPollutionEnabled() && config().breedingAffected.get()) {
            final Level level = event.getParentA().level();
            if (PollutionType.streamAll().anyMatch(pt -> 
                rollPollutionEffect(event.getParentA().getRandom(), level, event.getParentA().getOnPos(), pt, getPollutionTypeProperties(pt).breedingFailureChanceMultiplier())
            )) CommonEvents.failToBreed(event);
        };
    };

    /**
     * Crops may fail to grow.
     * @param event
     */
    @SubscribeEvent
    public static final void onCropGrowPre(CropGrowEvent.Pre event) {
        if (isPollutionEnabled() && config().growingAffected.get() && event.getLevel() instanceof final Level level && PollutionType.streamAll().anyMatch(pt -> 
            rollPollutionEffect(level.getRandom(), level, event.getPos(), pt, getPollutionTypeProperties(pt).growthFailureChanceMultiplier())
        )) {
            if (event.getLevel() instanceof ServerLevel serverLevel) serverLevel.sendParticles(PollutionHelper.getCropGrowthFailureParticles(), event.getPos().getX() + 0.5d, event.getPos().getY() + level.random.nextDouble() * event.getState().getShape(level, event.getPos()).max(Axis.Y), event.getPos().getZ() + 0.5d, 10, 0.25d, 0.25d, 0.25d, 0.02d);
            event.setResult(CropGrowEvent.Pre.Result.DO_NOT_GROW);
        };
    };

    /**
     * Bone Meal may fail: it is used up but nothing grows, like vanilla Bone Meal failing on a sapling. (1.20.1 only showed the failure
     * particles: it denied the event's result, which Forge ignored for Bone Meal.) Rolled on the server, which does the growing.
     * @param event
     */
    @SubscribeEvent
    public static final void onBonemeal(BonemealEvent event) {
        if (!isPollutionEnabled() || !config().growingAffected.get() || !config().bonemealingAffected.get() || event.getStack().is(DestroyTags.Items.BONEMEAL_BYPASSES_POLLUTION.tag)) return;
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) return;
        if (PollutionType.streamAll().anyMatch(pt -> 
            rollPollutionEffect(serverLevel.getRandom(), serverLevel, event.getPos(), pt, getPollutionTypeProperties(pt).bonemealFailureChanceMultiplier())
        )) {
            serverLevel.sendParticles(PollutionHelper.getCropGrowthFailureParticles(), event.getPos().getX() + 0.5d, event.getPos().getY() + serverLevel.getRandom().nextDouble() * event.getState().getShape(serverLevel, event.getPos()).max(Axis.Y), event.getPos().getZ() + 0.5d, 10, 0.25d, 0.25d, 0.25d, 0.02d);
            event.getStack().shrink(1);
            event.setSuccessful(true); // The Bone Meal was used
            event.setCanceled(true); // But nothing grows
        };
    };

    /**
     * Decrease Pollution when a tree (or fungus, mushroom or azalea, like 1.20.1's {@code SaplingGrowTreeEvent}) is grown:
     * each Pollution Type has a 1 in 3 chance of changing by its {@code tree_growth_change}.
     * @param event
     */
    @SubscribeEvent
    public static final void onBlockGrowFeature(BlockGrowFeatureEvent event) {
        if (event.getLevel() instanceof Level level && isPollutionEnabled() && config().growingTreesDecreasesPollution.get()) {
            PollutionType.streamAll().forEach(pt -> {
                final int change = getPollutionTypeProperties(pt).treeGrowthChange();
                if (change != 0 && level.random.nextInt(3) == 0) changePollution(level, event.getPos(), pt, change);
            });
        };
    };

    /**
     * Pollution increases Villager prices.
     * @param event
     */
    @SubscribeEvent
    public static final void onVillagerUpdateSpecialPrices(VillagerUpdateSpecialPricesEvent event) {
        if (!isPollutionEnabled() || !config().villagersIncreasePrices.get()) return;
        final Level level = event.getVillager().level();
        final BlockPos pos = event.getVillager().getOnPos();
        PollutionType.streamAll().forEach(pt -> {
            final float multiplier = getPollutionTypeProperties(pt).villagerPriceMultiplier();
            if (multiplier == 0f) return;
            final int change = (int)((double)multiplier * (double)getPollution(level, pos, pt) / (double)getPollutionTypeProperties(pt).max()); // In doubles, as 1.20.1
            event.getOffers().forEach(offer -> offer.addToSpecialPriceDiff(change));
        });
    };

    /**
     * Acid Rain can destroy, oxidize or otherwise change blocks.
     * @param event
     */
    @SubscribeEvent
    @SuppressWarnings("deprecation")
    public static final void onHandlePrecipitation(HandlePrecipitationEvent event) {
        if (event.getPrecipitation() != Biome.Precipitation.RAIN || !isPollutionEnabled() || !config().rainBreaksBlocks.get()) return;
        
        final float acidity = getPollutionProportion(event.getLevel(), ACID_RAIN);
        if (event.getLevel().getRandom().nextFloat() < acidity * config().acidRainOxidationChance.getF() && event.getState().getBlock() instanceof WeatheringCopper weatheringCopper) {
            weatheringCopper.getNext(event.getState()).ifPresent(state -> {
                event.getLevel().setBlockAndUpdate(event.getPos(), state);
                event.setCanceled(true);
            });
        };
        if (event.getLevel().getRandom().nextFloat() < acidity * config().acidRainReplacementChance.getF()) {
            final Block replacementBlock = event.getState().getBlock().builtInRegistryHolder().getData(DestroyDataMapTypes.ACID_RAIN_REPLACEMENTS);
            if (replacementBlock != null) {
                event.getLevel().setBlockAndUpdate(event.getPos(), replacementBlock.withPropertiesOf(event.getState()));
                event.setCanceled(true);
            };
        };
    };
};
