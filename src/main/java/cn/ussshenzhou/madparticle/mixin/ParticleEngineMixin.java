package cn.ussshenzhou.madparticle.mixin;

import cn.ussshenzhou.madparticle.mixinproxy.ParticleEngineMixinProxy;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleResources;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * @author USS_Shenzhou
 */
@Mixin(ParticleEngine.class)
public class ParticleEngineMixin {

    @Shadow
    public Queue<Particle> particlesToAdd;

    @Inject(method = "<init>", at = @At(value = "TAIL"))
    private void madparticleMakeBufferAsync(ClientLevel level, ParticleResources resourceManager, CallbackInfo ci) {
        particlesToAdd = new ConcurrentLinkedDeque<>();
    }

    //Do not need to manually skip any more since ParticleEngine.RENDER_ORDER
    //@WrapWithCondition(
    //        method = "render(Lnet/minecraft/client/Camera;FLnet/minecraft/client/renderer/MultiBufferSource$BufferSource;Lnet/minecraft/client/renderer/culling/Frustum;Ljava/util/function/Predicate;)V",
    //        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/particle/ParticleEngine;renderParticleType(Lnet/minecraft/client/Camera;FLnet/minecraft/client/renderer/MultiBufferSource$BufferSource;Lnet/minecraft/client/particle/ParticleRenderType;Ljava/util/Queue;Lnet/minecraft/client/renderer/culling/Frustum;)V")
    //)
    //private boolean madparticleSkipInstancedRender(Camera camera, float partialTick, MultiBufferSource.BufferSource bufferSource, ParticleRenderType particleType, Queue<Particle> particles, @Nullable Frustum frustum) {
    //    return particleType != ModParticleRenderTypes.INSTANCED && particleType != ModParticleRenderTypes.INSTANCED_TERRAIN;
    //}

    /**
     * @author USS_Shenzhou
     * @reason MadParticle has done so many things to this method that overwriting would be better than a bunch of injects.
     */
    @Overwrite
    public void tick() {
        ParticleEngineMixinProxy.tick((ParticleEngine) (Object) this);
    }
}
