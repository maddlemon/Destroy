package petrolpark.mc.destroy.client.ponder;

import petrolpark.mc.destroy.Destroy;
import petrolpark.mc.destroy.client.DestroyPonderScenes;
import petrolpark.mc.destroy.client.DestroyPonderTags;

import net.createmod.ponder.api.registration.PonderPlugin;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.createmod.ponder.api.registration.PonderTagRegistrationHelper;
import net.minecraft.resources.ResourceLocation;

public class DestroyPonderPlugin implements PonderPlugin {
    @Override
    public String getModId() {
        return Destroy.MOD_ID;
    }

    @Override
    public void registerScenes(PonderSceneRegistrationHelper<ResourceLocation> helper) {
        DestroyPonderScenes.register(helper);
    }

    @Override
    public void registerTags(PonderTagRegistrationHelper<ResourceLocation> helper) {
        DestroyPonderTags.register(helper);
    }
}
