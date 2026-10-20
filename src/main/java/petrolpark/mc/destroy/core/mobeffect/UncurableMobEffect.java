package petrolpark.mc.destroy.core.mobeffect;

import net.neoforged.neoforge.common.EffectCure;
import net.neoforged.neoforge.common.EffectCures;
import java.util.List;
import java.util.Set;

import petrolpark.mc.destroy.MoveToPetrolparkLibrary;

import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;

@MoveToPetrolparkLibrary
public class UncurableMobEffect extends DestroyMobEffect {
    public UncurableMobEffect(MobEffectCategory pCategory, int pColor) {
        super(pCategory, pColor);
    };
    
    /**
     * As in 1.20.1, milk does not cure this (its curative items did not include milk), but a Totem of Undying still clears it.
     */
    @Override
    public void fillEffectCures(Set<EffectCure> cures, MobEffectInstance effectInstance) {
        cures.add(EffectCures.PROTECTED_BY_TOTEM);
    };
};
