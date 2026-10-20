package petrolpark.mc.destroy.test;

import petrolpark.mc.destroy.Destroy;
import petrolpark.mc.destroy.DestroyBlocks;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * Smoke tests for {@code ./gradlew runGameTestServer} (only registered when the {@code destroy} game test namespace is enabled, as in the dev run configs).
 * The GameTest server loads every datapack like a dedicated server - recipes, tags, loot tables, advancements - so its log is the data check of the port.
 */
@GameTestHolder(Destroy.MOD_ID)
@PrefixGameTestTemplate(false)
public class DestroyGameTests {

    @GameTest(template = "plains_inn") // GameTestHolder adds the destroy: namespace
    public static void placeVatController(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 2, 1);
        helper.setBlock(pos, DestroyBlocks.VAT_CONTROLLER.getDefaultState());
        helper.assertBlockPresent(DestroyBlocks.VAT_CONTROLLER.get(), pos);
        helper.succeed();
    };
};
