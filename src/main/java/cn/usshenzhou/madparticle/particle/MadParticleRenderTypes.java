package cn.usshenzhou.madparticle.particle;

import cn.usshenzhou.madparticle.Madparticle;
import cn.usshenzhou.madparticle.mixin.VertexFormatElementUsageAccessor;
import com.google.common.collect.ImmutableMap;
import com.lowdragmc.shimmer.client.postprocessing.PostProcessing;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureManager;
import org.lwjgl.opengl.GL43;

import static com.mojang.blaze3d.vertex.DefaultVertexFormat.*;

/**
 * don't use the passed BufferBuilder instance in the {@link MadParticleRenderTypes#begin(BufferBuilder, TextureManager)}<p>
 * use the BufferBuilder holded by {@link MadParticleRenderTypes} itself
 *
 * @author zomb-676
 */
@SuppressWarnings("AlibabaEnumConstantsMustHaveComment")
public enum MadParticleRenderTypes implements ParticleRenderType {

    TERRAIN_SHEET {
        public void begin(@Deprecated BufferBuilder bufferBuilderPassed, TextureManager textureManager) {
            begin();
            RenderSystem.enableBlend();
            RenderSystem.setShader(MadParticleShader::getMadParticleShader);
            RenderSystem.defaultBlendFunc();
            RenderSystem.depthMask(true);
            RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_BLOCKS);
            this.bufferBuilder.begin(VertexFormat.Mode.QUADS, PARTICLE);
        }

        public void end(Tesselator tesselator) {
            end();
        }

        public String toString() {
            return "MAD_TERRAIN_SHEET";
        }
    },
    PARTICLE_SHEET_OPAQUE {
        public void begin(@Deprecated BufferBuilder bufferBuilderPassed, TextureManager textureManager) {
            begin();
            RenderSystem.disableBlend();
            RenderSystem.setShader(MadParticleShader::getMadParticleShader);
            RenderSystem.depthMask(true);
            RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_PARTICLES);
            this.bufferBuilder.begin(VertexFormat.Mode.QUADS, PARTICLE);
        }

        public void end(Tesselator tesselator) {
            end();
        }

        public String toString() {
            return "MAD_PARTICLE_SHEET_OPAQUE";
        }
    },
    PARTICLE_SHEET_TRANSLUCENT {
        public void begin(@Deprecated BufferBuilder bufferBuilderPassed, TextureManager textureManager) {
            begin();
            RenderSystem.setShader(MadParticleShader::getMadParticleShader);
            RenderSystem.depthMask(true);
            RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_PARTICLES);
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            this.bufferBuilder.begin(VertexFormat.Mode.QUADS, PARTICLE);
        }

        public void end(Tesselator tesselator) {
            end();
        }

        public String toString() {
            return "MAD_PARTICLE_SHEET_TRANSLUCENT";
        }
    },
    PARTICLE_SHEET_LIT {
        public void begin(@Deprecated BufferBuilder bufferBuilderPassed, TextureManager textureManager) {
            begin();
            RenderSystem.setShader(MadParticleShader::getMadParticleShader);
            RenderSystem.disableBlend();
            RenderSystem.depthMask(true);
            RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_PARTICLES);
            this.bufferBuilder.begin(VertexFormat.Mode.QUADS, PARTICLE);
        }

        public void end(Tesselator tesselator) {
            end();
        }

        public String toString() {
            return "MAD_PARTICLE_SHEET_LIT";
        }
    };

    public final MadParticleBufferBuilder bufferBuilder = new MadParticleBufferBuilder(1024 * 512);

    public void end() {
        bufferBuilder.end();
        BufferUploader.end(this.bufferBuilder);
        Madparticle.runOnShimmer(() -> () -> {
            GL43.glDrawBuffers(GL43.GL_COLOR_ATTACHMENT0);
            PostProcessing.getBlockBloom().renderBlockPost();
        });
    }

    public void begin() {
        Madparticle.runOnShimmer(() -> () -> GL43.glDrawBuffers(new int[]{GL43.GL_COLOR_ATTACHMENT0, GL43.GL_COLOR_ATTACHMENT1}));
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

    public static final VertexFormatElement.Usage BLOOM_FACTOR =
            VertexFormatElementUsageAccessor.constructor("BLOOM_FACTOR", 0,
                    "BLOOM_FACTOR",
                    (pCount, pGlType, pVertexSize, pOffset, pIndex, pStateIndex) -> {
                        GlStateManager._enableVertexAttribArray(pStateIndex);
                        GlStateManager._vertexAttribPointer(pStateIndex, pCount, pGlType, false, pVertexSize, pOffset);
                    },
                    (pIndex, pElementIndex) -> {
                        GlStateManager._disableVertexAttribArray(pElementIndex);
                    });

    public static final VertexFormatElement ELEMENT_BLOOM_FACTOR = new VertexFormatElement(0
            , VertexFormatElement.Type.FLOAT, BLOOM_FACTOR, 1);


    public static final VertexFormat PARTICLE = new VertexFormat(ImmutableMap.<String, VertexFormatElement>builder()
            .put("Position", ELEMENT_POSITION)
            .put("UV0", ELEMENT_UV0)
            .put("Color", MadParticleRenderTypes.ELEMENT_COLOR)
            .put("UV2", ELEMENT_UV2)
            .put("BloomFactor", ELEMENT_BLOOM_FACTOR)
            .build());

    static {
        if (Madparticle.isOptifineInstalled) {
            try {
                @SuppressWarnings("JavaReflectionMemberAccess")
                var field = VertexFormat.class.getDeclaredField("colorElementOffset");
                field.trySetAccessible();
                field.set(PARTICLE, ELEMENT_POSITION.getByteSize() + ELEMENT_UV0.getByteSize());
            } catch (IllegalAccessException | NoSuchFieldException e) {
                throw new RuntimeException("failed to set colorElementOffset", e);
            }
            try {
                @SuppressWarnings("JavaReflectionMemberAccess")
                var field = VertexFormatElement.class.getDeclaredField("attributeIndex");
                field.trySetAccessible();
                field.set(ELEMENT_COLOR, 1);
            } catch (IllegalAccessException | NoSuchFieldException e) {
                throw new RuntimeException("failed to set attributeIndex", e);
            }
        }
    }

}
