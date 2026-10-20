package petrolpark.mc.destroy.core.explosion;

import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.api.distmarker.Dist;
import net.createmod.catnip.platform.CatnipServices;
import java.util.Optional;

import petrolpark.mc.destroy.DestroyPackets;

import net.createmod.catnip.net.base.ClientboundPacketPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

public class SmartExplosionS2CPacket implements ClientboundPacketPayload {

    public static final StreamCodec<RegistryFriendlyByteBuf, SmartExplosionS2CPacket> STREAM_CODEC = StreamCodec.ofMember(SmartExplosionS2CPacket::toBytes, SmartExplosionS2CPacket::read);

    public final SmartExplosion explosion;
    public final Vec3 recipientKnockback;

    protected SmartExplosionS2CPacket(SmartExplosion explosion, Vec3 recipientKnockback) {
        this.explosion = explosion;
        this.recipientKnockback = recipientKnockback;
    };

    public static final SmartExplosionS2CPacket read(RegistryFriendlyByteBuf buffer) {
        SmartExplosion.Serializer<?> serializer = SmartExplosion.getType(buffer.readResourceLocation());
        return new SmartExplosionS2CPacket(serializer.read(buffer), new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble()));
    };

    public static void send(ServerPlayer player, SmartExplosion explosion) {
        CatnipServices.NETWORK.sendToClient(player, new SmartExplosionS2CPacket(explosion, Optional.ofNullable(explosion.getHitPlayers().get(player)).orElse(Vec3.ZERO)));
    };

    public void toBytes(RegistryFriendlyByteBuf buffer) {
        buffer.writeResourceLocation(explosion.getSerializer().id);
        explosion.write(buffer);
        buffer.writeDouble(recipientKnockback.x());
        buffer.writeDouble(recipientKnockback.y());
        buffer.writeDouble(recipientKnockback.z());
    };

    @OnlyIn(Dist.CLIENT)
    @Override
    public void handle(LocalPlayer player) {
        Minecraft mc = Minecraft.getInstance();
        explosion.finalizeExplosion(true);
        mc.player.setDeltaMovement(recipientKnockback);
    };

    @Override
    public PacketTypeProvider getTypeProvider() {
        return DestroyPackets.SMART_EXPLOSION;
    };
    
};
