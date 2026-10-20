package petrolpark.mc.destroy.core.chemistry.novelcompounds;

import petrolpark.mc.destroy.DestroyAttachmentTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.core.HolderLookup;
import java.util.Optional;
import java.util.HashSet;
import java.util.Set;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import petrolpark.mc.destroy.DestroyStats;
import petrolpark.mc.destroy.chemistry.legacy.LegacySpecies;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.util.INBTSerializable;

public class PlayerNovelCompoundsSynthesizedCapability implements INBTSerializable<CompoundTag> {

    /**
     * Players only (as the old capability was).
     */
    public static Optional<PlayerNovelCompoundsSynthesizedCapability> of(Entity entity) {
        return entity instanceof Player ? Optional.of(entity.getData(DestroyAttachmentTypes.PLAYER_NOVEL_COMPOUNDS_SYNTHESIZED)) : Optional.empty();
    };
  
    protected Set<String> novelCompoundFROWNSStrings = new HashSet<>();

    public static void add(Player player, LegacySpecies novelCompound) {
        of(player).ifPresent(pncs -> {
            if (pncs.novelCompoundFROWNSStrings.add(novelCompound.getFROWNSCode())) player.awardStat(DestroyStats.NOVEL_COMPOUNDS_SYNTHESIZED.get());
        });
    };

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        ListTag listTag = new ListTag();
        novelCompoundFROWNSStrings.forEach(string -> listTag.add(StringTag.valueOf(string)));
        tag.put("FROWNSStrings", listTag);
        return tag;
    };

    @Override
    public void deserializeNBT(HolderLookup.Provider registries, CompoundTag tag) {
        novelCompoundFROWNSStrings = tag.getList("FROWNSStrings", Tag.TAG_STRING).stream().map(Tag::getAsString).collect(HashSet::new, Set::add, Set::addAll);
    };

    public void copyFrom(PlayerNovelCompoundsSynthesizedCapability pncs) {
        novelCompoundFROWNSStrings.clear();
        novelCompoundFROWNSStrings.addAll(pncs.novelCompoundFROWNSStrings);
    };
};
