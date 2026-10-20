package petrolpark.mc.destroy.core.chemistry.hazard;

import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.api.distmarker.Dist;
import petrolpark.mc.destroy.DestroyPackets;
import petrolpark.mc.destroy.chemistry.legacy.LegacySpecies;

import net.createmod.catnip.net.base.ClientboundPacketPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public class ChemicalPoisonS2CPacket implements ClientboundPacketPayload {

    public static final StreamCodec<RegistryFriendlyByteBuf, ChemicalPoisonS2CPacket> STREAM_CODEC = StreamCodec.ofMember(ChemicalPoisonS2CPacket::toBytes, ChemicalPoisonS2CPacket::new);
    
    private final LegacySpecies molecule;
    
    public ChemicalPoisonS2CPacket(LegacySpecies molecule) {
        this.molecule = molecule;
    };

    public ChemicalPoisonS2CPacket(RegistryFriendlyByteBuf buffer) {
        String moleculeID = buffer.readUtf();
        if ("NO_MOLECULE".equals(moleculeID)) {
            molecule = null;
        } else {
            molecule = LegacySpecies.getMolecule(moleculeID);
        };

    };

    public void toBytes(RegistryFriendlyByteBuf buffer) {
        buffer.writeUtf(molecule == null ? "NO_MOLECULE" : molecule.getFullID());
    };

    @OnlyIn(Dist.CLIENT)
    @Override
    public void handle(LocalPlayer player) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null) {
            if (molecule == null) {
                EntityChemicalPoisonCapability.removeMolecule(minecraft.player);
            } else {
                EntityChemicalPoisonCapability.setMolecule(minecraft.player, molecule);
            };
        }
    };

    @Override
    public PacketTypeProvider getTypeProvider() {
        return DestroyPackets.CHEMICAL_POISON;
    };
};
