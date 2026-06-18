package cn.ussshenzhou.madparticle.mixin;

import cn.ussshenzhou.madparticle.particle.render.ModRenderPipelines;
import com.mojang.blaze3d.opengl.GlCommandEncoder;
import com.mojang.blaze3d.opengl.GlConst;
import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import org.jspecify.annotations.Nullable;
import org.lwjgl.opengl.ARBDrawBuffersBlend;
import org.lwjgl.opengl.GL33C;
import org.lwjgl.opengl.GL40;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GlCommandEncoder.class)
public class GlCommandEncoderMixin {

    @Shadow
    @Nullable
    public RenderPipeline lastPipeline;

    @Inject(method = "applyPipelineState", at = @At("HEAD"), cancellable = true)
    public void madparticleSupportSeparateBlend(RenderPipeline pipeline, CallbackInfo ci) {
        if (pipeline == ModRenderPipelines.INSTANCED_OIT) {
            if (this.lastPipeline != pipeline) {
                this.lastPipeline = pipeline;
                DepthStencilState depthStencilState = pipeline.getDepthStencilState();
                if (depthStencilState != null) {
                    GlStateManager._enableDepthTest();
                    GlStateManager._depthFunc(GlConst.toGl(depthStencilState.depthTest()));
                    GlStateManager._depthMask(depthStencilState.writeDepth());
                    if (depthStencilState.depthBiasConstant() == 0.0F && depthStencilState.depthBiasScaleFactor() == 0.0F) {
                        GlStateManager._disablePolygonOffset();
                    } else {
                        GlStateManager._polygonOffset(depthStencilState.depthBiasScaleFactor(), depthStencilState.depthBiasConstant());
                        GlStateManager._enablePolygonOffset();
                    }
                } else {
                    GlStateManager._disableDepthTest();
                    GlStateManager._depthMask(false);
                    GlStateManager._disablePolygonOffset();
                }

                if (pipeline.isCull()) {
                    GlStateManager._enableCull();
                } else {
                    GlStateManager._disableCull();
                }

                ColorTargetState[] colorTargetStates = pipeline.getColorTargetStates();

                for (int i = 0; i < colorTargetStates.length; i++) {
                    ColorTargetState state = colorTargetStates[i];
                    if (state != null) {
                        if (state.blendFunction().isPresent()) {
                            GlStateManager._enableBlend(i);
                            BlendFunction blendFunction = state.blendFunction().get();
                            ARBDrawBuffersBlend.glBlendFuncSeparateiARB(
                                    i,
                                    GlConst.toGl(blendFunction.color().sourceFactor()),
                                    GlConst.toGl(blendFunction.color().destFactor()),
                                    GlConst.toGl(blendFunction.alpha().sourceFactor()),
                                    GlConst.toGl(blendFunction.alpha().destFactor())
                            );
                            ARBDrawBuffersBlend.glBlendEquationSeparateiARB(
                                    i,
                                    GlConst.toGl(blendFunction.color().op()),
                                    GlConst.toGl(blendFunction.alpha().op())
                            );
                        } else {
                            GlStateManager._disableBlend(i);
                        }
                    }
                }

                GlStateManager._polygonMode(1032, GlConst.toGl(pipeline.getPolygonMode()));

                for (int i = 0; i < colorTargetStates.length; i++) {
                    ColorTargetState state = colorTargetStates[i];
                    if (state != null) {
                        GlStateManager._colorMask(i, state.writeMask());
                    }
                }
            }
            ci.cancel();
        }
    }
}
