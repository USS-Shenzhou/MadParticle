package cn.ussshenzhou.madparticle.mixin;

import cn.ussshenzhou.madparticle.MadParticle;
import cn.ussshenzhou.madparticle.particle.CustomParticleRegistry;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleResource;
import net.minecraft.util.profiling.ProfilerFiller;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * @author USS_Shenzhou
 */
@Mixin(ParticleEngine.class)
public abstract class ParticleEngineMixin {

    @Shadow
    @Final
    private Map<ResourceLocation, ParticleEngine.MutableSpriteSet> spriteSets;

    @Shadow
    public abstract void add(Particle pEffect);

    @Redirect(method = "loadParticleDescription", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/packs/resources/ResourceManager;getResource(Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/server/packs/resources/Resource;"))
    private Resource madParticleFakeResource(ResourceManager manager, ResourceLocation jsonLocation) throws IOException {
        ResourceLocation original = new ResourceLocation(jsonLocation.getNamespace(),
                jsonLocation.getPath().replace("particles/", "").replace(".json", ""));
        if (CustomParticleRegistry.getCustomParticleRes().contains(original)) {
            String s = """
                    {
                      "textures": [
                      ]
                    }
                    """;
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(s.getBytes());
            return new SimpleResource(
                    "Mod Resources",
                    jsonLocation,
                    byteArrayInputStream,
                    null
            );
        } else {
            return manager.getResource(jsonLocation);
        }
    }

    @Inject(method = "reload", at = @At("RETURN"))
    private void madparticleLoadCustomTextures(PreparableReloadListener.PreparationBarrier pStage, ResourceManager pResourceManager, ProfilerFiller pPreparationsProfiler, ProfilerFiller pReloadProfiler, Executor pBackgroundExecutor, Executor pGameExecutor, CallbackInfoReturnable<CompletableFuture<Void>> cir) {
        File particleDir = new File(Minecraft.getInstance().gameDirectory, "customparticles");
        CustomParticleRegistry.getCustomParticleTypes().forEach(type -> {
            ResourceLocation regName = type.getRegistryName();
            String prefix = getFileNamePrefix(regName);
            File[] files = particleDir.listFiles((dir, name) -> {
                String s = name.toLowerCase();
                return s.endsWith(".png") && s.startsWith(prefix);
            });
            if (files == null) {
                return;
            }
            if (files.length > 1) {
                for (int i = 0; i < files.length; i++) {
                    String name = prefix + "#" + i + ".png";
                    Optional<File> optional = Arrays.stream(files).filter(file -> file.getName().contains(name)).findFirst();
                    if (optional.isEmpty()) {
                        LogUtils.getLogger().error("Failed to find {}. Check if it is a valid name.", name);
                        continue;
                    }
                    try (FileInputStream fileInputStream = new FileInputStream(optional.get())) {
                        NativeImage nativeImage = NativeImage.read(fileInputStream);
                        ResourceLocation spriteLocation = new ResourceLocation(regName.getNamespace(), regName.getPath() + "_" + i);
                        TextureAtlasSprite sprite = new TextureAtlasSprite(
                                new TextureAtlas(spriteLocation),
                                new TextureAtlasSprite.Info(spriteLocation, nativeImage.getWidth(), nativeImage.getHeight(), AnimationMetadataSection.EMPTY),
                                0,
                                256, 256, 240, 240,
                                nativeImage
                        );
                        ((TextureManagerAccessor) Minecraft.getInstance().getTextureManager()).getByPath().put(spriteLocation, sprite.atlas());
                    } catch (IOException ignored) {
                        LogUtils.getLogger().error("Failed to find {}. Check if it is a valid name.", name);
                        continue;
                    }
                    //TODO
                }
            } else {
                String name = prefix + ".png";
                Optional<File> optional = Arrays.stream(files).filter(file -> file.getName().contains(name)).findFirst();
                if (optional.isEmpty()) {
                    LogUtils.getLogger().error("Failed to find {}. Check if it is a valid name.", name);
                    return;
                }
                try (FileInputStream fileInputStream = new FileInputStream(optional.get())) {
                    NativeImage nativeImage = NativeImage.read(fileInputStream);
                    ResourceLocation spriteLocation = new ResourceLocation(regName.getNamespace(), regName.getPath());
                    TextureAtlasSprite sprite = new TextureAtlasSprite(
                            new TextureAtlas(spriteLocation),
                            new TextureAtlasSprite.Info(spriteLocation, nativeImage.getWidth(), nativeImage.getHeight(), AnimationMetadataSection.EMPTY),
                            0,
                            1, 1, 0, 0,
                            nativeImage
                    );
                    ((TextureManagerAccessor) Minecraft.getInstance().getTextureManager()).getByPath().put(spriteLocation, sprite.atlas());
                    ParticleEngine.MutableSpriteSet spriteSet = new ParticleEngine.MutableSpriteSet();
                    spriteSet.rebind(List.of(sprite));
                    spriteSets.put(type.getRegistryName(), spriteSet);
                    CustomParticleRegistry.getSpriteLocations().put(spriteLocation, optional.get());
                } catch (IOException ignored) {
                    LogUtils.getLogger().error("Failed to find {}. Check if it is a valid name.", name);
                    return;
                }
            }
        });
    }

    private static String getFileNamePrefix(ResourceLocation resourceLocation) {
        if (MadParticle.MOD_ID.equals(resourceLocation.getNamespace())) {
            return resourceLocation.getPath();
        } else {
            return resourceLocation.getNamespace() + "~" + resourceLocation.getPath();
        }
    }
}
