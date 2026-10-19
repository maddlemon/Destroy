package petrolpark.mc.destroy.core.chemistry.hazard;

import java.util.function.Supplier;

import petrolpark.mc.destroy.chemistry.legacy.LegacySpecies;
import com.petrolpark.network.packet.S2CPacket;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.NetworkEvent;

public class ChemicalPoisonS2CPacket extends S2CPacket {
    
    private final LegacySpecies molecule;
    
    public ChemicalPoisonS2CPacket(LegacySpecies molecule) {
        this.molecule = molecule;
    };

    public ChemicalPoisonS2CPacket(FriendlyByteBuf buffer) {
        String moleculeID = buffer.readUtf();
        if ("NO_MOLECULE".equals(moleculeID)) {
            molecule = null;
        } else {
            molecule = LegacySpecies.getMolecule(moleculeID);
        };

    };

    @Override
    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeUtf(molecule == null ? "NO_MOLECULE" : molecule.getFullID());
    };

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.player != null) {
                if (molecule == null) {
                    EntityChemicalPoisonCapability.removeMolecule(minecraft.player);
                } else {
                    EntityChemicalPoisonCapability.setMolecule(minecraft.player, molecule);
                };
            }
        });
        return true;
    }; 
};
