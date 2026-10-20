package petrolpark.mc.destroy.client;

import petrolpark.mc.destroy.Destroy;

import net.minecraft.client.gui.Gui;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.common.asm.enumextension.EnumProxy;

/**
 * Heart types added to {@link Gui.HeartType} through {@code META-INF/enumextensions.json} (1.20.1 used a mixin which rebuilt the enum's values).
 * Their sprites are in {@code textures/gui/sprites/hud/heart}.
 */
public class DestroyHeartTypes {

    public static final EnumProxy<Gui.HeartType> CHEMICAL_POISON = new EnumProxy<>(Gui.HeartType.class,
        sprite("chemical_poison_full"),
        sprite("chemical_poison_full_blinking"),
        sprite("chemical_poison_half"),
        sprite("chemical_poison_half_blinking"),
        sprite("chemical_poison_hardcore_full"),
        sprite("chemical_poison_hardcore_full_blinking"),
        sprite("chemical_poison_hardcore_half"),
        sprite("chemical_poison_hardcore_half_blinking")
    );

    private static ResourceLocation sprite(String name) {
        return Destroy.asResource("hud/heart/" + name);
    };
};
