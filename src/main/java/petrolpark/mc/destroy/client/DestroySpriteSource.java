package petrolpark.mc.destroy.client;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.IntUnaryOperator;
import java.util.function.Supplier;

import com.google.common.base.Suppliers;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.NativeImage.Format;
import com.mojang.serialization.MapCodec;
import petrolpark.mc.destroy.Destroy;
import net.createmod.catnip.lang.Lang;

import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.atlas.SpriteResourceLoader;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.client.renderer.texture.atlas.SpriteSourceType;
import net.minecraft.client.renderer.texture.atlas.sources.LazyLoadedImage;
import net.minecraft.client.renderer.texture.atlas.sources.PalettedPermutations;
import net.minecraft.client.resources.metadata.animation.FrameSize;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceMetadata;
import net.minecraft.util.GsonHelper;
import net.neoforged.neoforge.client.event.RegisterSpriteSourceTypesEvent;

public class DestroySpriteSource implements SpriteSource {

    public static Set<String> UNIVERSAL_ARMOR_TRIMS = Set.of();

    private static final Gson GSON = new Gson();

    private final Atlas atlas;

    private static enum Atlas {
        BLOCKS,
        ARMOR_TRIMS;

        private final MapCodec<DestroySpriteSource> codec;
        private SpriteSourceType type; // Set by RegisterSpriteSourceTypesEvent (vanilla's SpriteSources.register only takes minecraft: ids in 1.21)

        Atlas() {
            codec = MapCodec.unit(() -> new DestroySpriteSource(this));
        };
    };

    public DestroySpriteSource(Atlas atlas) {
        this.atlas = atlas;
    };

