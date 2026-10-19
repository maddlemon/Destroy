package petrolpark.mc.destroy.core.chemistry.storage.testtube;

import java.util.ArrayList;
import java.util.Map;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Either;
import petrolpark.mc.library.core.client.rendering.item.TransparentItemRenderer;
import petrolpark.mc.destroy.Destroy;
import petrolpark.mc.destroy.DestroyItems;
import petrolpark.mc.destroy.client.DummyBaker;
import com.simibubi.create.foundation.item.render.CustomRenderedItemModel;
import com.simibubi.create.foundation.item.render.CustomRenderedItemModelRenderer;
import com.simibubi.create.foundation.item.render.PartialItemModelRenderer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemModelGenerator;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(value = Dist.CLIENT, modid = Destroy.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class TestTubeItemRenderer extends CustomRenderedItemModelRenderer {

    private static final ResourceLocation defaultItemModelRl = ResourceLocation.parse("generated");
    private static final ItemModelGenerator ITEM_MODEL_GENERATOR = new ItemModelGenerator();
    private static BakedModel overlayModel;

    @SuppressWarnings("deprecation")
    private static BakedModel getModel() {
        if (overlayModel == null) {
            Material texture = new Material(TextureAtlas.LOCATION_BLOCKS, Destroy.asResource("item/test_tube_overlay"));
            overlayModel = DummyBaker.bake(
            ITEM_MODEL_GENERATOR.generateBlockModel(
                Material::sprite, // Tell the Item Model Generator how to fetch the sprite
                new BlockModel(
                    defaultItemModelRl,
                    new ArrayList<>(), // No elements - these get added by the Item Model Generator
                    Map.of("layer0", Either.left(texture)),
                    null,
                    null,
                    ItemTransforms.NO_TRANSFORMS, // These aren't needed as the model will be composited - the Item Model Generator sets them anyway
                    new ArrayList<>()
                )
            ),
            Destroy.asResource("test_tube_overlay")
        );
        };
        return overlayModel;
    };

    @Override
    protected void render(ItemStack stack, CustomRenderedItemModel model, PartialItemModelRenderer renderer, ItemDisplayContext transformType, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        Minecraft mc = Minecraft.getInstance();
        mc.getItemRenderer().render(stack, ItemDisplayContext.NONE, false, ms, buffer, light, overlay, model.getOriginalModel());
        TransparentItemRenderer.transformAndRenderModel(getModel(), transformType, DestroyItems.TEST_TUBE.get().getColor(stack), light, overlay, ms, buffer);
    };

    @SubscribeEvent
    public static void registerReloadListener(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(new ReloadListener());
    };

    protected static class ReloadListener implements ResourceManagerReloadListener {

        @Override
        public void onResourceManagerReload(ResourceManager resourceManager) {
            overlayModel = null;
        };

    };
    
};
