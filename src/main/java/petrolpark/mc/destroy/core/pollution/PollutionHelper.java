package petrolpark.mc.destroy.core.pollution;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;
import java.util.function.Supplier;

import org.joml.Vector3f;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.createmod.catnip.platform.CatnipServices;
import net.createmod.ponder.api.level.PonderLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.fluids.FluidStack;
import petrolpark.mc.destroy.DestroyAttachmentTypes;
import petrolpark.mc.destroy.DestroyDataMapTypes;
import petrolpark.mc.destroy.DestroyFluids;
import petrolpark.mc.destroy.DestroyPollutionTypes;
import petrolpark.mc.destroy.DestroyRegistries;
import petrolpark.mc.destroy.chemistry.legacy.LegacySpecies;
import petrolpark.mc.destroy.chemistry.legacy.ReadOnlyMixture;
import petrolpark.mc.destroy.chemistry.minecraft.MixtureFluid;
import petrolpark.mc.destroy.config.DestroyConfigs;
import petrolpark.mc.destroy.core.chemistry.hazard.ChemistryHazardHelper;
import petrolpark.mc.destroy.core.fluid.gasparticle.EvaporatingFluidS2CPacket;

public class PollutionHelper {

    /**
     * Ponder Levels' Chunks are not persistent, so (like 1.20.1, which gave Ponder Levels a single Chunk Pollution capability)
     * each Ponder Level has one set of Chunk Pollution values for all positions.
     */
    private static final Map<Level, Object2IntMap<PollutionType<ChunkAccess>>> PONDER_CHUNK_POLLUTION = new WeakHashMap<>();

    private static final int getPonderPollution(Level ponderLevel, PollutionType<ChunkAccess> pollutionType) {
        final Object2IntMap<PollutionType<ChunkAccess>> values = PONDER_CHUNK_POLLUTION.get(ponderLevel);
        return values == null ? 0 : values.getInt(pollutionType);
    };

    private static final int setPonderPollution(Level ponderLevel, PollutionType<ChunkAccess> pollutionType, int value) {
        final int newValue = Mth.clamp(value, 0, getChunkPollutionTypeProperties(pollutionType).max());
        PONDER_CHUNK_POLLUTION.computeIfAbsent(ponderLevel, l -> new Object2IntOpenHashMap<>()).put(pollutionType, newValue);
        return newValue;
    };

    public static final boolean isPollutionEnabled() {
        return DestroyConfigs.server().pollution.enablePollution.get();
    };

    public static final int getPollution(Level level, PollutionType<Level> pollutionType) {
        return level.getData(DestroyAttachmentTypes.LEVEL_POLLUTION).getPollution(pollutionType);
    };

    public static final float getPollutionProportion(Level level, PollutionType<Level> pollutionType) {
        return (float)getPollution(level, pollutionType) / (float)getLevelPollutionTypeProperties(pollutionType).max();
    };
  
    public static final int setPollution(Level level, PollutionType<Level> pollutionType, int value) {
        return level.getData(DestroyAttachmentTypes.LEVEL_POLLUTION).setPollution(pollutionType, value);
    };

    public static final int changePollution(Level level, PollutionType<Level> pollutionType, int change) {
        return level.getData(DestroyAttachmentTypes.LEVEL_POLLUTION).changePollution(pollutionType, change);
    };

    public static final int getPollution(Level level, Supplier<PollutionType<Level>> pollutionType) {
        return level.getData(DestroyAttachmentTypes.LEVEL_POLLUTION).getPollution(pollutionType.get());
    };

    public static final float getPollutionProportion(Level level, Supplier<PollutionType<Level>> pollutionType) {
        return getPollutionProportion(level, pollutionType.get());
    };
  
    public static final int setPollution(Level level, Supplier<PollutionType<Level>> pollutionType, int value) {
        return level.getData(DestroyAttachmentTypes.LEVEL_POLLUTION).setPollution(pollutionType.get(), value);
    };

