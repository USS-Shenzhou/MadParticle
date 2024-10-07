package cn.ussshenzhou.madparticle.mixin;

import cn.ussshenzhou.madparticle.MultiThreadedEqualLinkedHashSetsQueue;
import cn.ussshenzhou.madparticle.MadParticleConfig;
import cn.ussshenzhou.madparticle.particle.*;
import cn.ussshenzhou.madparticle.particle.enums.TakeOver;
import cn.ussshenzhou.madparticle.particle.optimize.InstancedRenderManager;
import cn.ussshenzhou.madparticle.particle.optimize.ParallelTickManager;
import cn.ussshenzhou.t88.config.ConfigHelper;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.texture.TextureManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.Function;
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

    @ModifyArg(method = "tick", at = @At(value = "INVOKE", target = "Ljava/util/Map;computeIfAbsent(Ljava/lang/Object;Ljava/util/function/Function;)Ljava/lang/Object;"), index = 1)
    private Function<ParticleRenderType, Queue<Particle>> madparticleUseEvictingLinkedHashSetQueueInsteadOfEvictingQueue(Function<ParticleRenderType, Queue<Particle>> mappingFunction) {
        return t -> new MultiThreadedEqualLinkedHashSetsQueue<>(16384, ConfigHelper.getConfigRead(MadParticleConfig.class).maxParticleAmountOfSingleQueue);
    }

    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/particle/Particle;getRenderType()Lnet/minecraft/client/particle/ParticleRenderType;"))
    private ParticleRenderType madparticleTakeoverRenderType(Particle instance) {
        return TakeOver.check(instance);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void madparticleClearToAdd(CallbackInfo ci) {
        particlesToAdd.clear();
    }

    @Redirect(method = "render(Lnet/minecraft/client/renderer/LightTexture;Lnet/minecraft/client/Camera;FLnet/minecraft/client/renderer/culling/Frustum;Ljava/util/function/Predicate;)V",
            at = @At(value = "INVOKE", target = "Ljava/util/Iterator;next()Ljava/lang/Object;", ordinal = 0),
            remap = false
    )
    private Object madparticleCancelInstancedRenderTypeRender(Iterator<ParticleRenderType> iterator) {
        var r = iterator.next();
        if (r == ModParticleRenderTypes.INSTANCED) {
            return ParticleRenderType.NO_RENDER;
        }
        return r;
    }

    @Shadow
    @Final
    private TextureManager textureManager;

    @Inject(method = "render(Lnet/minecraft/client/renderer/LightTexture;Lnet/minecraft/client/Camera;FLnet/minecraft/client/renderer/culling/Frustum;Ljava/util/function/Predicate;)V",
            at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;depthMask(Z)V", shift = At.Shift.BEFORE)
    )
    private void madparticleRenderInstanced(LightTexture lightTexture, Camera camera, float partialTick, Frustum frustum, Predicate<ParticleRenderType> renderTypePredicate, CallbackInfo ci) {
        if (renderTypePredicate.test(ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT)) {
            InstancedRenderManager.render(camera, partialTick, frustum, textureManager);
        }
    }

    @Shadow
    @Final
    private Map<ParticleRenderType, Queue<Particle>> particles;

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Ljava/util/Queue;add(Ljava/lang/Object;)Z"), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void madparticleAddToInstancedRenderManager(CallbackInfo ci, Particle particle) {
        if (particle instanceof TextureSheetParticle p && TakeOver.check(p) == ModParticleRenderTypes.INSTANCED) {
            var queue = (MultiThreadedEqualLinkedHashSetsQueue<Particle>) particles.get(ModParticleRenderTypes.INSTANCED);
            if (queue.remainingCapacity() == 0) {
                InstancedRenderManager.remove((TextureSheetParticle) queue.peek());
            }
            InstancedRenderManager.add(p);
        }
    }

    @Inject(method = "tickParticleList", at = @At(value = "INVOKE", target = "Ljava/util/Iterator;remove()V", shift = At.Shift.AFTER))
    private void madparticleRemoveFromInstancedRenderManager(Collection<Particle> pParticles, CallbackInfo ci, @Local Particle particle) {
        if (particle instanceof TextureSheetParticle p) {
            InstancedRenderManager.remove(p);
        }
    }

    @Inject(method = "tickParticleList", at = @At("HEAD"), cancellable = true)
    private void madparticleTakeOverTick(Collection<Particle> particles, CallbackInfo ci) {
        var config = ConfigHelper.getConfigRead(MadParticleConfig.class);
        if (config.takeOverTicking != TakeOver.NONE && config.getBufferFillerThreads() > 1) {
            ParallelTickManager.tickList(particles);
            ci.cancel();
        } else {
            ParallelTickManager.clearCount();
        }
    }
}
