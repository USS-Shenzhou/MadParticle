package cn.ussshenzhou.madparticle.mixin;

import cn.ussshenzhou.madparticle.MultiThreadedEqualObjectLinkedOpenHashSetQueue;
import cn.ussshenzhou.madparticle.mixinproxy.ParticleEngineMixinProxy;
import cn.ussshenzhou.madparticle.particle.*;
import cn.ussshenzhou.madparticle.particle.optimize.NeoInstancedRenderManager;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.texture.TextureManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.Predicate;

/**
 * @author USS_Shenzhou
 */
@Mixin(ParticleEngine.class)
public class ParticleEngineMixin {

    @Shadow
    public Queue<Particle> particlesToAdd;

    @Inject(method = "<init>", at = @At(value = "TAIL"))
    private void madparticleMakeBufferAsync(ClientLevel pLevel, TextureManager pTextureManager, CallbackInfo ci) {
        particlesToAdd = new ConcurrentLinkedDeque<>();
    }

    @WrapWithCondition(
            method = "render(Lnet/minecraft/client/Camera;FLnet/minecraft/client/renderer/MultiBufferSource$BufferSource;Lnet/minecraft/client/renderer/culling/Frustum;Ljava/util/function/Predicate;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/particle/ParticleEngine;renderParticleType(Lnet/minecraft/client/Camera;FLnet/minecraft/client/renderer/MultiBufferSource$BufferSource;Lnet/minecraft/client/particle/ParticleRenderType;Ljava/util/Queue;Lnet/minecraft/client/renderer/culling/Frustum;)V")
    )
    private boolean madparticleSkipInstancedRender(Camera camera, float partialTick, MultiBufferSource.BufferSource bufferSource, ParticleRenderType particleType, Queue<Particle> particles, @Nullable Frustum frustum) {
        return particleType != ModParticleRenderTypes.INSTANCED && particleType != ModParticleRenderTypes.INSTANCED_TERRAIN;
    }

    @Inject(method = "render(Lnet/minecraft/client/Camera;FLnet/minecraft/client/renderer/MultiBufferSource$BufferSource;Lnet/minecraft/client/renderer/culling/Frustum;Ljava/util/function/Predicate;)V",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/particle/ParticleEngine;renderParticleType(Lnet/minecraft/client/Camera;FLnet/minecraft/client/renderer/MultiBufferSource$BufferSource;Lnet/minecraft/client/particle/ParticleRenderType;Ljava/util/Queue;Lnet/minecraft/client/renderer/culling/Frustum;)V",
                    //needcheck
                    shift = At.Shift.AFTER
            )
    )
    private void madparticleRenderInstanced(Camera camera, float partialTick, MultiBufferSource.BufferSource bufferSource, Frustum frustum, Predicate<ParticleRenderType> renderTypePredicate, CallbackInfo ci,
                                            @Local ParticleRenderType particlerendertype, @Local Queue<Particle> queue) {
        if (particlerendertype == ModParticleRenderTypes.INSTANCED || particlerendertype == ModParticleRenderTypes.INSTANCED_TERRAIN) {
            NeoInstancedRenderManager.getInstance(particlerendertype).render((MultiThreadedEqualObjectLinkedOpenHashSetQueue<Particle>) queue);
        }
    }

    /**
     * @author USS_Shenzhou
     * @reason MadParticle has done so many things to this method that overwriting would be better than a bunch of injects.
     */
    @Overwrite
    public void tick() {
        ParticleEngineMixinProxy.tick((ParticleEngine) (Object) this);
    }
}
