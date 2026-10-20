package petrolpark.mc.destroy.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;

/**
 * The 1.21.1 library only lets Items in {@code #petrolpark:flaggable} carry Flags. 1.20.1's library let every Item that
 * isn't a Block carry Contaminants (Blocks only by tag), so e.g. any modded tool could be fireproofed; keep that.
 * Targets the library's anonymous {@code Flaggables.ITEM} ({@code Flaggables$2} in library 1.21.1-1.5.11: {@code $1} is
 * {@code NOT}, {@code $3} is {@code FLUID}). If a library update renumbers it, this mixin fails to apply rather than
 * silently changing nothing.
 */
@Mixin(targets = "petrolpark.mc.library.core.flags.Flaggables$2", remap = false)
public class FlaggableItemsMixin {

    @ModifyReturnValue(
        method = "isFlaggable(Lnet/minecraft/world/item/Item;)Z",
        at = @At("RETURN")
    )
    public boolean destroy$flagNonBlockItems(boolean original, Item item) {
        return original || !(item instanceof BlockItem);
    };
};
