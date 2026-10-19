package petrolpark.mc.destroy.core.universalarmortrim;

import petrolpark.mc.destroy.MoveToPetrolparkLibrary;
import com.simibubi.create.foundation.item.render.CustomRenderedItemModel;

import net.minecraft.client.resources.model.BakedModel;

@MoveToPetrolparkLibrary
public class UniversalTrimWrapperModel extends CustomRenderedItemModel {

    public UniversalTrimWrapperModel(BakedModel originalModel) {
        super(originalModel);
        originalModel.getOverrides();
    };
    
};
