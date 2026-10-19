package petrolpark.mc.destroy;

import static petrolpark.mc.destroy.Destroy.REGISTRATE;

import petrolpark.mc.destroy.core.chemistry.vat.material.VatMaterial;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.RegistryBuilder;

public class DestroyRegistryKeys {
    
    public static final ResourceKey<Registry<VatMaterial>> VAT_MATERIAL = REGISTRATE.makeRegistry("vat_material", RegistryBuilder::new); // Data
    //TODO BM4x4 generator types (probably in library)
    //TODO Item Explosive Properties
    //TODO Seismology providers
};
