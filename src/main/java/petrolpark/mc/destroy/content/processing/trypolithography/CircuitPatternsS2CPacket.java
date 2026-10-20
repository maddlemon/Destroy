package petrolpark.mc.destroy.content.processing.trypolithography;

import java.util.Map;
import java.util.Map.Entry;
import java.util.HashMap;

import petrolpark.mc.destroy.DestroyPackets;
import petrolpark.mc.destroy.Destroy;

import net.createmod.catnip.net.base.ClientboundPacketPayload;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

public class CircuitPatternsS2CPacket implements ClientboundPacketPayload {

    public static final StreamCodec<RegistryFriendlyByteBuf, CircuitPatternsS2CPacket> STREAM_CODEC = StreamCodec.ofMember(CircuitPatternsS2CPacket::toBytes, CircuitPatternsS2CPacket::read);

    public final Map<ResourceLocation, Integer> patterns;

    public CircuitPatternsS2CPacket(Map<ResourceLocation, Integer> patterns) {
        this.patterns = patterns;
    };

    public static CircuitPatternsS2CPacket read(RegistryFriendlyByteBuf buffer) {
        int entries = buffer.readVarInt();
        Map<ResourceLocation, Integer> patterns = new HashMap<>(entries);
        for (int i = 0; i < entries; i++) {
            patterns.put(buffer.readResourceLocation(), (int)Short.MAX_VALUE + (int)buffer.readShort());
        };
        return new CircuitPatternsS2CPacket(patterns);
    };

    public void toBytes(RegistryFriendlyByteBuf buffer) {
        buffer.writeVarInt(patterns.size());
        for (Entry<ResourceLocation, Integer> entry : patterns.entrySet()) {
            buffer.writeResourceLocation(entry.getKey());
            buffer.writeShort((short)(entry.getValue() - Short.MAX_VALUE));
        };
    };

    @Override
    public void handle(LocalPlayer player) {
        Destroy.CIRCUIT_PATTERN_HANDLER.setPatterns(patterns);
    };

    @Override
    public PacketTypeProvider getTypeProvider() {
        return DestroyPackets.CIRCUIT_PATTERNS;
    };
    
};
