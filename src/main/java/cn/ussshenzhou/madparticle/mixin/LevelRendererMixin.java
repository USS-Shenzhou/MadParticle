package cn.ussshenzhou.madparticle.mixin;

import cn.ussshenzhou.madparticle.particle.render.ModParticleRenderTypes;
import cn.ussshenzhou.madparticle.particle.render.NeoInstancedRenderManager;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.resource.ResourceHandle;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.chunk.ChunkSectionsToRender;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.util.profiling.ProfilerFiller;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author USS_Shenzhou
 */
@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {

    @SuppressWarnings("rawtypes")
    @Inject(method = "lambda$addMainPass$0",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/state/level/ParticlesRenderState;submit(Lnet/minecraft/client/renderer/SubmitNodeStorage;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
                    shift = At.Shift.AFTER
            )
    )
    private void madparticleRenderInstanced(GpuBufferSlice terrainFog, LevelRenderState levelRenderState, ProfilerFiller profiler, ChunkSectionsToRender chunkSectionsToRender, Matrix4fc modelViewMatrix, ResourceHandle entityOutlineTarget, ResourceHandle translucentTarget, ResourceHandle mainTarget, ResourceHandle itemEntityTarget, ResourceHandle particleTarget, boolean renderOutline, CallbackInfo ci) {
        NeoInstancedRenderManager.getInstance(ModParticleRenderTypes.INSTANCED).render();
        NeoInstancedRenderManager.getInstance(ModParticleRenderTypes.INSTANCED_TERRAIN).render();
    }
}
