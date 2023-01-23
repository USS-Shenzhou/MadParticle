package cn.usshenzhou.madparticle.mixin;

import cn.usshenzhou.madparticle.particle.MadParticleRenderTypes;
import com.google.common.collect.ImmutableList;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.texture.TextureManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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

}
