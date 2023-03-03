package cn.usshenzhou.madparticle.mixin;

import cn.usshenzhou.madparticle.particle.CustomParticleRegistry;
import cn.usshenzhou.madparticle.particle.MadParticleRenderTypes;
import com.google.common.collect.ImmutableList;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleResource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

@Mixin(ParticleEngine.class)
public class ParticleEngineMixin {
	@Shadow @Final @Mutable
	private static List<ParticleRenderType> RENDER_ORDER;

	@Inject(method = "<init>",at = @At("TAIL"))
	private void injectCustomParticleRenderType(ClientLevel clientLevel, TextureManager textureManager, CallbackInfo ci){
		RENDER_ORDER = ImmutableList.<ParticleRenderType>builder()
				.addAll(RENDER_ORDER)
				.add(MadParticleRenderTypes.values())
				.build();
	}

	@Redirect(method = "loadParticleDescription", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/packs/resources/ResourceManager;getResource(Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/server/packs/resources/Resource;"))
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
	}

}
