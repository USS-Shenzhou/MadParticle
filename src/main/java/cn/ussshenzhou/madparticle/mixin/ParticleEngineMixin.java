package cn.ussshenzhou.madparticle.mixin;

import cn.ussshenzhou.madparticle.MadParticleConfig;
import cn.ussshenzhou.madparticle.particle.InstancedRenderManager;
import cn.ussshenzhou.madparticle.particle.MadParticle;
import cn.ussshenzhou.madparticle.particle.ModParticleRenderTypes;
import cn.ussshenzhou.madparticle.particle.TakeOver;
import cn.ussshenzhou.t88.config.ConfigHelper;
import com.google.common.collect.EvictingQueue;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.texture.TextureManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;

/**
 * @author USS_Shenzhou
 */
@Mixin(ParticleEngine.class)
public class ParticleEngineMixin {

    @ModifyConstant(method = "lambda$tick$11(Lnet/minecraft/client/particle/ParticleRenderType;)Ljava/util/Queue;", constant = @Constant(intValue = 16384), require = 0)
    private static int madparticleChangeMaxAmount(int constant) {
        return madparticleMaxAmount();
    }

    @ModifyConstant(method = "lambda$tick$11(Lnet/minecraft/client/particle/ParticleRenderType;)Ljava/util/Queue;", constant = @Constant(intValue = 16384), remap = false, require = 0, expect = 0)
    private static int madparticleChangeMaxAmountOptifineCompatibility(int constant) {
        return madparticleMaxAmount();
    }

    private static int madparticleMaxAmount() {
        return ConfigHelper.getConfigRead(MadParticleConfig.class).maxParticleAmountOfSingleQueue;
    }

    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/particle/Particle;getRenderType()Lnet/minecraft/client/particle/ParticleRenderType;"))
    private ParticleRenderType madparticleTakeoverRenderType(Particle instance) {
        return TakeOver.check(instance);
    }

    @Redirect(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;Lnet/minecraft/client/renderer/LightTexture;Lnet/minecraft/client/Camera;FLnet/minecraft/client/renderer/culling/Frustum;)V",
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

    @SuppressWarnings("JavaReflectionMemberAccess")
    @Inject(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;Lnet/minecraft/client/renderer/LightTexture;Lnet/minecraft/client/Camera;FLnet/minecraft/client/renderer/culling/Frustum;)V",
            at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;popPose()V")
    )
    private void madparticleRenderInstanced(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, LightTexture lightTexture, Camera camera, float partialTicks, Frustum clippingHelper, CallbackInfo ci) {
        try {
            //if Iris exist
            Class<?> thisClass = ParticleEngine.class;
            Field irisParticleRenderingPhase = thisClass.getDeclaredField("phase");
            irisParticleRenderingPhase.setAccessible(true);
            Method getPhaseName = irisParticleRenderingPhase.getClass().getMethod("name");
            String phaseName = (String) getPhaseName.invoke(irisParticleRenderingPhase);
            if ("TRANSLUCENT".equals(phaseName)) {
                InstancedRenderManager.render(poseStack, bufferSource, lightTexture, camera, partialTicks, clippingHelper, textureManager);
            }
        } catch (NoSuchFieldException | NoSuchMethodException | IllegalAccessException |
                 InvocationTargetException ignored) {
            //vanilla
            InstancedRenderManager.render(poseStack, bufferSource, lightTexture, camera, partialTicks, clippingHelper, textureManager);
        }
    }

    @Shadow
    @Final
    private Map<ParticleRenderType, Queue<Particle>> particles;

    @SuppressWarnings("UnstableApiUsage")
    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Ljava/util/Queue;add(Ljava/lang/Object;)Z"), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void madparticleAddToInstancedRenderManager(CallbackInfo ci, Particle particle) {
        if (particle instanceof MadParticle madParticle && madParticle.getRenderType() == ModParticleRenderTypes.INSTANCED) {
            var queue = (EvictingQueue<Particle>) particles.get(ModParticleRenderTypes.INSTANCED);
            if (queue.remainingCapacity() == 0) {
                InstancedRenderManager.remove((MadParticle) queue.peek());
            }
            InstancedRenderManager.add(madParticle);
        }
    }

    @Inject(method = "tickParticleList", at = @At(value = "INVOKE", target = "Ljava/util/Iterator;remove()V", shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void madparticleRemoveFromInstancedRenderManager(Collection<Particle> pParticles, CallbackInfo ci, Iterator<?> iterator, Particle particle) {
        if (particle instanceof MadParticle madParticle && madParticle.getRenderType() == ModParticleRenderTypes.INSTANCED) {
            InstancedRenderManager.remove(madParticle);
        }
    }
}
