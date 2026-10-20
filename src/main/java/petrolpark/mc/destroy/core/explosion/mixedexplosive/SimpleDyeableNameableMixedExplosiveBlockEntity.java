package petrolpark.mc.destroy.core.explosion.mixedexplosive;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.HolderLookup;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Dynamic;
import petrolpark.mc.destroy.Destroy;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.GameEventTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.BlockPositionSource;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.gameevent.PositionSource;
import net.minecraft.world.level.gameevent.vibrations.VibrationSystem;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;

public abstract class SimpleDyeableNameableMixedExplosiveBlockEntity extends SmartBlockEntity implements IDyeableMixedExplosiveBlockEntity, GameEventListener.Holder<VibrationSystem.Listener>, VibrationSystem {


    protected MixedExplosiveInventory inv;
    protected int color;
    protected Component name;

    public final VibrationSystem.Listener vibrationListener;
    public final SoundActivatedExplosiveVibrationSystemUser vibrationUser;
    protected VibrationSystem.Data vibrationData;

    public SimpleDyeableNameableMixedExplosiveBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        color = 0xFFFFFF;
        inv = createInv();
        refreshCapability();
        vibrationListener = new VibrationSystem.Listener(this);
        vibrationUser = new SoundActivatedExplosiveVibrationSystemUser();
        vibrationData = new VibrationSystem.Data();
    };

    public abstract MixedExplosiveInventory createInv();

    public abstract void explode(@Nullable Player cause);

    public void refreshCapability() {
        invalidateCapabilities();
    };

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {};

    public IItemHandler getItemHandler(Direction side) {
        return inv;
    };

    @Override
    public void onPlace(ItemStack blockItemStack, HolderLookup.Provider registries) {
        IDyeableMixedExplosiveBlockEntity.super.onPlace(blockItemStack, registries);
        if (blockItemStack.has(DataComponents.CUSTOM_NAME)) name = blockItemStack.getHoverName();
    };

    @Override
    public void tick() {
        super.tick();
        VibrationSystem.Ticker.tick(getLevel(), getVibrationData(), getVibrationUser());
    };

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        setColor(tag.getInt("Color"));
        if (tag.contains("CustomName", Tag.TAG_STRING)) name = Component.Serializer.fromJson(tag.getString("CustomName"));
        inv = createInv();
        inv.deserializeNBT(tag.getCompound("ExplosiveMix"));
        if (tag.contains("VibrationData", Tag.TAG_COMPOUND)) VibrationSystem.Data.CODEC.parse(new Dynamic<>(NbtOps.INSTANCE, tag.getCompound("VibrationData"))).resultOrPartial(Destroy.LOGGER::error).ifPresent(data -> vibrationData = data);
    };

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        tag.putInt("Color", color);
        if (name != null) tag.putString("CustomName", Component.Serializer.toJson(name));
        tag.put("ExplosiveMix", inv.serializeNBT());
        VibrationSystem.Data.CODEC.encodeStart(NbtOps.INSTANCE, vibrationData).resultOrPartial(Destroy.LOGGER::error).ifPresent(data -> tag.put("VibrationData", data));
    };

    @Override
    public MixedExplosiveInventory getExplosiveInventory() {
        return inv;
    };

    @Override
    public void setExplosiveInventory(MixedExplosiveInventory inv) {
        this.inv = inv;
        refreshCapability();
        notifyUpdate();
    };

    @Override
    public void setColor(int color) {
        boolean rerender = color != this.color;
        this.color = color;
        if (rerender) Destroy.unsafeRunClient(() -> () -> IDyeableMixedExplosiveBlockEntity.reRender(getLevel(), getBlockPos()));
        notifyUpdate();
    };

    @Override
    public ItemStack getFilledItemStack(ItemStack emptyItemStack, HolderLookup.Provider registries) {
        ItemStack stack = IDyeableMixedExplosiveBlockEntity.super.getFilledItemStack(emptyItemStack, registries);
        if (name != null) stack.set(DataComponents.CUSTOM_NAME, name);
        return stack;
    };

    @Override
    public int getColor() {
        return color;
    };

    @Override
    public Component getDisplayName() {
        return name != null ? name : getLevel().getBlockState(getBlockPos()).getBlock().getName();
    };

    @Override
    public boolean readFromClipboard(CompoundTag tag, Player player, Direction side, boolean simulate) {
        boolean success = IDyeableMixedExplosiveBlockEntity.super.readFromClipboard(tag, player, side, simulate);
        if (tag.contains("Name", Tag.TAG_STRING)) {
            if (!simulate) name = Component.Serializer.fromJson(tag.getString("Name"));
            return true;
        };
        return success;
    };

    @Override
    public boolean writeToClipboard(CompoundTag tag, Direction side) {
        boolean success = IDyeableMixedExplosiveBlockEntity.super.writeToClipboard(tag, side);
        if (name != null) {
            tag.putString("Name", Component.Serializer.toJson(name));
            return true;
        };
        return success;
    };

    @Override
    public VibrationSystem.Listener getListener() {
        return vibrationListener;
    };

    @Override
    public VibrationSystem.User getVibrationUser() {
        return vibrationUser;
    };

    @Override
    public VibrationSystem.Data getVibrationData() {
        return vibrationData;
    };

    public class SoundActivatedExplosiveVibrationSystemUser implements VibrationSystem.User {

        protected final BlockPositionSource position;

        public SoundActivatedExplosiveVibrationSystemUser() {
            position = new BlockPositionSource(getBlockPos());
        };

        @Override
        public int getListenerRadius() {
            return 8;
        };

        @Override
        public PositionSource getPositionSource() {
            return position;
        };

        @Override
        public boolean canReceiveVibration(ServerLevel level, BlockPos pos, GameEvent gameEvent, GameEvent.Context context) {
            return inv.getExplosiveProperties().fulfils(ExplosiveProperties.SOUND_ACTIVATED) && !pos.equals(getBlockPos()) && gameEvent.is(GameEventTags.VIBRATIONS) && !gameEvent.is(GameEventTags.IGNORE_VIBRATIONS_SNEAKING);
        };

        @Override
        public void onReceiveVibration(ServerLevel level, BlockPos pos, GameEvent gameEvent, Entity entity, Entity playerEntity, float distance) {
            explode(entity instanceof Player p ? p : null);
        };

    };
    
};
