package petrolpark.mc.destroy.core.chemistry.hazard.mobeffect;

import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.api.distmarker.Dist;
import petrolpark.mc.destroy.DestroyPackets;
import petrolpark.mc.destroy.DestroyMobEffects;

import net.createmod.catnip.net.base.ClientboundPacketPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class CryingS2CPacket implements ClientboundPacketPayload {

    public static final StreamCodec<RegistryFriendlyByteBuf, CryingS2CPacket> STREAM_CODEC = StreamCodec.ofMember(CryingS2CPacket::toBytes, CryingS2CPacket::new);

    private final boolean isCrying;
    private final int entityId;

    public CryingS2CPacket(LivingEntity entity, boolean isCrying) {
        this.entityId = entity.getId();
        this.isCrying = isCrying;
    };

    public CryingS2CPacket(RegistryFriendlyByteBuf buffer) {
        this.isCrying = buffer.readBoolean();
        this.entityId = buffer.readInt();
    };

    public void toBytes(RegistryFriendlyByteBuf buffer) {
        buffer.writeBoolean(isCrying);
        buffer.writeInt(entityId);
    };
    
    @SuppressWarnings("resource")
    @OnlyIn(Dist.CLIENT)
    @Override
    public void handle(LocalPlayer player) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return;
        Entity entity = level.getEntity(entityId);
        if (entity == null || !(entity instanceof LivingEntity livingEntity)) return;
        if (isCrying && !livingEntity.hasEffect(DestroyMobEffects.CRYING)) {
            livingEntity.addEffect(new MobEffectInstance(DestroyMobEffects.CRYING, Integer.MAX_VALUE, 0, true, false, false));
        } else {
            livingEntity.removeEffect(DestroyMobEffects.CRYING);
        };
    };

    @Override
    public PacketTypeProvider getTypeProvider() {
        return DestroyPackets.CRYING;
    };  
};
