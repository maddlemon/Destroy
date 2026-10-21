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
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidType;

/** NeoForge 1.21.1 side of the calls that differ from 1.20.1. */
public class Compat {

    static ResourceLocation rl(String s) {
        return ResourceLocation.parse(s);
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
        ui.setAllowCommands(true);
    };

    static List<Component> tooltip(Minecraft mc, ItemStack stack) {
        return stack.getTooltipLines(Item.TooltipContext.of(mc.level), mc.player, TooltipFlag.Default.NORMAL);
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
        if (o instanceof net.neoforged.neoforge.fluids.FluidStack f) return " " + f.getAmount() + "mB";
        return "";
    };

    /** Counts of every recipe type, then every Destroy-namespace recipe id with its type */
    static void recipes(Minecraft mc, List<String> rows) {
        Map<String, Integer> counts = new TreeMap<>();
        Map<String, String> destroy = new TreeMap<>();
        for (RecipeHolder<?> h : mc.level.getRecipeManager().getRecipes()) {
            String type = String.valueOf(BuiltInRegistries.RECIPE_TYPE.getKey(h.value().getType()));
            counts.merge(type, 1, Integer::sum);
            if (AutoTest.isDestroy(h.id())) destroy.put(h.id().toString(), type);
        };
        counts.forEach((k, v) -> rows.add("COUNT\t" + k + "\t" + v));
        destroy.forEach((k, v) -> rows.add(k + "\t" + v));
    };

    /** Per Destroy item: enchantability, then enchantments an anvil accepts (A) and the enchanting table offers (T) */
    static void enchantments(Minecraft mc, List<String> rows) {
        var reg = mc.level.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT);
        for (Item item : BuiltInRegistries.ITEM) {
            if (!AutoTest.isDestroy(BuiltInRegistries.ITEM.getKey(item))) continue;
            ItemStack stack = new ItemStack(item);
            List<String> a = new java.util.ArrayList<>(), t = new java.util.ArrayList<>();
            reg.holders().forEach(h -> {
                if (stack.supportsEnchantment(h)) a.add(h.key().location().toString());
                if (stack.isPrimaryItemFor(h)) t.add(h.key().location().toString());
            });
            java.util.Collections.sort(a); java.util.Collections.sort(t);
            rows.add(AutoTest.id(item) + "	" + stack.getEnchantmentValue() + "	A=" + a + "	T=" + t);
        };
    };

    static void compost(List<String> rows) {
        for (Item item : BuiltInRegistries.ITEM) {
            if (!AutoTest.isDestroy(BuiltInRegistries.ITEM.getKey(item))) continue;
            var c = item.builtInRegistryHolder().getData(net.neoforged.neoforge.registries.datamaps.builtin.NeoForgeDataMaps.COMPOSTABLES);
            if (c != null) rows.add(AutoTest.id(item) + "	" + c.chance());
        };
    };

    static final int DATA_PACK_FORMAT = 48;
    static final String STRUCTURE_DIR = "structure";

    static String saveBE(net.minecraft.world.level.block.entity.BlockEntity be, net.minecraft.server.MinecraftServer server) {
        return be.saveWithFullMetadata(server.registryAccess()).toString();
    };
}
