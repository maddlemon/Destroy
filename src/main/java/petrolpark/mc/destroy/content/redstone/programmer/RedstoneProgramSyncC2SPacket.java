package petrolpark.mc.destroy.content.redstone.programmer;

import net.createmod.catnip.platform.CatnipServices;
import petrolpark.mc.destroy.DestroyPackets;
import petrolpark.mc.destroy.config.DestroyAllConfigs;
import petrolpark.mc.destroy.content.redstone.programmer.RedstoneProgrammerMenu.DummyRedstoneProgram;

import net.createmod.catnip.net.base.ServerboundPacketPayload;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;

public class RedstoneProgramSyncC2SPacket implements ServerboundPacketPayload {

    public static final StreamCodec<RegistryFriendlyByteBuf, RedstoneProgramSyncC2SPacket> STREAM_CODEC = StreamCodec.ofMember(RedstoneProgramSyncC2SPacket::toBytes, RedstoneProgramSyncC2SPacket::new);

    public final RedstoneProgram program;

    public RedstoneProgramSyncC2SPacket(RedstoneProgram program) {
        this.program = program;
    };

    public RedstoneProgramSyncC2SPacket(RegistryFriendlyByteBuf buffer) {
        program = new DummyRedstoneProgram();
        program.read(buffer);
    };

    public void toBytes(RegistryFriendlyByteBuf buffer) {
        program.write(buffer);
    };

    @Override
    public void handle(ServerPlayer player) {
        AbstractContainerMenu menu = player.containerMenu;
        if (menu instanceof RedstoneProgrammerMenu programMenu) {
            RedstoneProgram program = programMenu.contentHolder;
            program.unload();
            program.copyFrom(this.program);
            program.load();
            program.whenChanged();
            CatnipServices.NETWORK.sendToClient(player, new RedstoneProgramSyncReplyS2CPacket());
            if (Math.min(program.getChannels().size() + 1, DestroyAllConfigs.SERVER.blocks.redstoneProgrammerMaxChannels.get()) * 2 > programMenu.slots.size()) programMenu.refreshSlots();
        };
    };

    @Override
    public PacketTypeProvider getTypeProvider() {
        return DestroyPackets.REDSTONE_PROGRAM_SYNC;
    };
    
};
