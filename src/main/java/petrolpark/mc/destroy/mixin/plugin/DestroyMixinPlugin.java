package petrolpark.mc.destroy.mixin.plugin;

import petrolpark.mc.library.mixin.plugin.PetrolparkMixinPlugin;

public class DestroyMixinPlugin extends PetrolparkMixinPlugin {
    
    @Override
    protected String getMixinPackage() {
        return "petrolpark.mc.destroy.mixin"; // compat.<modid>.* mixins only apply when that mod is loaded
    };

    @Override
    public void onLoad(String mixinPackage) {
        //NOOP
    };
};
