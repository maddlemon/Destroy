package petrolpark.mc.destroy.core.seismology;

import net.createmod.catnip.gui.ScreenOpener;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import petrolpark.mc.library.compat.create.core.world.item.valueSettings.ItemValueSettingsScreen;

/**
 * Client half of the Seismometer's provider menu (what the library's {@code IValueSettingsItem#useValueSettingsItem} does), kept in its own class so
 * the Screen is only ever linked on the client.
 */
@OnlyIn(Dist.CLIENT)
public class SeismometerClient {

    public static void openValueSettings(SeismometerItem item, Player player, InteractionHand hand, ItemStack stack) {
        ScreenOpener.open(new ItemValueSettingsScreen(hand, item.createValueSettingsBoard(player, hand, stack), item.getValueSettings(stack)));
    };
};
