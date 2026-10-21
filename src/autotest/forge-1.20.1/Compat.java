package destroytest;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import net.createmod.catnip.gui.ScreenOpener;
import net.createmod.ponder.foundation.ui.PonderUI;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidType;

/** Forge 1.20.1 side of the calls that differ from 1.21.1. */
public class Compat {

    static ResourceLocation rl(String s) {
        return new ResourceLocation(s);
    };

    static Item item(String id) {
        return BuiltInRegistries.ITEM.get(rl(id));
    };

    static void openInventory(Minecraft mc) {
        mc.setScreen(new InventoryScreen(mc.player));
    };

    static void ponder(ItemStack stack) {
        ScreenOpener.transitionTo(PonderUI.of(stack));
    };

    static void allowCommands(WorldCreationUiState ui) {
        ui.setAllowCheats(true);
    };

    static List<Component> tooltip(Minecraft mc, ItemStack stack) {
        return stack.getTooltipLines(mc.player, TooltipFlag.NORMAL);
    };

    static String fluidName(Fluid fluid) {
        return fluid.getFluidType().getDescription().getString();
    };

    static String fluidInfo(Fluid fluid) {
        FluidType t = fluid.getFluidType();
        return "temp=" + t.getTemperature() + " density=" + t.getDensity() + " viscosity=" + t.getViscosity() + " light=" + t.getLightLevel();
    };

    static void rebuildTabs(Minecraft mc) {
        CreativeModeTabs.tryRebuildTabContents(mc.player.connection.enabledFeatures(), true, mc.level.registryAccess());
    };

    static String amount(Object o) {
        if (o instanceof ItemStack s) return s.getCount() > 1 ? "x" + s.getCount() : "";
        if (o instanceof net.minecraftforge.fluids.FluidStack f) return " " + f.getAmount() + "mB";
        return "";
    };

    /** Counts of every recipe type, then every Destroy-namespace recipe id with its type */
    static void recipes(Minecraft mc, List<String> rows) {
        Map<String, Integer> counts = new TreeMap<>();
        Map<String, String> destroy = new TreeMap<>();
        for (Recipe<?> r : mc.level.getRecipeManager().getRecipes()) {
            String type = String.valueOf(BuiltInRegistries.RECIPE_TYPE.getKey(r.getType()));
            counts.merge(type, 1, Integer::sum);
            if (AutoTest.isDestroy(r.getId())) destroy.put(r.getId().toString(), type);
        };
        counts.forEach((k, v) -> rows.add("COUNT\t" + k + "\t" + v));
        destroy.forEach((k, v) -> rows.add(k + "\t" + v));
    };

    /** Per Destroy item: enchantability, then enchantments an anvil accepts (A) and the enchanting table offers (T) */
    static void enchantments(Minecraft mc, List<String> rows) {
        for (Item item : BuiltInRegistries.ITEM) {
            if (!AutoTest.isDestroy(BuiltInRegistries.ITEM.getKey(item))) continue;
            ItemStack stack = new ItemStack(item);
            List<String> a = new java.util.ArrayList<>(), t = new java.util.ArrayList<>();
            for (net.minecraft.world.item.enchantment.Enchantment e : BuiltInRegistries.ENCHANTMENT) {
                if (e.canEnchant(stack)) a.add(BuiltInRegistries.ENCHANTMENT.getKey(e).toString());
                if (stack.canApplyAtEnchantingTable(e) && !e.isTreasureOnly() && e.isDiscoverable()) t.add(BuiltInRegistries.ENCHANTMENT.getKey(e).toString());
            };
            java.util.Collections.sort(a); java.util.Collections.sort(t);
            rows.add(AutoTest.id(item) + "	" + stack.getEnchantmentValue() + "	A=" + a + "	T=" + t);
        };
    };

    static void compost(List<String> rows) {
        for (Item item : BuiltInRegistries.ITEM) {
            if (!AutoTest.isDestroy(BuiltInRegistries.ITEM.getKey(item))) continue;
            float c = net.minecraft.world.level.block.ComposterBlock.COMPOSTABLES.getFloat(item);
            if (c > 0f) rows.add(AutoTest.id(item) + "	" + c);
        };
    };

    static final int DATA_PACK_FORMAT = 15;
    static final String STRUCTURE_DIR = "structures";

    static String saveBE(net.minecraft.world.level.block.entity.BlockEntity be, net.minecraft.server.MinecraftServer server) {
        return be.saveWithFullMetadata().toString();
    };
}
