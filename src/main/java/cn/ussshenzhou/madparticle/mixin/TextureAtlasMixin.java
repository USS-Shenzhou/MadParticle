package cn.ussshenzhou.madparticle.mixin;

import cn.ussshenzhou.madparticle.particle.CustomParticleRegistry;
import com.mojang.logging.LogUtils;
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
    private Resource madparticleCustomTexture(ResourceManager manager, ResourceLocation parsedLocation) throws IOException {
        ResourceLocation originalLocation = new ResourceLocation(parsedLocation.getNamespace(), parsedLocation.getPath().replace("textures/", "").replace(".png", ""));
        if (originalLocation.getNamespace().contains("mad")) {
            LogUtils.getLogger().error("{} {}", originalLocation, parsedLocation);
            LogUtils.getLogger().error("{}",CustomParticleRegistry.getSpriteLocations().keySet());
        }
        if (CustomParticleRegistry.getSpriteLocations().containsKey(originalLocation)) {
            LogUtils.getLogger().error(originalLocation.toString());
            FileInputStream fileInputStream = new FileInputStream(CustomParticleRegistry.getSpriteLocations().get(originalLocation));
            return new SimpleResource("Mod Resource", originalLocation, fileInputStream, null);
        } else {
            return manager.getResource(parsedLocation);
        }
    }
}
