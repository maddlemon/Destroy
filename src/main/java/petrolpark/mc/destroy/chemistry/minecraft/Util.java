package petrolpark.mc.destroy.chemistry.minecraft;

import petrolpark.mc.destroy.chemistry.api.species.NamespacedId;

import net.minecraft.resources.ResourceLocation;

public class Util {
    
    public static final ResourceLocation toRl(NamespacedId speciesId) {
        return ResourceLocation.parse(speciesId.toString());
    };
};
