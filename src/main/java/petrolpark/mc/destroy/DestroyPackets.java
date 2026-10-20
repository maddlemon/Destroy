package petrolpark.mc.destroy;

import net.createmod.catnip.net.base.BasePacketPayload;
import net.createmod.catnip.net.base.CatnipPacketRegistry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import petrolpark.mc.destroy.content.confetti.ConfettiBurstS2CPacket;
import petrolpark.mc.destroy.content.processing.glassblowing.SelectGlassblowingRecipeC2SPacket;
import petrolpark.mc.destroy.content.processing.trypolithography.CircuitPatternsS2CPacket;
import petrolpark.mc.destroy.content.processing.trypolithography.keypunch.ChangeKeypunchPositionC2SPacket;
import petrolpark.mc.destroy.content.processing.trypolithography.keypunch.NameKeypunchC2SPacket;
import petrolpark.mc.destroy.content.processing.trypolithography.keypunch.RequestKeypunchNameS2CPacket;
import petrolpark.mc.destroy.content.product.periodictable.RefreshPeriodicTablePonderSceneS2CPacket;
import petrolpark.mc.destroy.content.redstone.programmer.RedstoneProgramSyncC2SPacket;
import petrolpark.mc.destroy.content.redstone.programmer.RedstoneProgramSyncReplyS2CPacket;
import petrolpark.mc.destroy.content.redstone.programmer.RedstoneProgrammerPowerChangedS2CPacket;
import petrolpark.mc.destroy.content.tool.swissarmyknife.SwissArmyKnifeToolC2SPacket;
import petrolpark.mc.destroy.core.chemistry.hazard.ChemicalPoisonS2CPacket;
import petrolpark.mc.destroy.core.chemistry.hazard.mobeffect.CryingS2CPacket;
import petrolpark.mc.destroy.core.chemistry.storage.measuringcylinder.TransferFluidC2SPacket;
import petrolpark.mc.destroy.core.chemistry.vat.material.SyncVatMaterialsS2CPacket;
import petrolpark.mc.destroy.core.chemistry.vat.observation.RedstoneQuantityMonitorThresholdChangeC2SPacket;
import petrolpark.mc.destroy.core.chemistry.vat.observation.colorimeter.ConfigureColorimeterC2SPacket;
import petrolpark.mc.destroy.core.explosion.SmartExplosionS2CPacket;
import petrolpark.mc.destroy.core.extendedinventory.ExtraInventorySizeChangeS2CPacket;
import petrolpark.mc.destroy.core.extendedinventory.RequestInventoryFullStateC2SPacket;
import petrolpark.mc.destroy.core.fluid.gasparticle.EvaporatingFluidS2CPacket;
import petrolpark.mc.destroy.core.pollution.ChunkPollutionPacket;
import petrolpark.mc.destroy.core.pollution.LevelPollutionPacket;
import petrolpark.mc.destroy.core.seismology.MarkSeismographPacket;
import petrolpark.mc.destroy.core.seismology.SeismometerSpikePacket;

public enum DestroyPackets implements BasePacketPayload.PacketTypeProvider {

