package petrolpark.mc.destroy.config;

public class DestroyCompatConfigs extends DestroyConfigBase {
    
    public final ConfigGroup TFMG = group(0, "TFMG", "Create: The Factory Must Grow");
    public final ConfigBool TFMGDistillationInDestroy = b(true, "TFMGDistillationInDestroy", "Allow applicable TFMG distillation recipes in Destroy's Bubble Caps");
    public final ConfigBool destroyDistillationInTFMG = b(true, "destroyDistillationInTFMG", "Allow Destroy distillation recipes in TFMG's Distillation Towers");


    public final ConfigGroup farmersDelight = group(0, "farmersDelight", "Farmer's Delight");
    public final ConfigBool cuttingOnionsCausesCrying = b(true, "cuttingOnionsCausesCrying", "Cutting onions on a Farmer's Delight Cutting Board causes the player to cry");

    @Override
    public String getName() {
        return "compatibility";
    };
};
