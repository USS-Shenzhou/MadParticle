package cn.ussshenzhou.madparticle.mixin;

import cn.ussshenzhou.madparticle.particle.MadParticleRenderTypes;
import com.mojang.blaze3d.vertex.BufferBuilder;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.texture.TextureManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * @author zomb-676
 */
@Mixin(value = ParticleEngine.class)
public class ParticleEngineMixin {

    @Redirect(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;Lnet/minecraft/client/renderer/LightTexture;Lnet/minecraft/client/Camera;FLnet/minecraft/client/renderer/culling/Frustum;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/particle/ParticleRenderType;begin(Lcom/mojang/blaze3d/vertex/BufferBuilder;Lnet/minecraft/client/renderer/texture/TextureManager;)V"))
    private void a(ParticleRenderType instance, BufferBuilder bufferBuilder, TextureManager textureManager) {
        if (instance instanceof MadParticleRenderTypes madParticleRenderType) {
            instance.begin(madParticleRenderType.bufferBuilder, textureManager);
        }else {
            instance.begin(bufferBuilder,textureManager);
        }
    }

}
