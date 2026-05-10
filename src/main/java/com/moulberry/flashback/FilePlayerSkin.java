package com.moulberry.flashback;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.ResourceLocation;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

public class FilePlayerSkin {

    private static class CleanState implements Runnable {
        private ResourceLocation skinResourceLocation;

        public CleanState(ResourceLocation skinResourceLocation) {
            this.skinResourceLocation = skinResourceLocation;
        }

        @Override
        public void run() {
            if (this.skinResourceLocation != null) {
                ResourceLocation toClean = this.skinResourceLocation;
                Minecraft.getInstance().execute(() -> {
                    Flashback.LOGGER.info("Cleaning player skin {} because it's no longer in use!", toClean);
                    Minecraft.getInstance().getTextureManager().release(toClean);
                });
                this.skinResourceLocation = null;
            }
        }
    }

    private transient ResourceLocation skinTexture = null;
    private transient String modelName = null;
    private final String pathToSkin;

    public FilePlayerSkin(String pathToSkin) {
        this.pathToSkin = pathToSkin;
    }

    public ResourceLocation getTextureLocation() {
        if (this.skinTexture != null) {
            return this.skinTexture;
        }

        Path path = Path.of(this.pathToSkin);
        try (InputStream inputStream = Files.newInputStream(path)) {
            NativeImage nativeImage = NativeImage.read(inputStream);

            int w = nativeImage.getWidth();
            int h = nativeImage.getHeight();

            // We determine the type using the alpha of the pixel at 54, 20
            int argb = nativeImage.getPixelRGBA(54 * w / 64, 20 * h / 64);
            String model = "default";
            if (((argb >> 24) & 0xFF) < 20) {
                model = "slim";
            }

            DynamicTexture dynamicTexture = new DynamicTexture(nativeImage);

            ResourceLocation resourceLocation = Flashback.createResourceLocation("skin_from_file/" + UUID.randomUUID());
            Minecraft.getInstance().getTextureManager().register(resourceLocation, dynamicTexture);
            GlobalCleaner.INSTANCE.register(this, new CleanState(resourceLocation));

            this.skinTexture = resourceLocation;
            this.modelName = model;
        } catch (Exception e) {
            Flashback.LOGGER.error("Unable to load skin from file", e);
            UUID uuid = UUID.randomUUID();
            this.skinTexture = DefaultPlayerSkin.getDefaultSkin(uuid);
            this.modelName = DefaultPlayerSkin.getSkinModelName(uuid);
        }

        return this.skinTexture;
    }

    public String getModelName() {
        if (this.modelName == null) {
            this.getTextureLocation();
        }
        return this.modelName != null ? this.modelName : "default";
    }

}
