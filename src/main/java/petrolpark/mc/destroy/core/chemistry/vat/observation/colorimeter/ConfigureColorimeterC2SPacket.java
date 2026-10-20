package petrolpark.mc.destroy.core.chemistry.vat.observation.colorimeter;

import petrolpark.mc.destroy.DestroyPackets;
import petrolpark.mc.destroy.DestroyBlockEntityTypes;
import petrolpark.mc.destroy.chemistry.legacy.LegacySpecies;

import net.createmod.catnip.net.base.ServerboundPacketPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;

public class ConfigureColorimeterC2SPacket implements ServerboundPacketPayload {

    public static final StreamCodec<RegistryFriendlyByteBuf, ConfigureColorimeterC2SPacket> STREAM_CODEC = StreamCodec.ofMember(ConfigureColorimeterC2SPacket::toBytes, ConfigureColorimeterC2SPacket::new);

    protected final boolean observingGas;
    protected final LegacySpecies species;
    protected final BlockPos pos;

    public ConfigureColorimeterC2SPacket(boolean observingGas, LegacySpecies species, BlockPos pos) {
        this.observingGas = observingGas;
        this.species = species;
        this.pos = pos;
    };

    public ConfigureColorimeterC2SPacket(RegistryFriendlyByteBuf buffer) {
        observingGas = buffer.readBoolean();
        if (buffer.readBoolean()) {
            species = LegacySpecies.getMolecule(buffer.readUtf());
        } else {
            species = null;
        }
        pos = buffer.readBlockPos();
    };

    public void toBytes(RegistryFriendlyByteBuf buffer) {
        buffer.writeBoolean(observingGas);
        buffer.writeBoolean(species != null);
        if (species != null) buffer.writeUtf(species.getFullID());
        buffer.writeBlockPos(pos);
    };

    @Override
    public void handle(ServerPlayer player) {
        player.level().getBlockEntity(pos, DestroyBlockEntityTypes.COLORIMETER.get()).ifPresent(be -> {
            be.configure(species, observingGas);
        });
    };

    @Override
    public PacketTypeProvider getTypeProvider() {
        return DestroyPackets.CONFIGURE_COLORIMETER;
    };
    
};
