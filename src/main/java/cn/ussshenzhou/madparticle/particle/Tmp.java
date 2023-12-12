package cn.ussshenzhou.madparticle.particle;

/**
 * @author USS_Shenzhou
 */
public class Tmp {
    /*private static final List<ParticleRenderType> RENDER_ORDER = ImmutableList.of(ParticleRenderType.TERRAIN_SHEET, ParticleRenderType.PARTICLE_SHEET_OPAQUE, ParticleRenderType.PARTICLE_SHEET_LIT, ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT, ParticleRenderType.CUSTOM);
    private Map<ParticleRenderType, Queue<Particle>> particles = Maps.newTreeMap(net.minecraftforge.client.ForgeHooksClient.makeParticleRenderTypeComparator(RENDER_ORDER));
    private TextureManager textureManager;


    public void render(PoseStack pMatrixStack, MultiBufferSource.BufferSource pBuffer, LightTexture pLightTexture, Camera pActiveRenderInfo, float pPartialTicks, @Nullable Frustum clippingHelper) {
        pLightTexture.turnOnLightLayer();
        RenderSystem.enableDepthTest();
        RenderSystem.activeTexture(GL13.GL_TEXTURE2);
        RenderSystem.activeTexture(GL13.GL_TEXTURE0);
        PoseStack posestack = RenderSystem.getModelViewStack();
        posestack.pushPose();
        posestack.mulPoseMatrix(pMatrixStack.last().pose());
        RenderSystem.applyModelViewMatrix();



        for (ParticleRenderType particlerendertype : this.particles.keySet()) {
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

                for (Particle particle : iterable) {
                    if (clippingHelper != null && particle.shouldCull() && !clippingHelper.isVisible(particle.getBoundingBox())) {
                        continue;
                    }
                    try {

                        //particle.render(bufferbuilder, pActiveRenderInfo, pPartialTicks);
                        Vec3 vec3 = pActiveRenderInfo.getPosition();
                        float f = (float) (Mth.lerp((double) pPartialTicks, particle.xo, particle.x) - vec3.x());
                        float f1 = (float) (Mth.lerp((double) pPartialTicks, particle.yo, particle.y) - vec3.y());
                        float f2 = (float) (Mth.lerp((double) pPartialTicks, particle.zo, particle.z) - vec3.z());
                        Quaternionf quaternionf;
                        if (particle.roll == 0.0F) {
                            quaternionf = pActiveRenderInfo.rotation();
                        } else {
                            quaternionf = new Quaternionf(pActiveRenderInfo.rotation());
                            quaternionf.rotateZ(Mth.lerp(pPartialTicks, particle.oRoll, particle.roll));
                        }

                        Vector3f[] avector3f = new Vector3f[]{new Vector3f(-1.0F, -1.0F, 0.0F), new Vector3f(-1.0F, 1.0F, 0.0F), new Vector3f(1.0F, 1.0F, 0.0F), new Vector3f(1.0F, -1.0F, 0.0F)};
                        float f3 = particle.getQuadSize(pPartialTicks);

                        for (int i = 0; i < 4; ++i) {
                            Vector3f vector3f = avector3f[i];
                            vector3f.rotate(quaternionf);
                            vector3f.mul(f3);
                            vector3f.add(f, f1, f2);
                        }

                        float f6 = ((SingleQuadParticle) particle).getU0();
                        float f7 = ((SingleQuadParticle) particle).getU1();
                        float f4 = ((SingleQuadParticle) particle).getV0();
                        float f5 = ((SingleQuadParticle) particle).getV1();
                        int j = particle.getLightColor(pPartialTicks);
                        bufferbuilder.vertex((double) avector3f[0].x(), (double) avector3f[0].y(), (double) avector3f[0].z())
                                .uv(f7, f5)
                                .color(particle.rCol, particle.gCol, particle.bCol, particle.alpha)
                                .uv2(j)
                                .endVertex();
                        bufferbuilder.vertex((double) avector3f[1].x(), (double) avector3f[1].y(), (double) avector3f[1].z())
                                .uv(f7, f4)
                                .color(particle.rCol, particle.gCol, particle.bCol, particle.alpha)
                                .uv2(j)
                                .endVertex();
                        bufferbuilder.vertex((double) avector3f[2].x(), (double) avector3f[2].y(), (double) avector3f[2].z())
                                .uv(f6, f4)
                                .color(particle.rCol, particle.gCol, particle.bCol, particle.alpha)
                                .uv2(j)
                                .endVertex();
                        bufferbuilder.vertex((double) avector3f[3].x(), (double) avector3f[3].y(), (double) avector3f[3].z())
                                .uv(f6, f5)
                                .color(particle.rCol, particle.gCol, particle.bCol, particle.alpha)
                                .uv2(j)
                                .endVertex();


                    } catch (Throwable throwable) {
                        CrashReport crashreport = CrashReport.forThrowable(throwable, "Rendering Particle");
                        CrashReportCategory crashreportcategory = crashreport.addCategory("Particle being rendered");
                        crashreportcategory.setDetail("Particle", particle::toString);
                        crashreportcategory.setDetail("Particle Type", particlerendertype::toString);
                        throw new ReportedException(crashreport);
                    }
                }

                //particlerendertype.end(tesselator);
                //tesselator.end();
                BufferBuilder.RenderedBuffer renderedBuffer = tesselator.builder.end();
                //BufferUploader.drawWithShader(renderedBuffer);
                //glVertexAttribPointer
                VertexBuffer vertexbuffer = BufferUploader.upload(renderedBuffer);
                //vertexbuffer._drawWithShader(RenderSystem.getModelViewMatrix(), RenderSystem.getProjectionMatrix(), RenderSystem.getShader());
                org.joml.Matrix4f pModelViewMatrix = RenderSystem.getModelViewMatrix();
                Matrix4f pProjectionMatrix = RenderSystem.getProjectionMatrix();
                ShaderInstance pShader = RenderSystem.getShader();
                for (int i = 0; i < 12; ++i) {
                    int j = RenderSystem.getShaderTexture(i);
                    pShader.setSampler("Sampler" + i, j);
                }

                if (pShader.MODEL_VIEW_MATRIX != null) {
                    pShader.MODEL_VIEW_MATRIX.set(pModelViewMatrix);
                }

                if (pShader.PROJECTION_MATRIX != null) {
                    pShader.PROJECTION_MATRIX.set(pProjectionMatrix);
                }

                if (pShader.INVERSE_VIEW_ROTATION_MATRIX != null) {
                    pShader.INVERSE_VIEW_ROTATION_MATRIX.set(RenderSystem.getInverseViewRotationMatrix());
                }

                if (pShader.COLOR_MODULATOR != null) {
                    pShader.COLOR_MODULATOR.set(RenderSystem.getShaderColor());
                }

                if (pShader.GLINT_ALPHA != null) {
                    pShader.GLINT_ALPHA.set(RenderSystem.getShaderGlintAlpha());
                }

                if (pShader.FOG_START != null) {
                    pShader.FOG_START.set(RenderSystem.getShaderFogStart());
                }

                if (pShader.FOG_END != null) {
                    pShader.FOG_END.set(RenderSystem.getShaderFogEnd());
                }

                if (pShader.FOG_COLOR != null) {
                    pShader.FOG_COLOR.set(RenderSystem.getShaderFogColor());
                }

                if (pShader.FOG_SHAPE != null) {
                    pShader.FOG_SHAPE.set(RenderSystem.getShaderFogShape().getIndex());
                }

                if (pShader.TEXTURE_MATRIX != null) {
                    pShader.TEXTURE_MATRIX.set(RenderSystem.getTextureMatrix());
                }

                if (pShader.GAME_TIME != null) {
                    pShader.GAME_TIME.set(RenderSystem.getShaderGameTime());
                }

                if (pShader.SCREEN_SIZE != null) {
                    Window window = Minecraft.getInstance().getWindow();
                    pShader.SCREEN_SIZE.set((float) window.getWidth(), (float) window.getHeight());
                }

                if (pShader.LINE_WIDTH != null && (vertexbuffer.mode == VertexFormat.Mode.LINES || vertexbuffer.mode == VertexFormat.Mode.LINE_STRIP)) {
                    pShader.LINE_WIDTH.set(RenderSystem.getShaderLineWidth());
                }

                RenderSystem.setupShaderLights(pShader);
                pShader.apply();

                //vertexbuffer.draw();
                //RenderSystem.drawElements(vertexbuffer.mode.asGLMode, vertexbuffer.indexCount, vertexbuffer.getIndexType().asGLType);
                //var pMode = vertexbuffer.mode.asGLMode;
                //var pCount = vertexbuffer.indexCount;
                //var pType = vertexbuffer.getIndexType().asGLType;
                //GlStateManager._drawElements(pMode, pCount, pType, 0L);
                //GL11.glDrawElements(pMode, pCount, pType, 0);
                GL11C.glDrawElements(4, 6, 5123, 0);
                pShader.clear();
            }
        }


        posestack.popPose();
        RenderSystem.applyModelViewMatrix();
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
        pLightTexture.turnOffLightLayer();
    }
*/
}
