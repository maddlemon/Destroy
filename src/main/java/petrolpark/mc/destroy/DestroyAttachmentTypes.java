package petrolpark.mc.destroy;

import petrolpark.mc.destroy.core.player.PlayerPreviousPositionsCapability;
import petrolpark.mc.destroy.core.player.PlayerCrouchingCapability;
import petrolpark.mc.destroy.core.chemistry.novelcompounds.PlayerNovelCompoundsSynthesizedCapability;
import petrolpark.mc.destroy.core.chemistry.hazard.EntityChemicalPoisonCapability;
import petrolpark.mc.destroy.content.product.babyblue.PlayerBabyBlueAddictionCapability;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import petrolpark.mc.destroy.core.oil.ChunkCrudeOil;
import petrolpark.mc.destroy.core.pollution.ChunkPollution;
import petrolpark.mc.destroy.core.pollution.LevelPollution;

public class DestroyAttachmentTypes {

    private static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, Destroy.MOD_ID);

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<LevelPollution>> LEVEL_POLLUTION = ATTACHMENT_TYPES.register("level_pollution", AttachmentType.builder(LevelPollution::create)
        .serialize(LevelPollution.SERIALIZER)
        ::build
    );

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<ChunkPollution>> CHUNK_POLLUTION = ATTACHMENT_TYPES.register("chunk_pollution", AttachmentType.builder(ChunkPollution::create)
        .serialize(ChunkPollution.SERIALIZER)
        ::build
    );

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<ChunkCrudeOil>> CHUNK_CRUDE_OIL = ATTACHMENT_TYPES.register("crude_oil", AttachmentType.builder(ChunkCrudeOil::create)
        .serialize(ChunkCrudeOil.SERIALIZER)
        ::build
    );

    // Former Forge entity capabilities. Not copied on death by default; DestroyCommonEvents#onPlayerCloned copies the ones that should survive.
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<EntityChemicalPoisonCapability>> ENTITY_CHEMICAL_POISON = ATTACHMENT_TYPES.register("chemical_poison", AttachmentType.serializable(EntityChemicalPoisonCapability::new)::build);
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<PlayerBabyBlueAddictionCapability>> PLAYER_BABY_BLUE_ADDICTION = ATTACHMENT_TYPES.register("baby_blue_addiction", AttachmentType.serializable(PlayerBabyBlueAddictionCapability::new)::build);
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<PlayerCrouchingCapability>> PLAYER_CROUCHING = ATTACHMENT_TYPES.register("crouching", AttachmentType.serializable(PlayerCrouchingCapability::new)::build);
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<PlayerNovelCompoundsSynthesizedCapability>> PLAYER_NOVEL_COMPOUNDS_SYNTHESIZED = ATTACHMENT_TYPES.register("novel_compounds_synthesized", AttachmentType.serializable(PlayerNovelCompoundsSynthesizedCapability::new)::build);
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<PlayerPreviousPositionsCapability>> PLAYER_PREVIOUS_POSITIONS = ATTACHMENT_TYPES.register("previous_positions", AttachmentType.serializable(PlayerPreviousPositionsCapability::create)::build);

    public static final void register(IEventBus modEventBus) {
        ATTACHMENT_TYPES.register(modEventBus);
    };
};
