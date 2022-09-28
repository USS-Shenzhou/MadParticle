package cn.ussshenzhou.madparticle.particle;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormatElement;

public class MadParticleBufferBuilder extends BufferBuilder {

    MadParticleBufferBuilder(int pCapacity) {
        super(pCapacity);
    }

    /**
     * allow greater than 255
     */
    @Override
    public VertexConsumer color(int pRed, int pGreen, int pBlue, int pAlpha) {
        VertexFormatElement vertexformatelement = this.currentElement();
        if (vertexformatelement.getUsage() != MadParticleRenderTypes.NO_NORMALIZED_COLOR) {
            return this;
        } else if (vertexformatelement.getType() == VertexFormatElement.Type.FLOAT && vertexformatelement.getCount() == 4) {
            this.putFloat(0, pRed / 255f);
            this.putFloat(4, pGreen / 255f);
            this.putFloat(2 * 4, pBlue / 255f);
            this.putFloat(3 * 4, pAlpha / 255f);
            this.nextElement();
            return this;
        } else {
            throw new IllegalStateException();
        }
    }

    /**
     * allow greater than 1f
     */
    @Override
    public VertexConsumer color(float pRed, float pGreen, float pBlue, float pAlpha) {
        VertexFormatElement vertexformatelement = this.currentElement();
        if (vertexformatelement.getUsage() != MadParticleRenderTypes.NO_NORMALIZED_COLOR) {
            return this;
        } else if (vertexformatelement.getType() == VertexFormatElement.Type.FLOAT && vertexformatelement.getCount() == 4) {
            this.putFloat(0, pRed);
            this.putFloat(4, pGreen);
            this.putFloat(2 * 4, pBlue);
            this.putFloat(3 * 4, pAlpha);
            this.nextElement();
            return this;
        } else {
            throw new IllegalStateException();
        }
    }

    /**
     * don't use this
     */
    @Override
    @Deprecated
    public VertexConsumer color(int pColorARGB) {
        return super.color(pColorARGB);
    }

    //TODO override the full vertex fill version
}
