package petrolpark.mc.destroy.core.pollution;

import java.util.Collections;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

import javax.annotation.ParametersAreNonnullByDefault;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.providers.number.LootNumberProviderType;
import petrolpark.mc.destroy.Destroy;
import petrolpark.mc.destroy.DestroyNumberProviderTypes;
import petrolpark.mc.destroy.DestroyRegistries;
import petrolpark.mc.library.core.data.numberProvider.IEstimableNumberProvider;
import petrolpark.mc.library.core.data.numberProvider.NumberEstimate;

@ParametersAreNonnullByDefault
/**
 * The level of a type of Pollution. JSON (as in 1.20.1, which only had {@code pollution_type}, {@code scale} and {@code invert}):
 * <pre>
 * {
 *      "type": "destroy:pollution",
 *      "pollution_type": "destroy:smog", // 1.20.1's enum names (e.g. "SMOG") also work
 *      "proportion": true, // Proportion of the maximum (the only option in 1.20.1), or the raw level
 *      "scale": 10.0,
 *      "invert": false
 * }
 * </pre>
 */
public record PollutionNumberProvider(Either<Holder<PollutionType<Level>>, Holder<PollutionType<ChunkAccess>>> pollutionType, boolean proportion, float scale, boolean invert) implements IEstimableNumberProvider {

    private static final Codec<Either<Holder<PollutionType<Level>>, Holder<PollutionType<ChunkAccess>>>> POLLUTION_TYPE_CODEC = Codec.STRING.comapFlatMap(PollutionNumberProvider::parsePollutionType, PollutionNumberProvider::pollutionTypeName);

    public static final MapCodec<PollutionNumberProvider> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        POLLUTION_TYPE_CODEC.fieldOf("pollution_type").forGetter(PollutionNumberProvider::pollutionType),
        Codec.BOOL.optionalFieldOf("proportion", true).forGetter(PollutionNumberProvider::proportion),
        Codec.FLOAT.optionalFieldOf("scale", 1f).forGetter(PollutionNumberProvider::scale),
        Codec.BOOL.optionalFieldOf("invert", false).forGetter(PollutionNumberProvider::invert)
    ).apply(instance, PollutionNumberProvider::new));

    private static DataResult<Either<Holder<PollutionType<Level>>, Holder<PollutionType<ChunkAccess>>>> parsePollutionType(String name) {
        final ResourceLocation id = name.matches("[A-Z_]+") ? Destroy.asResource(name.toLowerCase(Locale.ROOT)) : ResourceLocation.tryParse(name);
        if (id == null) return DataResult.error(() -> "Invalid Pollution Type " + name);
        final Optional<Holder.Reference<PollutionType<Level>>> levelType = DestroyRegistries.LEVEL_POLLUTION_TYPES.getHolder(id);
        if (levelType.isPresent()) return DataResult.success(Either.left(levelType.get()));
        final Optional<Holder.Reference<PollutionType<ChunkAccess>>> chunkType = DestroyRegistries.CHUNK_POLLUTION_TYPES.getHolder(id);
        if (chunkType.isPresent()) return DataResult.success(Either.right(chunkType.get()));
        return DataResult.error(() -> "Unknown Pollution Type " + name);
    };

    private static String pollutionTypeName(Either<Holder<PollutionType<Level>>, Holder<PollutionType<ChunkAccess>>> pollutionType) {
        return pollutionType.map(h -> h.unwrapKey().orElseThrow().location().toString(), h -> h.unwrapKey().orElseThrow().location().toString());
    };

    @Override
    public float getFloat(LootContext lootContext) {
        final BlockPos pos = lootContext.hasParam(LootContextParams.ORIGIN) ? BlockPos.containing(lootContext.getParam(LootContextParams.ORIGIN)) : BlockPos.ZERO; // As 1.20.1
        final int level = pollutionType().map(
            h -> PollutionHelper.getPollution(lootContext.getLevel(), h.value()),
            h -> PollutionHelper.getPollution(lootContext.getLevel(), pos, h.value())
        );
        float value = proportion() ? (float)level / (float)getMax() : level;
        if (invert()) value = (proportion() ? 1f : getMax()) - value;
        return value * scale();
    };

    @Override
    public LootNumberProviderType getType() {
        return DestroyNumberProviderTypes.POLLUTION.get();
    };

    @Override
    public NumberEstimate getEstimate() {
        return NumberEstimate.ranged(0f, (proportion ? 1f : getMax()) * scale);
    };

    @Override
    public float getMaxFloat(LootContext context) {
        return getFloat(context);
    };

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return pollutionType().map(h -> Collections.emptySet(), h -> Collections.singleton(LootContextParams.ORIGIN));
    };

    private int getMax() {
        return pollutionType().map(h -> PollutionHelper.getLevelPollutionTypeProperties(h).max(), h -> PollutionHelper.getChunkPollutionTypeProperties(h).max());
    };
    
};
