package petrolpark.mc.destroy.chemistry.legacy.reactionresult;

import java.util.Optional;

import petrolpark.mc.destroy.chemistry.legacy.LegacySpecies;
import petrolpark.mc.destroy.chemistry.legacy.LegacyReaction;
import petrolpark.mc.destroy.chemistry.legacy.ReactionResult;
import petrolpark.mc.destroy.core.chemistry.novelcompounds.PlayerNovelCompoundsSynthesizedCapability;
import petrolpark.mc.destroy.core.chemistry.vat.VatControllerBlockEntity;
import petrolpark.mc.destroy.core.data.advancement.DestroyAdvancementBehaviour;
import com.simibubi.create.content.processing.basin.BasinBlockEntity;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class NovelCompoundSynthesizedReactionResult extends ReactionResult {

    public final LegacySpecies novelMolecule;

    public NovelCompoundSynthesizedReactionResult(float moles, LegacyReaction reaction, LegacySpecies novelMolecule) {
        super(moles, reaction);
        this.novelMolecule = novelMolecule;
    };

    @Override
    public void onBasinReaction(Level level, BasinBlockEntity basin) {
        Optional.ofNullable(basin.getBehaviour(DestroyAdvancementBehaviour.TYPE)).ifPresent(behaviour -> {
            if (behaviour.getPlayer() != null)
                PlayerNovelCompoundsSynthesizedCapability.add(behaviour.getPlayer(), novelMolecule);
        });
    };

    @Override
    public void onVatReaction(Level level, VatControllerBlockEntity vatController) {
        Player player = vatController.getBehaviour(DestroyAdvancementBehaviour.TYPE).getPlayer();
        if (player != null) PlayerNovelCompoundsSynthesizedCapability.add(player, novelMolecule);
    };
    
};
