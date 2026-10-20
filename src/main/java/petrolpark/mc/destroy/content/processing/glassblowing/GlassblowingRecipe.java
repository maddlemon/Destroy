package petrolpark.mc.destroy.content.processing.glassblowing;

import petrolpark.mc.library.compat.create.core.data.recipe.AdvancedProcessingRecipeParams;
import petrolpark.mc.library.compat.create.core.data.recipe.AdvancedProcessingRecipe;
import java.util.ArrayList;
import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import petrolpark.mc.destroy.DestroyRecipeTypes;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.wrapper.RecipeWrapper;

public class GlassblowingRecipe extends AdvancedProcessingRecipe<RecipeWrapper> {

    public List<BlowingShape> blowingShapes;

    public GlassblowingRecipe(AdvancedProcessingRecipeParams params) {
        super(DestroyRecipeTypes.GLASSBLOWING, params);
        blowingShapes = new ArrayList<>();
    };

    @Override
    public boolean matches(RecipeWrapper pContainer, Level pLevel) {
        return true;
    };

    @Override
    protected int getMaxFluidInputCount() {
        return 1;
    };

    @Override
    protected int getMaxInputCount() {
        return 0;
    };

    @Override
    protected int getMaxOutputCount() {
        return 1;
    };

    public static record BlowingShape(float length, float radius) {

        /**
         * In JSON a shape component is {@code [length, radius]} in pixels (sixteenths of a block), as in 1.20.1.
         */
        public static final Codec<BlowingShape> CODEC = Codec.FLOAT.listOf().comapFlatMap(
            list -> list.size() == 2 ? DataResult.success(new BlowingShape(list.get(0) / 16f, list.get(1) / 16f)) : DataResult.error(() -> "Each entry in the shape must specify two values: length and radius"),
            shape -> List.of(shape.length() * 16f, shape.radius() * 16f)
        );

        public static final StreamCodec<RegistryFriendlyByteBuf, BlowingShape> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT, BlowingShape::length,
            ByteBufCodecs.FLOAT, BlowingShape::radius,
            BlowingShape::new
        );
    };

    /**
     * The Petrolpark Library's processing recipe serializer plus the optional {@code "shape"} list (was {@code readAdditional}/{@code writeAdditional} in 1.20.1).
     */
    public static class Serializer implements RecipeSerializer<GlassblowingRecipe> {

        private final MapCodec<GlassblowingRecipe> codec;
        private final StreamCodec<RegistryFriendlyByteBuf, GlassblowingRecipe> streamCodec;

        public Serializer() {
            AdvancedProcessingRecipe.Serializer<GlassblowingRecipe> base = new AdvancedProcessingRecipe.Serializer<>(GlassblowingRecipe::new);
            codec = RecordCodecBuilder.mapCodec(instance -> instance.group(
                base.codec().forGetter(recipe -> recipe),
                BlowingShape.CODEC.listOf().optionalFieldOf("shape", List.of()).forGetter(recipe -> recipe.blowingShapes)
            ).apply(instance, GlassblowingRecipe::withShapes));
            streamCodec = StreamCodec.composite(
                base.streamCodec(), recipe -> recipe,
                BlowingShape.STREAM_CODEC.apply(ByteBufCodecs.list()), recipe -> recipe.blowingShapes,
                GlassblowingRecipe::withShapes
            );
        };

        @Override
        public MapCodec<GlassblowingRecipe> codec() {
            return codec;
        };

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, GlassblowingRecipe> streamCodec() {
            return streamCodec;
        };
    };

    private static GlassblowingRecipe withShapes(GlassblowingRecipe recipe, List<BlowingShape> shapes) {
        recipe.blowingShapes = new ArrayList<>(shapes);
        return recipe;
    };

};
