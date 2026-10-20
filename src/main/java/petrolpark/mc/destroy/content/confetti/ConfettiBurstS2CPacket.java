package petrolpark.mc.destroy.content.confetti;

import org.joml.Vector3f;

import net.createmod.catnip.net.base.ClientboundPacketPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import petrolpark.mc.destroy.DestroyPackets;

public class ConfettiBurstS2CPacket implements ClientboundPacketPayload {

    public static final StreamCodec<RegistryFriendlyByteBuf, ConfettiBurstS2CPacket> STREAM_CODEC = StreamCodec.ofMember(ConfettiBurstS2CPacket::toBytes, ConfettiBurstS2CPacket::new);

    protected final ItemStack confetti;
    protected final Vector3f pos;
    protected final Vector3f velocity;

    public ConfettiBurstS2CPacket(RegistryFriendlyByteBuf buffer) {
        confetti = ItemStack.OPTIONAL_STREAM_CODEC.decode(buffer);
        pos = buffer.readVector3f();
        velocity = buffer.readVector3f();
    };

    public ConfettiBurstS2CPacket(ItemStack confetti, Vec3 pos, Vec3 v) {
        this.confetti = confetti;
        this.pos = new Vector3f((float)pos.x, (float)pos.y, (float)pos.z);
        this.velocity = new Vector3f((float)v.x, (float)v.y, (float)v.z);
    };

    public void toBytes(RegistryFriendlyByteBuf buffer) {
        ItemStack.OPTIONAL_STREAM_CODEC.encode(buffer, confetti);
        buffer.writeVector3f(pos);
        buffer.writeVector3f(velocity);
    };

    @Override
    public void handle(LocalPlayer player) {
        Minecraft mc = Minecraft.getInstance();
        RandomSource rand = mc.level.random;
        if (!(confetti.getItem() instanceof ConfettiItem item)) return;
        for (int i = 0; i < 256; i++) {
            Vec3 v = new Vec3(velocity).add(new Vec3(-0.05d + rand.nextFloat() * 0.1d, -0.05d + rand.nextFloat() * 0.1d, -0.05d + rand.nextFloat() * 0.1d));
            mc.level.addParticle(item.particleFactory.get(), pos.x(), pos.y(), pos.z(), v.x(), v.y(), v.z());
        };
    };

    @Override
    public PacketTypeProvider getTypeProvider() {
        return DestroyPackets.CONFETTI_BURST;
    };
    

};