    public static final int changePollution(Level level, Supplier<PollutionType<Level>> pollutionType, int change) {
        return level.getData(DestroyAttachmentTypes.LEVEL_POLLUTION).changePollution(pollutionType.get(), change);
    };

    public static final int getPollution(ChunkAccess chunk, PollutionType<ChunkAccess> pollutionType) {
        return chunk.getData(DestroyAttachmentTypes.CHUNK_POLLUTION).getPollution(pollutionType);
    };

    public static final float getPollutionProportion(ChunkAccess chunk, PollutionType<ChunkAccess> pollutionType) {
        return (float)getPollution(chunk, pollutionType) / (float)getChunkPollutionTypeProperties(pollutionType).max();
    };
  
    public static final int setPollution(ChunkAccess chunk, PollutionType<ChunkAccess> pollutionType, int value) {
        return chunk.getData(DestroyAttachmentTypes.CHUNK_POLLUTION).setPollution(pollutionType, value);
    };

    public static final int changePollution(ChunkAccess chunk, PollutionType<ChunkAccess> pollutionType, int change) {
        return chunk.getData(DestroyAttachmentTypes.CHUNK_POLLUTION).changePollution(pollutionType, change);
    };

    @SuppressWarnings("unchecked")
    public static final int getPollution(Level level, BlockPos pos, PollutionType<?> pollutionType) {
        if (!pollutionType.chunk) try {
            final PollutionType<Level> levelPollutionType = (PollutionType<Level>)pollutionType;
            return getPollution(level, levelPollutionType);
        } catch (ClassCastException e) {};
        try {
            final PollutionType<ChunkAccess> chunkPollutionType = (PollutionType<ChunkAccess>)pollutionType;
            if (level instanceof PonderLevel) return getPonderPollution(level, chunkPollutionType);
            return getPollution(level.getChunk(pos), chunkPollutionType);
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("PollutionType must be of Level or Chunk");
        }
    };

    @SuppressWarnings("unchecked")
    public static final float getPollutionProportion(Level level, BlockPos pos, PollutionType<?> pollutionType) {
        if (!pollutionType.chunk) try {
            final PollutionType<Level> levelPollutionType = (PollutionType<Level>)pollutionType;
            return getPollutionProportion(level, levelPollutionType);
        } catch (ClassCastException e) {};
        try {
            final PollutionType<ChunkAccess> chunkPollutionType = (PollutionType<ChunkAccess>)pollutionType;
            if (level instanceof PonderLevel) return (float)getPonderPollution(level, chunkPollutionType) / (float)getChunkPollutionTypeProperties(chunkPollutionType).max();
            return getPollutionProportion(level.getChunk(pos), chunkPollutionType);
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("PollutionType must be of Level or Chunk");
        }
        
    };
  
    @SuppressWarnings("unchecked")
    public static final int setPollution(Level level, BlockPos pos, PollutionType<?> pollutionType, int value) {
        if (!pollutionType.chunk) try {
            final PollutionType<Level> levelPollutionType = (PollutionType<Level>)pollutionType;
            return setPollution(level, levelPollutionType, value);
        } catch (ClassCastException e) {};
        try {
            final PollutionType<ChunkAccess> chunkPollutionType = (PollutionType<ChunkAccess>)pollutionType;
            if (level instanceof PonderLevel) return setPonderPollution(level, chunkPollutionType, value);
            return setPollution(level.getChunk(pos), chunkPollutionType, value);
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("PollutionType must be of Level or Chunk");
        }
    };

    @SuppressWarnings("unchecked")
    public static final int changePollution(Level level, BlockPos pos, PollutionType<?> pollutionType, int change) {
        if (!pollutionType.chunk) try {
            final PollutionType<Level> levelPollutionType = (PollutionType<Level>)pollutionType;
            return changePollution(level, levelPollutionType, change);
        } catch (ClassCastException e) {};
        try {
            final PollutionType<ChunkAccess> chunkPollutionType = (PollutionType<ChunkAccess>)pollutionType;
            if (level instanceof PonderLevel) return setPonderPollution(level, chunkPollutionType, getPonderPollution(level, chunkPollutionType) + change);
            return changePollution(level.getChunk(pos), chunkPollutionType, change);
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("PollutionType must be of Level or Chunk");
        }
    };

