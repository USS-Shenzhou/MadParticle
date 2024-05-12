package cn.ussshenzhou.madparticle.particle;

import cn.ussshenzhou.madparticle.MadParticle;
import cn.ussshenzhou.madparticle.MadParticleConfig;
import cn.ussshenzhou.madparticle.mixin.VertexFormatElementUsageAccessor;
import cn.ussshenzhou.madparticle.particle.enums.TranslucentMethod;
import cn.ussshenzhou.madparticle.particle.optimize.InstancedRenderBufferBuilder;
import cn.ussshenzhou.t88.config.ConfigHelper;
import com.google.common.collect.ImmutableMap;
//import com.lowdragmc.shimmer.client.postprocessing.PostProcessing;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureManager;
import org.lwjgl.opengl.GL43;

import javax.annotation.ParametersAreNonnullByDefault;

import static com.mojang.blaze3d.vertex.DefaultVertexFormat.*;


/**
 * @author USS_Shenzhou
 */
@ParametersAreNonnullByDefault
public class ModParticleRenderTypes {

    @SuppressWarnings("AlibabaConstantFieldShouldBeUpperCase")
    public static final InstancedRenderBufferBuilder instancedRenderBufferBuilder = new InstancedRenderBufferBuilder(1024 * 512);

    public static final ParticleRenderType INSTANCED = new ParticleRenderType() {
        @Override
        public void begin(BufferBuilder pBuilder, TextureManager pTextureManager) {
            RenderSystem.setShader(ModParticleShaders::getInstancedParticleShader);
            RenderSystem.depthMask(ConfigHelper.getConfigRead(MadParticleConfig.class).translucentMethod == TranslucentMethod.DEPTH_TRUE);
            RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_PARTICLES);
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            instancedRenderBufferBuilder.begin(VertexFormat.Mode.QUADS, INSTANCED_FORMAT);
        }

        @Override
        public void end(Tesselator pTesselator) {
            instancedRenderBufferBuilder.end();
        }
    };

    public static final VertexFormatElement.Usage UV_CONTROL =
            VertexFormatElementUsageAccessor.constructor("UV_CONTROL", 0,
                    "UV_CONTROL",
                    (pCount, pGlType, pVertexSize, pOffset, pIndex, pStateIndex) -> {
                        GlStateManager._enableVertexAttribArray(pStateIndex);
                        GlStateManager._vertexAttribIPointer(pStateIndex, pCount, pGlType, pVertexSize, pOffset);
                    },
                    (pIndex, pElementIndex) -> {
                        GlStateManager._disableVertexAttribArray(pElementIndex);
                    });

    public static final VertexFormatElement ELEMENT_UV_CONTROL = new VertexFormatElement(0, VertexFormatElement.Type.INT, UV_CONTROL, 4);

    public static final VertexFormat INSTANCED_FORMAT = new VertexFormat(ImmutableMap.<String, VertexFormatElement>builder()
            .put("Position", ELEMENT_POSITION)
            .put("UVControl", ELEMENT_UV_CONTROL)
            .build());


    /**
     * don't use the passed BufferBuilder instance in the {@link Traditional#begin(BufferBuilder, TextureManager)}<p>
     * use the BufferBuilder holded by {@link Traditional} itself
     *
     * @author zomb-676
     */
    public enum Traditional implements ParticleRenderType {

        TERRAIN_SHEET {
            @Override
            public void begin(@Deprecated BufferBuilder bufferBuilderPassed, TextureManager textureManager) {
                begin();
                RenderSystem.enableBlend();
                RenderSystem.setShader(ModParticleShaders::getTraditionalParticleShader);
                RenderSystem.defaultBlendFunc();
                RenderSystem.depthMask(true);
                RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_BLOCKS);
                this.bufferBuilder.begin(VertexFormat.Mode.QUADS, TRADITIONAL_FORMAT);
            }

            @Override
            public void end(Tesselator tesselator) {
                end();
            }

            @Override
            public String toString() {
                return "MAD_TERRAIN_SHEET";
            }
        },
        PARTICLE_SHEET_OPAQUE {
            @Override
            public void begin(@Deprecated BufferBuilder bufferBuilderPassed, TextureManager textureManager) {
                begin();
                RenderSystem.disableBlend();
                RenderSystem.setShader(ModParticleShaders::getTraditionalParticleShader);
                RenderSystem.depthMask(true);
                RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_PARTICLES);
                this.bufferBuilder.begin(VertexFormat.Mode.QUADS, TRADITIONAL_FORMAT);
            }

            @Override
            public void end(Tesselator tesselator) {
                end();
            }

            @Override
            public String toString() {
                return "MAD_PARTICLE_SHEET_OPAQUE";
            }
        },
        PARTICLE_SHEET_TRANSLUCENT {
            @Override
            public void begin(@Deprecated BufferBuilder bufferBuilderPassed, TextureManager textureManager) {
                begin();
                RenderSystem.setShader(ModParticleShaders::getTraditionalParticleShader);
                RenderSystem.depthMask(true);
                RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_PARTICLES);
                RenderSystem.enableBlend();
                RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
                this.bufferBuilder.begin(VertexFormat.Mode.QUADS, TRADITIONAL_FORMAT);
            }

            @Override
            public void end(Tesselator tesselator) {
                end();
            }

            @Override
            public String toString() {
                return "MAD_PARTICLE_SHEET_TRANSLUCENT";
            }
        },
        PARTICLE_SHEET_LIT {
            @Override
            public void begin(@Deprecated BufferBuilder bufferBuilderPassed, TextureManager textureManager) {
                begin();
                RenderSystem.setShader(ModParticleShaders::getTraditionalParticleShader);
                RenderSystem.disableBlend();
                RenderSystem.depthMask(true);
                RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_PARTICLES);
                this.bufferBuilder.begin(VertexFormat.Mode.QUADS, TRADITIONAL_FORMAT);
            }

            @Override
            public void end(Tesselator tesselator) {
                end();
            }

            @Override
            public String toString() {
                return "MAD_PARTICLE_SHEET_LIT";
            }
        };

        public final MadParticleBufferBuilder bufferBuilder = new MadParticleBufferBuilder(1024 * 512);

        public void end() {
            BufferUploader.drawWithShader(bufferBuilder.end());
            MadParticle.runOnShimmer(() -> () -> {
                GL43.glDrawBuffers(GL43.GL_COLOR_ATTACHMENT0);
                //PostProcessing.getBlockBloom().renderBlockPost();
            });
        }

        public void begin() {
            MadParticle.runOnShimmer(() -> () -> GL43.glDrawBuffers(new int[]{GL43.GL_COLOR_ATTACHMENT0, GL43.GL_COLOR_ATTACHMENT1}));
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

        public static final VertexFormat TRADITIONAL_FORMAT = new VertexFormat(ImmutableMap.<String, VertexFormatElement>builder()
                .put("Position", ELEMENT_POSITION)
                .put("UV0", ELEMENT_UV0)
                .put("Color", Traditional.ELEMENT_COLOR)
                .put("UV2", ELEMENT_UV2)
                .put("BloomFactor", ELEMENT_BLOOM_FACTOR)
                .build());

        static {
            if (MadParticle.IS_OPTIFINE_INSTALLED) {
                try {
                    @SuppressWarnings("JavaReflectionMemberAccess")
                    var field = VertexFormat.class.getDeclaredField("colorElementOffset");
                    field.trySetAccessible();
                    field.set(TRADITIONAL_FORMAT, ELEMENT_POSITION.getByteSize() + ELEMENT_UV0.getByteSize());
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
}