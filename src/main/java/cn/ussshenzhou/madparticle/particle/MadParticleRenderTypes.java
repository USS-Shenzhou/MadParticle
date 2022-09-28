package cn.ussshenzhou.madparticle.particle;

import cn.ussshenzhou.madparticle.mixin.VertexFormatElementUsageAccessor;
import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureManager;

import static com.mojang.blaze3d.vertex.DefaultVertexFormat.*;

public enum MadParticleRenderTypes implements ParticleRenderType {

    TERRAIN_SHEET {
        public void begin(BufferBuilder bufferBuilder, TextureManager textureManager) {
            RenderSystem.enableBlend();
            RenderSystem.setShader(MadParticleShader::getMadParticleShader);
            RenderSystem.defaultBlendFunc();
            RenderSystem.depthMask(true);
            RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_BLOCKS);
            bufferBuilder.begin(VertexFormat.Mode.QUADS, PARTICLE);
        }

        public void end(Tesselator tesselator) {
            end();
        }

        public String toString() {
            return "MAD_TERRAIN_SHEET";
        }
    },
    PARTICLE_SHEET_OPAQUE {
        public void begin(BufferBuilder bufferBuilder, TextureManager textureManager) {
            RenderSystem.disableBlend();
            RenderSystem.setShader(MadParticleShader::getMadParticleShader);
            RenderSystem.depthMask(true);
            RenderSystem.setShader(GameRenderer::getParticleShader);
            RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_PARTICLES);
            bufferBuilder.begin(VertexFormat.Mode.QUADS, PARTICLE);
        }

        public void end(Tesselator tesselator) {
            end();
        }

        public String toString() {
            return "MAD_PARTICLE_SHEET_OPAQUE";
        }
    },
    PARTICLE_SHEET_TRANSLUCENT {
        public void begin(BufferBuilder bufferBuilder, TextureManager textureManager) {
            RenderSystem.setShader(MadParticleShader::getMadParticleShader);
            RenderSystem.depthMask(true);
            RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_PARTICLES);
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            bufferBuilder.begin(VertexFormat.Mode.QUADS, PARTICLE);
        }

        public void end(Tesselator tesselator) {
            end();
        }

        public String toString() {
            return "MAD_PARTICLE_SHEET_TRANSLUCENT";
        }
    },
    PARTICLE_SHEET_LIT {
        public void begin(BufferBuilder bufferBuilder, TextureManager textureManager) {
            RenderSystem.setShader(MadParticleShader::getMadParticleShader);
            RenderSystem.disableBlend();
            RenderSystem.depthMask(true);
            RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_PARTICLES);
            bufferBuilder.begin(VertexFormat.Mode.QUADS, PARTICLE);
        }

        public void end(Tesselator tesselator) {
            end();
        }

        public String toString() {
            return "MAD_PARTICLE_SHEET_LIT";
        }
    };

    public final MadParticleBufferBuilder bufferBuilder = new MadParticleBufferBuilder(1024 * 512);

    public void end(){
        bufferBuilder.end();
        BufferUploader.end(this.bufferBuilder);
    }

    public static final VertexFormatElement.Usage NO_NORMALIZED_COLOR =
            VertexFormatElementUsageAccessor.constructor("NO_NORMALIZED_COLOR", 0,
                    "NO_NORMALIZED_COLOR",
                    (pCount, pGlType, pVertexSize, pOffset, pIndex, pStateIndex) -> {
                        GlStateManager._enableVertexAttribArray(pStateIndex);
                        GlStateManager._vertexAttribPointer(pStateIndex, pCount, pGlType, false, pVertexSize, pOffset);
                    },
                    (pIndex, pElementIndex) -> {
                        GlStateManager._disableVertexAttribArray(pElementIndex);
                    });

    public static final VertexFormatElement ELEMENT_COLOR = new VertexFormatElement(0
            , VertexFormatElement.Type.FLOAT, NO_NORMALIZED_COLOR, 4);


    public static final VertexFormat PARTICLE = new VertexFormat(ImmutableMap.<String, VertexFormatElement>builder()
            .put("Position", ELEMENT_POSITION)
            .put("UV0", ELEMENT_UV0)
            .put("Color", ELEMENT_COLOR)
            .put("UV2", ELEMENT_UV2)
            .build());

}