    public static final int getPollution(Level level, BlockPos pos, Supplier<PollutionType<ChunkAccess>> pollutionType) {
        return getPollution(level, pos, pollutionType.get());
    };

    public static final float getPollutionProportion(Level level, BlockPos pos, Supplier<PollutionType<ChunkAccess>> pollutionType) {
        return getPollutionProportion(level, pos, pollutionType.get());
    };
  
    public static final int setPollution(Level level, BlockPos pos, Supplier<PollutionType<ChunkAccess>> pollutionType, int value) {
        return setPollution(level, pos, pollutionType, value);
    };

    public static final int changePollution(Level level, BlockPos pos, Supplier<PollutionType<ChunkAccess>> pollutionType, int change) {
        return changePollution(level, pos, pollutionType.get(), change);
    };

    public static final PollutionType.Properties getLevelPollutionTypeProperties(PollutionType<Level> pollutionType) {
        return getLevelPollutionTypeProperties(DestroyRegistries.LEVEL_POLLUTION_TYPES.wrapAsHolder(pollutionType));  
    };

    public static final PollutionType.Properties getLevelPollutionTypeProperties(Holder<PollutionType<Level>> pollutionTypeHolder) {
        return Optional.ofNullable(pollutionTypeHolder.getData(DestroyDataMapTypes.LEVEL_POLLUTION_PROPERTIES)).orElse(PollutionType.Properties.DEFAULT);
    };

    public static final PollutionType.Properties getChunkPollutionTypeProperties(PollutionType<ChunkAccess> pollutionType) {
        return getChunkPollutionTypeProperties(DestroyRegistries.CHUNK_POLLUTION_TYPES.wrapAsHolder(pollutionType));  
    };

    public static final PollutionType.Properties getChunkPollutionTypeProperties(Holder<PollutionType<ChunkAccess>> pollutionTypeHolder) {
        return Optional.ofNullable(pollutionTypeHolder.getData(DestroyDataMapTypes.CHUNK_POLLUTION_PROPERTIES)).orElse(PollutionType.Properties.DEFAULT);
    };

    @SuppressWarnings("unchecked")
    public static final PollutionType.Properties getPollutionTypeProperties(PollutionType<?> pollutionType) {
        if (!pollutionType.chunk) try {
            final PollutionType<Level> levelPollutionType = (PollutionType<Level>)pollutionType;
            return getLevelPollutionTypeProperties(levelPollutionType);
        } catch (ClassCastException e) {}
        try {
            final PollutionType<ChunkAccess> chunkPollutionType = (PollutionType<ChunkAccess>)pollutionType;
            return getChunkPollutionTypeProperties(chunkPollutionType);
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("PollutionType must be of Level or Chunk");
        }
    };

    /**
     * The chance roll 1.20.1 used for every pollution effect: succeeds with probability ({@code pollution + 1}) / {@code max}, so a
     * {@code multiplier} of 1 reproduces it exactly. Pollution Types whose multiplier is 0 are not rolled at all (1.20.1 did not check them).
     * @param multiplier The chance of success at the maximum level, from the data map
     */
    public static final boolean rollPollutionEffect(RandomSource random, Level level, BlockPos pos, PollutionType<?> pollutionType, float multiplier) {
        if (multiplier <= 0f) return false;
        return random.nextInt(getPollutionTypeProperties(pollutionType).max()) <= getPollution(level, pos, pollutionType) * multiplier;
    };

