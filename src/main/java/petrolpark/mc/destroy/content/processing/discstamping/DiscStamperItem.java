package petrolpark.mc.destroy.content.processing.discstamping;

import java.util.List;


import petrolpark.mc.destroy.DestroyDataComponentTypes;
import petrolpark.mc.destroy.DestroyItems;
import petrolpark.mc.destroy.core.item.WithSecondaryItem;
import com.tterrag.registrate.util.nullness.NonnullType;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.JukeboxSong;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemContainerContents;

public class DiscStamperItem extends WithSecondaryItem {

    public DiscStamperItem(@NonnullType Properties properties) {
        super(properties, DiscStamperItem::getDisc);
    };

    public static ItemStack getDisc(ItemStack stamper) {
        return stamper.getOrDefault(DestroyDataComponentTypes.DISC, ItemContainerContents.EMPTY).copyOne();
    };

    public static ItemStack of(ItemStack discStack) {
        ItemStack stack = DestroyItems.DISC_STAMPER.asStack();
        stack.set(DestroyDataComponentTypes.DISC, ItemContainerContents.fromItems(List.of(discStack)));
        return stack;
    };

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext tooltipContext, List<Component> tooltip, TooltipFlag isAdvanced) {
        // The disc's track title (1.20.1 used the RecordItem's description)
        JukeboxSong.fromStack(tooltipContext.registries(), getDisc(stack)).ifPresent(song -> tooltip.add(song.value().description().copy().withStyle(ChatFormatting.GRAY)));
    };

    @Override
    public ItemStack getCraftingRemainingItem(ItemStack itemStack) {
        return itemStack;
    };
    
};
