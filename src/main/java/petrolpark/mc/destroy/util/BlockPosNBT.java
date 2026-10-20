package petrolpark.mc.destroy.util;

import java.util.Optional;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;

/**
 * {@link NbtUtils#writeBlockPos} writes an int array in 1.21; 1.20.1 wrote a compound of {@code X}, {@code Y} and {@code Z}.
 * Reading both keeps Block Entities and Contraptions from old worlds working.
 */
public class BlockPosNBT {

    public static boolean contains(CompoundTag tag, String key) {
        return tag.contains(key, Tag.TAG_INT_ARRAY) || tag.contains(key, Tag.TAG_COMPOUND);
    };

    public static Optional<BlockPos> read(CompoundTag tag, String key) {
        if (tag.contains(key, Tag.TAG_COMPOUND)) {
            CompoundTag pos = tag.getCompound(key);
            return Optional.of(new BlockPos(pos.getInt("X"), pos.getInt("Y"), pos.getInt("Z")));
        };
        return NbtUtils.readBlockPos(tag, key);
    };

    /**
     * Like 1.20.1's {@code NbtUtils.readBlockPos}, a missing position is the origin.
     */
    public static BlockPos readOrOrigin(CompoundTag tag, String key) {
        return read(tag, key).orElse(BlockPos.ZERO);
    };
};
