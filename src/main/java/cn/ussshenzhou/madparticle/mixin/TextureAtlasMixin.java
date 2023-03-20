package cn.ussshenzhou.madparticle.mixin;

import net.minecraft.client.renderer.texture.TextureAtlas;
import org.spongepowered.asm.mixin.Mixin;

/**
 * @author USS_Shenzhou
 */
@Mixin(TextureAtlas.class)
public class TextureAtlasMixin {

/*
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
    }*/
}
