package petrolpark.mc.destroy.core.chemistry.vat.material;

import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.api.distmarker.Dist;
import java.util.HashMap;
import java.util.Map;

import petrolpark.mc.destroy.DestroyPackets;
import petrolpark.mc.destroy.client.DestroyPonderTags;
import petrolpark.mc.library.core.data.recipe.ingredient.BlockIngredient;

import net.createmod.catnip.net.base.ClientboundPacketPayload;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public class SyncVatMaterialsS2CPacket implements ClientboundPacketPayload {

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncVatMaterialsS2CPacket> STREAM_CODEC = StreamCodec.ofMember(SyncVatMaterialsS2CPacket::toBytes, SyncVatMaterialsS2CPacket::new);

    private final Map<BlockIngredient<?>, VatMaterial> materials;

    public SyncVatMaterialsS2CPacket(Map<BlockIngredient<?>, VatMaterial> materials) {
        this.materials = materials;
    };

    public SyncVatMaterialsS2CPacket(RegistryFriendlyByteBuf buffer) {
        int count = buffer.readVarInt();
        materials = new HashMap<>(count);
        for (int i = 0; i < count; i++) {
            materials.put(BlockIngredient.read(buffer), new VatMaterial(buffer.readFloat(), buffer.readFloat(), buffer.readBoolean(), false));
        };
    };

    public void toBytes(RegistryFriendlyByteBuf buffer) {
        buffer.writeVarInt(materials.size());
        materials.forEach((blockIngredient, material) -> {
            BlockIngredient.write(blockIngredient, buffer);
            buffer.writeFloat(material.maxPressure());
            buffer.writeFloat(material.thermalConductivity());
            buffer.writeBoolean(material.transparent());
        });
    };

    @OnlyIn(Dist.CLIENT)
    @Override
    public void handle(LocalPlayer player) {
        VatMaterial.clearDatapackMaterials();
        VatMaterial.BLOCK_MATERIALS.putAll(materials);
        DestroyPonderTags.refreshVatMaterialsTag();
    };

    @Override
    public PacketTypeProvider getTypeProvider() {
        return DestroyPackets.SYNC_VAT_MATERIALS;
    };
    
};
