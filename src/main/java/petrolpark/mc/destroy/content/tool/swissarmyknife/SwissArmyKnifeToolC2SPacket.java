package petrolpark.mc.destroy.content.tool.swissarmyknife;

import javax.annotation.Nullable;

import petrolpark.mc.destroy.DestroyPackets;
import petrolpark.mc.destroy.content.tool.swissarmyknife.SwissArmyKnifeItem.Tool;

import net.createmod.catnip.net.base.ServerboundPacketPayload;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

public class SwissArmyKnifeToolC2SPacket implements ServerboundPacketPayload {

    public static final StreamCodec<RegistryFriendlyByteBuf, SwissArmyKnifeToolC2SPacket> STREAM_CODEC = StreamCodec.ofMember(SwissArmyKnifeToolC2SPacket::toBytes, SwissArmyKnifeToolC2SPacket::new);

    private final boolean hasTool;
    private final Tool tool;

    public SwissArmyKnifeToolC2SPacket(@Nullable Tool tool) {
        this.tool = tool;
        hasTool = tool != null;
    };

    public SwissArmyKnifeToolC2SPacket(RegistryFriendlyByteBuf buffer) {
        hasTool = buffer.readBoolean();
        tool = hasTool ? Tool.values()[buffer.readInt()] : null;
    };

    public void toBytes(RegistryFriendlyByteBuf buffer) {
        buffer.writeBoolean(hasTool);
        buffer.writeInt(hasTool ? tool.ordinal() : 0);
    };

    @Override
    public void handle(ServerPlayer player) {
        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack stack = player.getItemInHand(hand);
            if (stack.getItem() instanceof SwissArmyKnifeItem) SwissArmyKnifeItem.putTool(stack, tool);
        };
    };

    @Override
    public PacketTypeProvider getTypeProvider() {
        return DestroyPackets.SWISS_ARMY_KNIFE_TOOL;
    };
    
};
