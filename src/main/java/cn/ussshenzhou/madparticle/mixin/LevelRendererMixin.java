package cn.ussshenzhou.madparticle.mixin;

import cn.ussshenzhou.madparticle.particle.ModParticleRenderTypes;
import cn.ussshenzhou.madparticle.particle.optimize.NeoInstancedRenderManager;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import com.mojang.blaze3d.resource.ResourceHandle;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.state.LevelRenderState;
import net.minecraft.util.profiling.ProfilerFiller;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author USS_Shenzhou
 */
@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {

    @Inject(method = "lambda$addMainPass$0",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/state/ParticlesRenderState;submit(Lnet/minecraft/client/renderer/SubmitNodeStorage;Lnet/minecraft/client/renderer/state/CameraRenderState;)V",
                    shift = At.Shift.AFTER
            )
    )
    private void madparticleRenderInstanced(GpuBufferSlice terrainFog, LevelRenderState levelRenderState, ProfilerFiller profiler, Matrix4f modelViewMatrix, ResourceHandle entityOutlineTarget, ResourceHandle translucentTarget, ResourceHandle mainTarget, ResourceHandle itemEntityTarget, ResourceHandle particleTarget, boolean renderOutline, CallbackInfo ci) {
        NeoInstancedRenderManager.getInstance(ModParticleRenderTypes.INSTANCED).render();
        NeoInstancedRenderManager.getInstance(ModParticleRenderTypes.INSTANCED_TERRAIN).render();
    }
}
