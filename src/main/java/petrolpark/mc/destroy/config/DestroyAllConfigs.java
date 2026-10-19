package petrolpark.mc.destroy.config;

import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModLoadingContext;

/**
 * Facade over {@link DestroyConfigs} preserving the 1.20.1 static-field API
 * (DestroyAllConfigs.CLIENT / .COMMON / .SERVER) used across the codebase.
 */
public class DestroyAllConfigs {

    public static DestroyClientConfigs CLIENT;
    public static DestroyCommonConfigs COMMON;
    public static DestroyWorldConfigs SERVER;

    public static void register(ModLoadingContext context, ModContainer container) {
        DestroyConfigs.register(context, container);
        CLIENT = DestroyConfigs.client();
        COMMON = DestroyConfigs.common();
        SERVER = DestroyConfigs.server();
    };

};