    // Server -> client
    CHUNK_POLLUTION(ChunkPollutionPacket.class, ChunkPollutionPacket.STREAM_CODEC),
    LEVEL_POLLUTION(LevelPollutionPacket.class, LevelPollutionPacket.STREAM_CODEC),
    SEISMOMETER_SPIKE(SeismometerSpikePacket.class, StreamCodec.unit(SeismometerSpikePacket.INSTANCE)),
    CHEMICAL_POISON(ChemicalPoisonS2CPacket.class, ChemicalPoisonS2CPacket.STREAM_CODEC),
    CIRCUIT_PATTERNS(CircuitPatternsS2CPacket.class, CircuitPatternsS2CPacket.STREAM_CODEC),
    CONFETTI_BURST(ConfettiBurstS2CPacket.class, ConfettiBurstS2CPacket.STREAM_CODEC),
    CRYING(CryingS2CPacket.class, CryingS2CPacket.STREAM_CODEC),
    EVAPORATING_FLUID(EvaporatingFluidS2CPacket.class, EvaporatingFluidS2CPacket.STREAM_CODEC),
    EXTRA_INVENTORY_SIZE_CHANGE(ExtraInventorySizeChangeS2CPacket.class, ExtraInventorySizeChangeS2CPacket.STREAM_CODEC),
    REDSTONE_PROGRAMMER_POWER_CHANGED(RedstoneProgrammerPowerChangedS2CPacket.class, RedstoneProgrammerPowerChangedS2CPacket.STREAM_CODEC),
    REDSTONE_PROGRAM_SYNC_REPLY(RedstoneProgramSyncReplyS2CPacket.class, RedstoneProgramSyncReplyS2CPacket.STREAM_CODEC),
    REFRESH_PERIODIC_TABLE_PONDER_SCENE(RefreshPeriodicTablePonderSceneS2CPacket.class, RefreshPeriodicTablePonderSceneS2CPacket.STREAM_CODEC),
    REQUEST_KEYPUNCH_NAME(RequestKeypunchNameS2CPacket.class, RequestKeypunchNameS2CPacket.STREAM_CODEC),
    SMART_EXPLOSION(SmartExplosionS2CPacket.class, SmartExplosionS2CPacket.STREAM_CODEC),
    SYNC_VAT_MATERIALS(SyncVatMaterialsS2CPacket.class, SyncVatMaterialsS2CPacket.STREAM_CODEC),

    // Client -> server
    MARK_SEISMOGRAPH(MarkSeismographPacket.class, MarkSeismographPacket.STREAM_CODEC),
    CHANGE_KEYPUNCH_POSITION(ChangeKeypunchPositionC2SPacket.class, ChangeKeypunchPositionC2SPacket.STREAM_CODEC),
    CONFIGURE_COLORIMETER(ConfigureColorimeterC2SPacket.class, ConfigureColorimeterC2SPacket.STREAM_CODEC),
    NAME_KEYPUNCH(NameKeypunchC2SPacket.class, NameKeypunchC2SPacket.STREAM_CODEC),
    REDSTONE_PROGRAM_SYNC(RedstoneProgramSyncC2SPacket.class, RedstoneProgramSyncC2SPacket.STREAM_CODEC),
    REDSTONE_QUANTITY_MONITOR_THRESHOLD_CHANGE(RedstoneQuantityMonitorThresholdChangeC2SPacket.class, RedstoneQuantityMonitorThresholdChangeC2SPacket.STREAM_CODEC),
    REQUEST_INVENTORY_FULL_STATE(RequestInventoryFullStateC2SPacket.class, RequestInventoryFullStateC2SPacket.STREAM_CODEC),
    SELECT_GLASSBLOWING_RECIPE(SelectGlassblowingRecipeC2SPacket.class, SelectGlassblowingRecipeC2SPacket.STREAM_CODEC),
    SWISS_ARMY_KNIFE_TOOL(SwissArmyKnifeToolC2SPacket.class, SwissArmyKnifeToolC2SPacket.STREAM_CODEC),
    TRANSFER_FLUID(TransferFluidC2SPacket.class, TransferFluidC2SPacket.STREAM_CODEC),
    ;

    private final CatnipPacketRegistry.PacketType<?> type;

    <T extends BasePacketPayload> DestroyPackets(Class<T> clazz, StreamCodec<? super RegistryFriendlyByteBuf, T> codec) {
		type = new CatnipPacketRegistry.PacketType<>(
			new CustomPacketPayload.Type<>(Destroy.asResource(name().toLowerCase())),
			clazz, codec
		);
	};

    @Override
    @SuppressWarnings("unchecked")
    public <T extends CustomPacketPayload> CustomPacketPayload.Type<T> getType() {
        return (CustomPacketPayload.Type<T>)type.type();
    };

    public static final void register() {
		final CatnipPacketRegistry packetRegistry = new CatnipPacketRegistry(Destroy.MOD_ID, 1);
		for (DestroyPackets packet : DestroyPackets.values()) {
			packetRegistry.registerPacket(packet.type);
		};
		packetRegistry.registerAllPackets();
	};
};
