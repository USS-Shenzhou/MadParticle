package cn.usshenzhou.madparticle.mixin;

import cn.usshenzhou.madparticle.particle.MadParticleShader;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.server.packs.resources.ResourceManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
	@Shadow
	@Final
	private Map<String, ShaderInstance> shaders;

	/**
	 * Replacement for RegisterShadersEvent, as fabric has no equivalent event
	 */
	@Inject(method = "reloadShaders", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;shutdownShaders()V", shift = At.Shift.AFTER))
	private void reloadShaders(ResourceManager resourceManager, CallbackInfo ci) {
		this.setupShaderMp(MadParticleShader::registerShader, resourceManager);
	}

	private void setupShaderMp(Function<ResourceManager, Pair<ShaderInstance, Consumer<ShaderInstance>>> function, ResourceManager manager) {
		var shader = function.apply(manager);
		this.shaders.put(shader.getFirst().getName(), shader.getFirst());
		shader.getSecond().accept(shader.getFirst());
		//shader.getFirst().close();
	}
}