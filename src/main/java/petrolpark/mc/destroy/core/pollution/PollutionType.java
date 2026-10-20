package petrolpark.mc.destroy.core.pollution;

import java.util.stream.Stream;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.simibubi.create.foundation.gui.AllIcons;

import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import petrolpark.mc.destroy.DestroyRegistries;
import petrolpark.mc.library.util.codec.CodecHelper;

public class PollutionType<P extends IAttachmentHolder> {

    public static final Stream<PollutionType<?>> streamAll() {
        return Stream.concat(DestroyRegistries.LEVEL_POLLUTION_TYPES.stream(), DestroyRegistries.CHUNK_POLLUTION_TYPES.stream());
    };

    public final boolean chunk;
    protected final AllIcons icon;
    protected final String translationKey;
    protected Component name = null;

    public PollutionType(boolean chunk, AllIcons icon, String translationKey) {
        this.chunk = chunk;
        this.icon = icon;
        this.translationKey = translationKey;
    };

    public Component getName() {
        if (name == null) name = Component.translatable(translationKey);
        return name;
    };
    
    /**
     * Data map values (see {@link petrolpark.mc.destroy.DestroyDataMapTypes}). The shipped values reproduce 1.20.1, where these were hardcoded.
     * Decay and spreading rates are not here: as in 1.20.1 they are server configs ({@link petrolpark.mc.destroy.config.DestroyPollutionConfigs}).
     * @param max The maximum level (1.20.1: 65536)
     * @param syncThreshold How much the level must change before it is sent to clients
     * @param lightningStrikeChange Change when lightning strikes (1.20.1: -50 Ozone Depletion)
     * @param treeGrowthChange Change with a 1 in 3 chance when a tree, fungus or mushroom grows (1.20.1: -1 Smog, Greenhouse Gas and Acid Rain)
     * @param breedingFailureChanceMultiplier Chance of breeding failing at the maximum level (1.20.1: 1 for Smog)
     * @param growthFailureChanceMultiplier Chance of crops failing to grow at the maximum level (1.20.1: 1 for Smog, Greenhouse Gas and Acid Rain)
     * @param bonemealFailureChanceMultiplier Chance of Bone Meal failing at the maximum level (1.20.1: as for crop growth)
     * @param villagerPriceMultiplier Villager price increase at the maximum level (1.20.1: 50 for Smog)
     */
    public static final record Properties(
        int max, int syncThreshold,
        int lightningStrikeChange, int treeGrowthChange,
        float breedingFailureChanceMultiplier, float growthFailureChanceMultiplier, float bonemealFailureChanceMultiplier, float villagerPriceMultiplier
    ) {

        public static final PollutionType.Properties DEFAULT = new PollutionType.Properties(
            65536, 1,
            0, 0,
            0f, 0f, 0f, 0f
        );

        public static final Codec<PollutionType.Properties> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            CodecHelper.POS_INT.optionalFieldOf("max", DEFAULT.max()).forGetter(PollutionType.Properties::max),
            CodecHelper.POS_INT.optionalFieldOf("sync_threshold", DEFAULT.syncThreshold()).forGetter(PollutionType.Properties::syncThreshold),
            Codec.INT.optionalFieldOf("lightning_strike_change", DEFAULT.lightningStrikeChange()).forGetter(PollutionType.Properties::lightningStrikeChange),
            Codec.INT.optionalFieldOf("tree_growth_change", DEFAULT.treeGrowthChange()).forGetter(PollutionType.Properties::treeGrowthChange),
            Codec.floatRange(0f, 1f).optionalFieldOf("breeding_failure_chance_multiplier", DEFAULT.breedingFailureChanceMultiplier()).forGetter(PollutionType.Properties::breedingFailureChanceMultiplier),
            Codec.floatRange(0f, 1f).optionalFieldOf("growth_failure_chance_multiplier", DEFAULT.growthFailureChanceMultiplier()).forGetter(PollutionType.Properties::growthFailureChanceMultiplier),
            Codec.floatRange(0f, 1f).optionalFieldOf("bonemeal_failure_chance_multiplier", DEFAULT.bonemealFailureChanceMultiplier()).forGetter(PollutionType.Properties::bonemealFailureChanceMultiplier),
            Codec.FLOAT.optionalFieldOf("villager_price_multiplier", DEFAULT.villagerPriceMultiplier()).forGetter(PollutionType.Properties::villagerPriceMultiplier)
        ).apply(instance, PollutionType.Properties::new));
    };
};
