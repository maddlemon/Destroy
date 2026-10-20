package petrolpark.mc.destroy;

import java.util.EnumMap;
import java.util.List;

import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem.Type;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * In 1.21 Armor Materials are registered records and durability is set on each Item (see {@link #getDurability}).
 */
public class DestroyArmorMaterials {

    private static final DeferredRegister<ArmorMaterial> ARMOR_MATERIALS = DeferredRegister.create(Registries.ARMOR_MATERIAL, Destroy.MOD_ID);

    public static final Holder<ArmorMaterial> HAZMAT = ARMOR_MATERIALS.register("hazmat", () -> new ArmorMaterial(
        Util.make(new EnumMap<>(Type.class), map -> {
            for (Type type : Type.values()) map.put(type, 1);
        }),
        0,
        SoundEvents.ARMOR_EQUIP_LEATHER,
        () -> Ingredient.of(DestroyTags.Items.TEXTILE_PLASTICS.tag),
        List.of(new ArmorMaterial.Layer(Destroy.asResource("hazmat"))), // Textures come from Create's BaseArmorItem
        0f,
        0f
    ));

    /**
     * 1.20.1's per-type durability multipliers, indexed by {@link Type#ordinal()} as 1.20.1 did (so they are not in vanilla's order).
     */
    private static final int[] MAX_DAMAGE = new int[] { 13, 15, 16, 11 };

    /**
     * @return The durability of a Hazmat armor piece, as in 1.20.1 ({@code MAX_DAMAGE[type] * 2})
     */
    public static int getHazmatDurability(Type type) {
        return MAX_DAMAGE[type.ordinal()] * 2;
    };

    public static void register(IEventBus modEventBus) {
        ARMOR_MATERIALS.register(modEventBus);
    };
};