    /**
     * Pollute a single Fluid Stack, with no Particles, and without damaging nearby Entities. Ported from 1.20.1.
     * @param level
     * @param fluidStack
     * @see PollutionHelper#pollute(Level, BlockPos, float, int, FluidStack...) Harming Entities and showing evaporation particles too
     */
    public static void pollute(Level level, BlockPos pos, float multiplier, FluidStack fluidStack) {
        if (DestroyFluids.isMixture(fluidStack)) {
            polluteMixture(level, pos, multiplier, fluidStack.getAmount(), MixtureFluid.getMixtureTag(fluidStack));
        } else {
            for (DestroyPollutionTypes.Source source : DestroyPollutionTypes.SOURCES.get()) {
                if (fluidStack.getFluid().is(source.fluidTag())) {
                    float pollutionAmount = multiplier * (float)fluidStack.getAmount() / 250f;
                    if (pollutionAmount < 1f) {
                        if (level.random.nextFloat() <= pollutionAmount) changePollution(level, pos, source.pollutionType().get(), 1);
                    } else {
                        changePollution(level, pos, source.pollutionType().get(), (int)pollutionAmount);
                    };
                };
            };
        };
    };

    /**
     * @param mixtureTag The serialized Mixture ({@link MixtureFluid#getMixtureTag}); 1.20.1 took the Fluid Stack's whole tag instead
     */
    public static void polluteMixture(Level level, BlockPos pos, float multiplier, int amount, CompoundTag mixtureTag) {
        ReadOnlyMixture mixture = ReadOnlyMixture.readNBT(ReadOnlyMixture::new, mixtureTag);
        for (LegacySpecies molecule : mixture.getContents(true)) {
            float pollutionAmount = multiplier * mixture.getConcentrationOf(molecule) * amount / 1000; // One mole of polluting Molecule = one point of Pollution
            for (DestroyPollutionTypes.Source source : DestroyPollutionTypes.SOURCES.get()) {
                if (molecule.hasTag(source.moleculeTag())) {
                    if (pollutionAmount < 1) {
                        if (level.random.nextFloat() <= pollutionAmount) changePollution(level, pos, source.pollutionType().get(), 1);
                    } else {
                        changePollution(level, pos, source.pollutionType().get(), (int)pollutionAmount);
                    };
                };
            };
        };
    };

    /**
     * Release the given Fluids into the environment, sometimes summon evaporation particles, and expose nearby entities to the effects of the chemicals.
     * @param level The level in which the pollution is taking place
     * @param blockPos The position from which the evaporation Particles should originate
     * @param particleWeight Meant to give a {@code 1} in {@code particleWeight} chance of a Particle being shown, but (as in 1.20.1) unused
     * @param fluidStacks The Fluids with which to pollute
     */
    public static void pollute(Level level, BlockPos blockPos, float multiplier, int particleWeight, FluidStack ...fluidStacks) {
        if (level.isClientSide()) return;
        List<LivingEntity> nearbyEntities = level.getEntities(null, new AABB(blockPos).inflate(2)).stream().filter(e -> e instanceof LivingEntity).map(e -> (LivingEntity)e).toList();
        for (FluidStack fluidStack : List.of(fluidStacks)) {
            pollute(level, blockPos, multiplier, fluidStack);
            CatnipServices.NETWORK.sendToAllClients(new EvaporatingFluidS2CPacket(blockPos, fluidStack)); // 1.20.1 meant to send this with a 1 in particleWeight chance, but a stray semicolon made it unconditional
            for (LivingEntity entity : nearbyEntities) {
                ChemistryHazardHelper.damage(level, entity, fluidStack, true);
            };
        };
    };

    public static void pollute(Level level, BlockPos pos, FluidStack ...fluidStacks) {
        pollute(level, pos, 1f, 1, fluidStacks);
    };

    /**
     * Release the given Fluids into the environment and summon evaporation particles.
     * @param level The Level in which the pollution is taking place
     * @param blockPos The position from which the evaporation Particles should originate
     * @param fluidStacks The Fluids with which to pollute
     */
    public static void pollute(Level level, BlockPos blockPos, float multiplier, FluidStack ...fluidStacks) {
        pollute(level, blockPos, multiplier, 1, fluidStacks);
    };

    public static final DustParticleOptions getCropGrowthFailureParticles() {
        return new DustParticleOptions(new Vector3f(109 / 256f, 77 / 256f, 14 / 256f), 1f);
    };


};
