package petrolpark.mc.destroy.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import petrolpark.mc.destroy.DestroyClient;
import petrolpark.mc.destroy.MoveToPetrolparkLibrary;
import petrolpark.mc.destroy.core.extendedinventory.ExtendedInventoryClientHandler;

import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;

@MoveToPetrolparkLibrary
@Mixin(RecipeBookComponent.class)
public abstract class RecipeBookComponentMixin {

    @Shadow
    private int xOffset;

    @Shadow
    private boolean widthTooNarrow;
    
    @Inject(
        method = "initVisuals",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/gui/screens/recipebook/RecipeBookComponent;width:I"
        )
    )
    public void inInitVisuals(CallbackInfo ci) {
        if (!widthTooNarrow) xOffset += - DestroyClient.EXTENDED_INVENTORY_HANDLER.getLeftmostX() + ExtendedInventoryClientHandler.INVENTORY_SPACING;
    };
};
