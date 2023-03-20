package cn.ussshenzhou.madparticle.mixin;

import net.minecraft.client.particle.ParticleEngine;
import org.spongepowered.asm.mixin.Mixin;

/**
 * @author USS_Shenzhou
 */
@Mixin(ParticleEngine.class)
public abstract class ParticleEngineMixin {

    /*@Redirect(method = "loadParticleDescription", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/packs/resources/ResourceManager;getResource(Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/server/packs/resources/Resource;"))
    private Resource madParticleFakeResource(ResourceManager manager, ResourceLocation jsonLocation) throws IOException {
        ResourceLocation original = new ResourceLocation(jsonLocation.getNamespace(),
                jsonLocation.getPath().replace("particles/", "").replace(".json", ""));
        if (CustomParticleRegistry.CUSTOM_PARTICLES_TYPE_NAMES_AND_TEXTURES.containsKey(original)) {
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
            return new SimpleResource(
                    "Mod Resources",
                    jsonLocation,
                    byteArrayInputStream,
                    null
            );
        } else {
            return manager.getResource(jsonLocation);
        }
    }*/
}
