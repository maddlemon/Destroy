package petrolpark.mc.destroy.compat.jei.animation;

import petrolpark.mc.destroy.chemistry.legacy.LegacySpecies;
import petrolpark.mc.destroy.core.chemistry.MoleculeRenderer;

import mezz.jei.api.gui.drawable.IDrawable;
import net.minecraft.client.gui.GuiGraphics;

public class JEIMoleculeRenderer extends MoleculeRenderer implements IDrawable {

    public JEIMoleculeRenderer(LegacySpecies molecule) {
        super(molecule);
    };

    @Override
    public void draw(GuiGraphics graphics, int xOffset, int yOffset) {
        render(xOffset, yOffset, graphics);
    };
    
};
