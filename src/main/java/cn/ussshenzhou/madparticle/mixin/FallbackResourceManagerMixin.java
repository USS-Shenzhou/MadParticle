package cn.ussshenzhou.madparticle.mixin;

import cn.ussshenzhou.madparticle.particle.CustomParticleRegistry;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.resources.FallbackResourceManager;
import net.minecraft.server.packs.resources.Resource;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * @author USS_Shenzhou
 */
@Mixin(FallbackResourceManager.class)
public class FallbackResourceManagerMixin {

    @Shadow
    @Final
    public List<FallbackResourceManager.PackEntry> fallbacks;

    @Inject(method = "listResources", at = @At("RETURN"))
    private void madParticleFakeResourceJson(String pPath, Predicate<ResourceLocation> pFilter, CallbackInfoReturnable<Map<ResourceLocation, Resource>> cir) {
        if (!"particles".equals(pPath)) {
            return;
        }
        Map<ResourceLocation, Resource> map = cir.getReturnValue();
        PackResources resources = fallbacks.get(0).resources();
        CustomParticleRegistry.CUSTOM_PARTICLE_TYPES.forEach(particleType -> {
            ResourceLocation original = ForgeRegistries.PARTICLE_TYPES.getKey(particleType);
            String s = String.format(
                    """
                            {
                              "textures": [
                              %s
                              ]
                            }
                            """,
                    CustomParticleRegistry.listToJsonString(CustomParticleRegistry.CUSTOM_PARTICLES_TYPE_NAMES_AND_TEXTURES.get(original))
            );
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(s.getBytes());
            ResourceLocation jsonLocation = new ResourceLocation(original.getNamespace(), "particles/" + original.getPath() + ".json");
            map.put(jsonLocation, new Resource(resources, () -> byteArrayInputStream));
        });
    }

    @Inject(method = "listResources", at = @At("RETURN"))
    private void madParticleCustomTexture(String pPath, Predicate<ResourceLocation> pFilter, CallbackInfoReturnable<Map<ResourceLocation, Resource>> cir) {
        if (!"textures/particle".equals(pPath)) {
            return;
        }
        Map<ResourceLocation, Resource> map = cir.getReturnValue();
        PackResources resources = fallbacks.get(0).resources();
        CustomParticleRegistry.ALL_TEXTURES.forEach(textureResourceLocation -> {
            try {
                //noinspection resource
                FileInputStream fileInputStream = new FileInputStream(CustomParticleRegistry.textureResLocToFile(textureResourceLocation));
                ResourceLocation pngLocation = new ResourceLocation(textureResourceLocation.getNamespace(), "textures/particle/" + textureResourceLocation.getPath() + ".png");
                map.put(pngLocation, new Resource(resources, () -> fileInputStream));
            } catch (IOException ignored) {
                LogUtils.getLogger().error("Failed to find file of texture {}. Check if it is a valid name.", textureResourceLocation);
            }
        });
    }
}
