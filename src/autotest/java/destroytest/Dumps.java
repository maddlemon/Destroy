package destroytest;

import java.util.List;
import java.util.stream.Collectors;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;

/** Version-neutral data dumps; anything that differs between 1.20.1 and 1.21.1 goes through {@link Compat}. */
public class Dumps {

    static String clean(String s) {
        return s.replace('\t', ' ').replace('\n', ' ');
    };

    /** id, name, untranslated?, missing model?, max stack, tooltip */
    static void items(Minecraft mc, List<String> rows) {
        BakedModel missing = mc.getModelManager().getMissingModel();
        rows.add("id\tname\tmissing_model\tmax_stack\ttooltip");
        for (Item item : BuiltInRegistries.ITEM) {
            ResourceLocation rl = BuiltInRegistries.ITEM.getKey(item);
            if (!AutoTest.isDestroy(rl)) continue;
            ItemStack stack = new ItemStack(item);
            BakedModel model = mc.getItemRenderer().getModel(stack, mc.level, mc.player, 0);
            String tooltip = Compat.tooltip(mc, stack).stream().skip(1).map(Component::getString).map(Dumps::clean).collect(Collectors.joining(" | "));
            rows.add(rl + "\t" + clean(stack.getHoverName().getString()) + "\t" + (model == missing) + "\t" + stack.getMaxStackSize() + "\t" + tooltip);
        };
    };

    /** id, name, missing model (default state), has item, hardness, light */
    static void blocks(Minecraft mc, List<String> rows) {
        BakedModel missing = mc.getModelManager().getMissingModel();
        rows.add("id\tname\tmissing_model\titem\thardness\tlight\tstates");
        for (Block block : BuiltInRegistries.BLOCK) {
            ResourceLocation rl = BuiltInRegistries.BLOCK.getKey(block);
            if (!AutoTest.isDestroy(rl)) continue;
            BlockState state = block.defaultBlockState();
            long missingStates = block.getStateDefinition().getPossibleStates().stream().filter(s -> mc.getBlockRenderer().getBlockModel(s) == missing).count();
            rows.add(rl + "\t" + clean(block.getName().getString()) + "\t" + missingStates + "/" + block.getStateDefinition().getPossibleStates().size()
                + "\t" + BuiltInRegistries.ITEM.getKey(block.asItem()) + "\t" + state.getDestroySpeed(mc.level, mc.player.blockPosition()) + "\t" + state.getLightEmission());
        };
    };

    static void fluids(List<String> rows) {
        for (Fluid fluid : BuiltInRegistries.FLUID) {
            ResourceLocation rl = BuiltInRegistries.FLUID.getKey(fluid);
            if (!AutoTest.isDestroy(rl)) continue;
            rows.add(rl + "\t" + clean(Compat.fluidName(fluid)) + "\t" + Compat.fluidInfo(fluid));
        };
    };

    /** Every Destroy creative tab and its contents in order */
    static void creative(Minecraft mc, List<String> rows) {
        Compat.rebuildTabs(mc);
        for (CreativeModeTab tab : BuiltInRegistries.CREATIVE_MODE_TAB) {
            ResourceLocation rl = BuiltInRegistries.CREATIVE_MODE_TAB.getKey(tab);
            if (!AutoTest.isDestroy(rl)) continue;
            rows.add("TAB " + rl + "\t" + tab.getDisplayName().getString() + "\t" + tab.getDisplayItems().size() + " items");
            for (ItemStack stack : tab.getDisplayItems())
                rows.add(AutoTest.id(stack.getItem()) + "\t" + clean(stack.getHoverName().getString()) + "\t" + stack.getCount());
        };
        for (CreativeModeTab tab : BuiltInRegistries.CREATIVE_MODE_TAB) {
            ResourceLocation rl = BuiltInRegistries.CREATIVE_MODE_TAB.getKey(tab);
            if (AutoTest.isDestroy(rl) || tab.getType() != CreativeModeTab.Type.CATEGORY) continue;
            for (ItemStack stack : tab.getDisplayItems()) if (AutoTest.isDestroy(BuiltInRegistries.ITEM.getKey(stack.getItem())))
                rows.add("OTHER TAB " + rl + "	" + AutoTest.id(stack.getItem()) + "	" + clean(stack.getHoverName().getString()));
        };
        for (ItemStack stack : BuiltInRegistries.CREATIVE_MODE_TAB.get(CreativeModeTabs.SEARCH).getSearchTabDisplayItems())
            if (AutoTest.isDestroy(BuiltInRegistries.ITEM.getKey(stack.getItem()))) rows.add("SEARCH	" + AutoTest.id(stack.getItem()) + "	" + clean(stack.getHoverName().getString()));
        rows.add("search tab total destroy items: " + BuiltInRegistries.CREATIVE_MODE_TAB.get(CreativeModeTabs.SEARCH).getSearchTabDisplayItems().stream()
            .filter(s -> AutoTest.isDestroy(BuiltInRegistries.ITEM.getKey(s.getItem()))).count());
    };

    /** Tags of every Destroy item, block and fluid */
    @SuppressWarnings("deprecation")
    static void tags(List<String> rows) {
        for (Item item : BuiltInRegistries.ITEM) if (AutoTest.isDestroy(BuiltInRegistries.ITEM.getKey(item)))
            rows.add("item " + BuiltInRegistries.ITEM.getKey(item) + "	" + item.builtInRegistryHolder().tags().map(t -> t.location().toString()).sorted().collect(Collectors.joining(" ")));
        for (Block block : BuiltInRegistries.BLOCK) if (AutoTest.isDestroy(BuiltInRegistries.BLOCK.getKey(block)))
            rows.add("block " + BuiltInRegistries.BLOCK.getKey(block) + "	" + block.builtInRegistryHolder().tags().map(t -> t.location().toString()).sorted().collect(Collectors.joining(" ")));
        for (Fluid fluid : BuiltInRegistries.FLUID) if (AutoTest.isDestroy(BuiltInRegistries.FLUID.getKey(fluid)))
            rows.add("fluid " + BuiltInRegistries.FLUID.getKey(fluid) + "	" + fluid.builtInRegistryHolder().tags().map(t -> t.location().toString()).sorted().collect(Collectors.joining(" ")));
    };
}
