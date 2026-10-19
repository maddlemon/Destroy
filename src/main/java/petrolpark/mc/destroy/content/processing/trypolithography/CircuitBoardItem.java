package petrolpark.mc.destroy.content.processing.trypolithography;

import java.util.Optional;
import java.util.function.Consumer;

import petrolpark.mc.destroy.Destroy;
import petrolpark.mc.destroy.client.DestroyGuiTextures;
import com.simibubi.create.foundation.item.render.SimpleCustomRenderer;

import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

public class CircuitBoardItem extends CircuitPatternItem {

    public CircuitBoardItem(Properties properties) {
        super(properties);
    };

    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        return Optional.of(new CircuitPatternTooltipComponent(getPattern(stack), true, DestroyGuiTextures.CIRCUIT_BOARD_BORDER, DestroyGuiTextures.CIRCUIT_BOARD_CELL, DestroyGuiTextures.CIRCUIT_BOARD_CELL_SHADING));
    };

    @Override
    @OnlyIn(Dist.CLIENT)
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(SimpleCustomRenderer.create(this, new CircuitPatternItemRenderer(Destroy.asResource("item/circuit_pattern/circuit_board"))));
    };
    
};
