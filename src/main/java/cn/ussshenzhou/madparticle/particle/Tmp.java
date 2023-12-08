package cn.ussshenzhou.madparticle.particle;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.Camera;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureManager;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Queue;

/**
 * @author USS_Shenzhou
 */
public class Tmp {
    private static final List<ParticleRenderType> RENDER_ORDER = ImmutableList.of(ParticleRenderType.TERRAIN_SHEET, ParticleRenderType.PARTICLE_SHEET_OPAQUE, ParticleRenderType.PARTICLE_SHEET_LIT, ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT, ParticleRenderType.CUSTOM);
    private Map<ParticleRenderType, Queue<Particle>> particles = Maps.newTreeMap(net.minecraftforge.client.ForgeHooksClient.makeParticleRenderTypeComparator(RENDER_ORDER));
    private TextureManager textureManager;

    /**
     * @see net.minecraft.client.particle.ParticleEngine#render(PoseStack, MultiBufferSource.BufferSource, LightTexture, Camera, float, Frustum)
     */
    public void render(PoseStack pMatrixStack, MultiBufferSource.BufferSource pBuffer, LightTexture pLightTexture, Camera pActiveRenderInfo, float pPartialTicks, @Nullable net.minecraft.client.renderer.culling.Frustum clippingHelper) {
        pLightTexture.turnOnLightLayer();
        RenderSystem.enableDepthTest();
        RenderSystem.activeTexture(org.lwjgl.opengl.GL13.GL_TEXTURE2);
        RenderSystem.activeTexture(org.lwjgl.opengl.GL13.GL_TEXTURE0);
        PoseStack posestack = RenderSystem.getModelViewStack();
        posestack.pushPose();
        posestack.mulPoseMatrix(pMatrixStack.last().pose());
        RenderSystem.applyModelViewMatrix();

        for(ParticleRenderType particlerendertype : this.particles.keySet()) {
            if (particlerendertype == ParticleRenderType.NO_RENDER) {
                continue;
            }
            Iterable<Particle> iterable = this.particles.get(particlerendertype);
            if (iterable != null) {
                RenderSystem.setShader(GameRenderer::getParticleShader);

                Tesselator tesselator = Tesselator.getInstance();
                BufferBuilder bufferbuilder = tesselator.getBuilder();

                //particlerendertype.begin(bufferbuilder, this.textureManager);
                RenderSystem.disableBlend();
                RenderSystem.depthMask(true);
                RenderSystem.setShader(GameRenderer::getParticleShader);
                RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_PARTICLES);
                bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);

                for(Particle particle : iterable) {
                    if (clippingHelper != null && particle.shouldCull() && !clippingHelper.isVisible(particle.getBoundingBox())) {
                        continue;
                    }
                    try {

                        particle.render(bufferbuilder, pActiveRenderInfo, pPartialTicks);

                    } catch (Throwable throwable) {
                        CrashReport crashreport = CrashReport.forThrowable(throwable, "Rendering Particle");
                        CrashReportCategory crashreportcategory = crashreport.addCategory("Particle being rendered");
                        crashreportcategory.setDetail("Particle", particle::toString);
                        crashreportcategory.setDetail("Particle Type", particlerendertype::toString);
                        throw new ReportedException(crashreport);
                    }
                }

                //particlerendertype.end(tesselator);
                tesselator.end();
            }
        }

        posestack.popPose();
        RenderSystem.applyModelViewMatrix();
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
        pLightTexture.turnOffLightLayer();
    }
}
