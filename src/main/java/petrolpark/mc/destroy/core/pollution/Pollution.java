package petrolpark.mc.destroy.core.pollution;

import java.util.Collections;
import java.util.Map;

import javax.annotation.ParametersAreNonnullByDefault;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.createmod.ponder.api.level.PonderLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;
import petrolpark.mc.destroy.DestroyPollutionTypes;
import petrolpark.mc.destroy.config.DestroyAllConfigs;
import petrolpark.mc.library.util.codec.ContextualCodec;
import petrolpark.mc.library.util.codec.RecordContextualCodecBuilder;

@ParametersAreNonnullByDefault
public abstract class Pollution<HOLDER extends IAttachmentHolder> {

    protected final HOLDER holder;

    protected final Object2IntMap<PollutionType<HOLDER>> values = new Object2IntOpenHashMap<>();
    protected final Object2IntMap<PollutionType<HOLDER>> lastSyncedValues = new Object2IntOpenHashMap<>();
    
    public Pollution(HOLDER holder, Map<PollutionType<HOLDER>, Integer> values) {
        this.holder = holder;

        this.values.defaultReturnValue(0);
        lastSyncedValues.defaultReturnValue(0);

        this.values.putAll(values);
    };

    protected Map<PollutionType<HOLDER>, Integer> getValues() {
        return values;
    };

    protected Object2IntMap<PollutionType<HOLDER>> setValues(Map<PollutionType<HOLDER>, Integer> values) {
        this.values.putAll(values);
        this.lastSyncedValues.putAll(values);
        setChanged();
        return this.values;
    };

    public int changePollution(PollutionType<HOLDER> pollutionType, int change) {
        return setPollution(pollutionType, values.getInt(pollutionType) + change);
    };

    /**
     * @param pollutionType
     * @param change
     * @return Whether to sync to client
     */
    protected boolean changePollutionUnchecked(PollutionType<HOLDER> pollutionType, int change) {
        return setPollutionUnchecked(pollutionType, values.getInt(pollutionType) + change);
    };

    public int setPollution(PollutionType<HOLDER> pollutionType, int value) {
        final int newValue = Mth.clamp(value, 0, getProperties(pollutionType).max());
        if (setPollutionUnchecked(pollutionType, newValue)) sync();
        setChanged();
        return newValue;
    };

    /**
     * @param pollutionType
     * @param newValue
     * @return Whether to sync to client
     */
    protected boolean setPollutionUnchecked(PollutionType<HOLDER> pollutionType, int newValue) {
        final int oldValue = values.getInt(pollutionType);
        values.put(pollutionType, newValue);
        setChanged();
        return Mth.abs(oldValue - newValue) >= getProperties(pollutionType).syncThreshold();
    };

    public int getPollution(PollutionType<HOLDER> pollutionType) {
        return values.getInt(pollutionType);
    };

    /**
     * As in 1.20.1: if Pollution is enabled, each Pollution Type has a (configurable) chance per tick of decreasing by 1.
     */
    public void tick(RandomSource random) {
        if (!PollutionHelper.isPollutionEnabled()) return;
        for (PollutionType<HOLDER> pollutionType : values.keySet()) {
            if (random.nextFloat() <= getDecreaseRate(pollutionType)) changePollution(pollutionType, -1);
        };
    };

    /**
     * @return The chance per tick that the level of this Pollution Type decreases by 1
     */
    public abstract float getDecreaseRate(PollutionType<HOLDER> pollutionType);

    public void setChanged() {};

    public final void sync() {
        syncInternal();
        lastSyncedValues.putAll(values);
    };

    protected abstract void syncInternal();

    public abstract PollutionType.Properties getProperties(PollutionType<HOLDER> pollutionType);

    public static final float BASE_OUTDOOR_TEMPERATURE = 289f;

    /**
     * Get the outdoor ("room") temperature at the given position, accounting for the change in temperature due to pollution
     * and the natural heat of the Biome at that position. Same as 1.20.1, including its integer division: Greenhouse Gas
     * (+20K) and Ozone Depletion (+4K) only raise the temperature when at their maximum.
     * @param level
     * @param pos
     * @return Temperature in kelvins
     */
    public static float getLocalTemperature(Level level, BlockPos pos) {
        if (level instanceof PonderLevel) return BASE_OUTDOOR_TEMPERATURE; // 1.20.1 gave Ponder Levels no Level Pollution
        float outdoorTemperature = BASE_OUTDOOR_TEMPERATURE;
        if (PollutionHelper.isPollutionEnabled() && DestroyAllConfigs.SERVER.pollution.temperatureAffected.get()) {
            outdoorTemperature +=
                (PollutionHelper.getPollution(level, DestroyPollutionTypes.GREENHOUSE) / PollutionHelper.getLevelPollutionTypeProperties(DestroyPollutionTypes.GREENHOUSE.get()).max()) * 20f
              + (PollutionHelper.getPollution(level, DestroyPollutionTypes.OZONE_DEPLETION) / PollutionHelper.getLevelPollutionTypeProperties(DestroyPollutionTypes.OZONE_DEPLETION.get()).max()) * 4f;
        };
        return outdoorTemperature + (10f * level.getBiome(pos).value().getBaseTemperature());
    };

    public static abstract class Serializer<HOLDER extends IAttachmentHolder, POLLUTION extends Pollution<HOLDER>> implements IAttachmentSerializer<Tag, POLLUTION> {

        protected final Registry<PollutionType<HOLDER>> pollutionTypeRegistry;
        protected final ContextualCodec<HOLDER, POLLUTION> codec;

        public Serializer(Registry<PollutionType<HOLDER>> pollutionTypeRegistry) {
            this.pollutionTypeRegistry = pollutionTypeRegistry;
            this.codec = RecordContextualCodecBuilder.create(instance -> instance.group(
                instance.context(),
                ContextualCodec.<HOLDER, Map<PollutionType<HOLDER>, Integer>>of(Codec.unboundedMap(pollutionTypeRegistry.byNameCodec(), Codec.INT)).fieldOf("values").forGetter(Pollution::getValues)
            ).apply(instance, this::create));
        };

        protected abstract POLLUTION create(HOLDER holder, Map<PollutionType<HOLDER>, Integer> values);

        public abstract HOLDER castHolder(IAttachmentHolder holder);

        @Override
        public final POLLUTION read(IAttachmentHolder holder, Tag tag, HolderLookup.Provider provider) {
            final HOLDER trueHolder = castHolder(holder);
            return codec.parse(RegistryOps.create(NbtOps.INSTANCE, provider), trueHolder, tag).resultOrPartial().orElse(create(trueHolder, Collections.emptyMap()));
        };

        @Override
        public final @Nullable Tag write(POLLUTION attachment, HolderLookup.Provider provider) {
            return codec.encodeStart(RegistryOps.create(NbtOps.INSTANCE, provider), attachment.holder, attachment).resultOrPartial().orElse(null);
        };

    };
};
