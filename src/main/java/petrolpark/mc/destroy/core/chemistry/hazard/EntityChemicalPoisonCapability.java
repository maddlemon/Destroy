package petrolpark.mc.destroy.core.chemistry.hazard;

import net.createmod.catnip.platform.CatnipServices;
import petrolpark.mc.destroy.DestroyAttachmentTypes;
import net.minecraft.core.HolderLookup;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import petrolpark.mc.destroy.chemistry.legacy.LegacySpecies;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.common.util.INBTSerializable;

public class EntityChemicalPoisonCapability implements INBTSerializable<CompoundTag> {

    /**
     * Living Entities only (as the old capability was).
     */
    public static Optional<EntityChemicalPoisonCapability> of(Entity entity) {
        return entity instanceof LivingEntity ? Optional.of(entity.getData(DestroyAttachmentTypes.ENTITY_CHEMICAL_POISON)) : Optional.empty();
    };
  
    private LegacySpecies molecule;

    public static void setMolecule(Entity entity, LegacySpecies molecule) {
        if (!(entity instanceof LivingEntity)) return;
        of(entity).ifPresent(cp -> {
            if (cp.molecule != null) return; // Don't replace existing poison
            cp.molecule = molecule;
            if (entity instanceof ServerPlayer serverPlayer) CatnipServices.NETWORK.sendToClient(serverPlayer, new ChemicalPoisonS2CPacket(molecule));
        });
    };

    public static void removeMolecule(Entity entity) {
        if (!(entity instanceof LivingEntity)) return;
        of(entity).ifPresent(cp -> {
            cp.molecule = null;
            if (entity instanceof ServerPlayer serverPlayer) CatnipServices.NETWORK.sendToClient(serverPlayer, new ChemicalPoisonS2CPacket((LegacySpecies)null));
        });
    };

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        if (molecule != null) tag.putString("ToxicMolecule", molecule.getFullID());
        return tag;
    };

    @Override
    public void deserializeNBT(HolderLookup.Provider registries, CompoundTag tag) {
        if (tag.contains("ToxicMolecule", Tag.TAG_STRING)) molecule = LegacySpecies.getMolecule(tag.getString("ToxicMolecule"));
    };

    @Nullable
    public LegacySpecies getMolecule() {
        return molecule;
    };
};
