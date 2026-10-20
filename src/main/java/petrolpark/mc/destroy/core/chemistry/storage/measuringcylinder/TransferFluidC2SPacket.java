package petrolpark.mc.destroy.core.chemistry.storage.measuringcylinder;

import petrolpark.mc.destroy.DestroyPackets;
import petrolpark.mc.destroy.core.chemistry.storage.IMixtureStorageItem;
import petrolpark.mc.destroy.core.chemistry.storage.ItemMixtureTank;

import net.createmod.catnip.net.base.ServerboundPacketPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;

public class TransferFluidC2SPacket implements ServerboundPacketPayload {

    public static final StreamCodec<RegistryFriendlyByteBuf, TransferFluidC2SPacket> STREAM_CODEC = StreamCodec.ofMember(TransferFluidC2SPacket::toBytes, TransferFluidC2SPacket::new);

    private final BlockPos pos;
    private final Direction face;
    private final InteractionHand hand; // The hand the Item is in

    private final boolean blockToItem; // If true, the Fluid in the Block should be transferred to the Item
    private final int transferAmount;

    public TransferFluidC2SPacket(BlockPos pos, Direction face, InteractionHand hand, boolean blockToItem, int transferAmount) {
        this.pos = pos;
        this.face = face;
        this.hand = hand;
        this.blockToItem = blockToItem;
        this.transferAmount = transferAmount;
    };

    public TransferFluidC2SPacket(RegistryFriendlyByteBuf buffer) {
        pos = buffer.readBlockPos();
        face = buffer.readEnum(Direction.class);
        hand = buffer.readBoolean() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
        blockToItem = buffer.readBoolean();
        transferAmount = buffer.readVarInt();
    };

    public void toBytes(RegistryFriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos);
        buffer.writeEnum(face);
        buffer.writeBoolean(hand == InteractionHand.MAIN_HAND);
        buffer.writeBoolean(blockToItem);
        buffer.writeVarInt(transferAmount);
    };

    @Override
    public void handle(ServerPlayer player) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.getItem() instanceof IMixtureStorageItem mixtureItem) {
            Level level = player.level();
            BlockState state = level.getBlockState(pos);
            IFluidHandlerItem cap = stack.getCapability(Capabilities.FluidHandler.ITEM);
            if (cap == null) return;
            ItemMixtureTank itemTank = (ItemMixtureTank)cap;
            IFluidHandler otherTank = mixtureItem.getTank(level, pos, state, face, player, hand, stack, !blockToItem);
            if (otherTank == null) return;
            if (blockToItem) {
                mixtureItem.afterFill(level, pos, state, face, player, hand, stack, mixtureItem.tryFill(stack, itemTank, otherTank, transferAmount));
            } else {
                mixtureItem.afterEmpty(level, pos, state, face, player, hand, stack, mixtureItem.tryEmpty(stack, itemTank, otherTank, false, transferAmount));
            };
        };
    };

    @Override
    public PacketTypeProvider getTypeProvider() {
        return DestroyPackets.TRANSFER_FLUID;
    };
    
};
