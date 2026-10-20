package petrolpark.mc.destroy;

import static petrolpark.mc.destroy.Destroy.REGISTRATE;

import java.util.List;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.google.common.base.Suppliers;
import com.tterrag.registrate.util.entry.RegistryEntry;

import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.material.Fluid;
import petrolpark.mc.destroy.DestroyTags.Fluids;
import petrolpark.mc.destroy.chemistry.legacy.LegacySpeciesTag;
import petrolpark.mc.destroy.chemistry.legacy.index.DestroyMolecules;
import petrolpark.mc.destroy.client.DestroyIcon;
import petrolpark.mc.destroy.core.pollution.PollutionType;

public class DestroyPollutionTypes {

    public static final RegistryEntry<PollutionType<Level>, PollutionType<Level>>

    GREENHOUSE = REGISTRATE.levelPollutionType("greenhouse", DestroyIcon.GREENHOUSE, PollutionType::new),
    OZONE_DEPLETION = REGISTRATE.levelPollutionType("ozone_depletion", DestroyIcon.OZONE_DEPLETION, PollutionType::new),
    ACID_RAIN = REGISTRATE.levelPollutionType("acid_rain", DestroyIcon.ACID_RAIN, PollutionType::new);

    public static final RegistryEntry<PollutionType<ChunkAccess>, PollutionType<ChunkAccess>>

    SMOG = REGISTRATE.chunkPollutionType("smog", DestroyIcon.SMOG, PollutionType::new),
    RADIOACTIVITY = REGISTRATE.chunkPollutionType("radioactivity", DestroyIcon.RADIOACTIVITY, PollutionType::new); // Local (chunk) Pollution, as in 1.20.1

    /**
     * What causes each type of Pollution when released into the world: Fluids in the tag, and Molecules (in Mixtures) with the tag.
     * These were fields of 1.20.1's {@code PollutionType} enum; listed in that enum's order.
     */
    public static final record Source(Supplier<? extends PollutionType<?>> pollutionType, TagKey<Fluid> fluidTag, @Nullable LegacySpeciesTag moleculeTag) {};

    /**
     * Lazy, so the Molecule index is not loaded while registries are being set up.
     */
    public static final Supplier<List<Source>> SOURCES = Suppliers.memoize(() -> List.of(
        new Source(GREENHOUSE, Fluids.GREENHOUSE_GAS.tag, DestroyMolecules.Tags.GREENHOUSE),
        new Source(OZONE_DEPLETION, Fluids.DEPLETES_OZONE.tag, DestroyMolecules.Tags.OZONE_DEPLETER),
        new Source(SMOG, Fluids.AMPLIFIES_SMOG.tag, DestroyMolecules.Tags.SMOG),
        new Source(ACID_RAIN, Fluids.ACIDIFIES_RAIN.tag, DestroyMolecules.Tags.ACID_RAIN),
        new Source(RADIOACTIVITY, Fluids.RADIOACTIVE.tag, null)
    ));

    public static final void register() {};
};