    @Override
    public void run(ResourceManager resourceManager, Output output) {
        Destroy.LOGGER.info("Supplying custom Destroy sprites to "+atlas.name());

        // ARMOR TRIMS

        Supplier<int[]> paletteKeySupplier = Suppliers.memoize(() -> {
            return PalettedPermutations.loadPaletteEntryFromImage(resourceManager, ResourceLocation.fromNamespaceAndPath("minecraft", "trims/color_palettes/trim_palette"));
        });

        // Find all trim Materials which we want to indiscriminately generate textures for
        Map<String, Supplier<IntUnaryOperator>> trimMaterials = new HashMap<>();
        for (String namespace : resourceManager.getNamespaces()) {
            ResourceLocation location = ResourceLocation.fromNamespaceAndPath(namespace, "textures/trims/universal_trim_materials.json");
            Optional<Resource> resource = resourceManager.getResource(location);
            if (resource.isEmpty()) continue;
            try (InputStream inputStream = resource.get().open()) {
                JsonObject jsonObject = GSON.fromJson(new InputStreamReader(inputStream, StandardCharsets.UTF_8), JsonObject.class);
                jsonObject.get("values").getAsJsonObject().entrySet().forEach(entry -> {
                    ResourceLocation textureId = ResourceLocation.parse(entry.getValue().getAsString());
                    trimMaterials.put(entry.getKey(), Suppliers.memoize(() -> PalettedPermutations.createPaletteMapping(paletteKeySupplier.get(), PalettedPermutations.loadPaletteEntryFromImage(resourceManager, textureId))));
                });
            } catch (IOException e) {
                Destroy.LOGGER.error("Failed to read universal trim material file: ", e);
            };
        };

        UNIVERSAL_ARMOR_TRIMS = Set.of(trimMaterials.keySet().toArray(i -> new String[i])); // Set this as it gets referenced by model generators

        // Get the textures which the trim material palettes should be applied to
        Map<ResourceLocation, Resource> trimTextures = new HashMap<>();
        if (atlas == Atlas.BLOCKS) {
            trimTextures = resourceManager.listResources("textures/trims/items", rl -> true);
        } else if (atlas == Atlas.ARMOR_TRIMS) {
            trimTextures = resourceManager.listResources("textures/trims/models/armor", rl -> true);
        };

        // Apply the palettes to the trim textures and add them to the Texture Atlas
        for (Entry<ResourceLocation, Resource> entry : trimTextures.entrySet()) {
            ResourceLocation file = entry.getKey();
            ResourceLocation id = TEXTURE_ID_CONVERTER.fileToId(file);

            LazyLoadedImage lazyloadedimage = new LazyLoadedImage(file, entry.getValue(), trimMaterials.size());

            for(Map.Entry<String, Supplier<IntUnaryOperator>> trimMaterial : trimMaterials.entrySet()) {
                ResourceLocation permutationLocation = id.withSuffix("_" + trimMaterial.getKey());
                output.add(permutationLocation, new PalettedSpriteSupplier(lazyloadedimage, trimMaterial.getValue(), permutationLocation));
            };
        };

        // CIRCUIT MASK ITEMS

        for (Entry<ResourceLocation, Resource> entry : new FileToIdConverter("textures/item/circuit_pattern", ".png").listMatchingResources(resourceManager).entrySet()) {
            ResourceLocation file = entry.getKey();
            ResourceLocation id = TEXTURE_ID_CONVERTER.fileToId(file);

            int xStart = 3;
            int yStart = 3;
            int tileWidth = 2;
            int tileHeight = 2;

            // Gather the information on how to split up the texture
            ResourceLocation metaFile = ResourceLocation.fromNamespaceAndPath(file.getNamespace(), file.getPath() + ".mcmeta");
            Optional<Resource> metaFileOpened = resourceManager.getResource(metaFile);
            if (metaFileOpened.isPresent()) {
                try (Reader reader = resourceManager.getResource(metaFile).get().openAsReader()) {
                    JsonObject object = GsonHelper.fromJson(GSON, reader, JsonElement.class).getAsJsonObject();
                    if (object.has("tiles_start_x")) xStart = object.get("tiles_start_x").getAsInt();
                    if (object.has("tiles_start_y")) yStart = object.get("tiles_start_y").getAsInt();
                    if (object.has("tiles_width")) tileWidth = object.get("tile_width").getAsInt();
                    if (object.has("tiles_height")) tileHeight = object.get("tile_height").getAsInt();
                } catch (JsonSyntaxException | IOException e) {
                    Destroy.LOGGER.error("Failed to read .mcmeta file for Circuit Pattern texture: "+file, e);
                    continue;
                };
            } else {
                Destroy.LOGGER.error("No .mcmeta file for Circuit Pattern texture: "+file, new FileNotFoundException());
                continue;
            };

            // Generate each texture and put it in the atlas
            try (NativeImage image = new LazyLoadedImage(file, entry.getValue(), trimMaterials.size()).get()) {
                for (int x = 0; x < 4; x++) {
                    for (int y = 0; y < 4; y++) {
                        NativeImage partialImage = new NativeImage(Format.RGBA, image.getWidth(), image.getHeight(), false); // Another (usually 16x16) texture containing only a single tile
                        int xPos = xStart + x * tileWidth;
                        int yPos = yStart + y * tileHeight;
                        image.copyRect(partialImage, xPos, yPos, xPos, yPos, tileWidth, tileHeight, false, false);
                        ResourceLocation sectionId = ResourceLocation.fromNamespaceAndPath(id.getNamespace(), id.getPath() + "/" + ((y * 4) + x));
                        output.add(sectionId, loader -> new SpriteContents(sectionId, new FrameSize(image.getWidth(), image.getHeight()), partialImage, ResourceMetadata.EMPTY));
                    };
                };
            } catch (IOException e) {
                Destroy.LOGGER.error("Failed to open Circuit Pattern texture: "+ file, e);
                continue;
            };
        };
        

    };

    @Override
    public SpriteSourceType type() {
        return atlas.type;
    };

    /**
     * Registers {@code destroy:blocks} and {@code destroy:armor_trims} (referenced by {@code assets/minecraft/atlases}).
     */
    public static void register(RegisterSpriteSourceTypesEvent event) {
        for (Atlas atlas : Atlas.values()) atlas.type = event.register(Destroy.asResource(Lang.asId(atlas.name())), atlas.codec);
    };


    /**
     * Copied from the {@link net.minecraft.client.renderer.texture.atlas.sources.PalettedPermutations.PalettedSpriteSupplier Minecraft source code}.
     */
    public static record PalettedSpriteSupplier(LazyLoadedImage baseImage, Supplier<IntUnaryOperator> palette, ResourceLocation permutationLocation) implements SpriteSource.SpriteSupplier {

        @Override
        public SpriteContents apply(SpriteResourceLoader loader) {
            try {
                NativeImage nativeimage = baseImage.get().mappedCopy(this.palette.get());
                return new SpriteContents(permutationLocation, new FrameSize(nativeimage.getWidth(), nativeimage.getHeight()), nativeimage, ResourceMetadata.EMPTY);
            } catch (IllegalArgumentException | IOException ioexception) {
                Destroy.LOGGER.error("Unable to apply palette to {}", permutationLocation, ioexception);
            } finally {
                baseImage.release();
            };
            return null;
        };

        @Override
        public void discard() {
            baseImage.release();
        };
    }
    
};
