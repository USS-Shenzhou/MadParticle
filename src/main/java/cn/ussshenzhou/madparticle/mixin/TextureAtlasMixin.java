package cn.ussshenzhou.madparticle.mixin;

import cn.ussshenzhou.madparticle.particle.CustomParticleRegistry;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleResource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.io.FileInputStream;
import java.io.IOException;

/**
 * @author USS_Shenzhou
 */
@Mixin(TextureAtlas.class)
public class TextureAtlasMixin {


    @Redirect(method = "lambda$getBasicSpriteInfos$2", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/packs/resources/ResourceManager;getResource(Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/server/packs/resources/Resource;"))
    private Resource madparticleCustomTextureInfo(ResourceManager manager, ResourceLocation parsedLocation) throws IOException {
        return madparticleRedirect(manager, parsedLocation);
    }

    @Redirect(method = "load(Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/client/renderer/texture/TextureAtlasSprite$Info;IIIII)Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;",
    at = @At(value = "INVOKE",target = "Lnet/minecraft/server/packs/resources/ResourceManager;getResource(Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/server/packs/resources/Resource;"))
    private Resource madparticleCustomTextureLoad(ResourceManager manager, ResourceLocation parsedLocation) throws IOException {
        return madparticleRedirect(manager, parsedLocation);
    }

    private Resource madparticleRedirect(ResourceManager manager, ResourceLocation parsedLocation) throws IOException {
        ResourceLocation originalLocation = new ResourceLocation(parsedLocation.getNamespace(),
                parsedLocation.getPath().replace("textures/particle/", "").replace(".png", ""));
        if (CustomParticleRegistry.ALL_TEXTURES.contains(originalLocation)) {
            FileInputStream fileInputStream = new FileInputStream(CustomParticleRegistry.textureResLocToFile(originalLocation));
            return new SimpleResource("Mod Resource", originalLocation, fileInputStream, null);
        } else {
            return manager.getResource(parsedLocation);
        }
    }
}
