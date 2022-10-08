package cn.ussshenzhou.madparticle.particle;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferVertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormatElement;

/**
 * @author zomb-676
 */
public class MadParticleBufferBuilder extends BufferBuilder {

    MadParticleBufferBuilder(int pCapacity) {
        super(pCapacity);
    }

    /**
     * allow greater than 255
     */
    @Override
    public MadParticleBufferBuilder color(int pRed, int pGreen, int pBlue, int pAlpha) {
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
    public MadParticleBufferBuilder color(float pRed, float pGreen, float pBlue, float pAlpha) {
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
     *
     * @param bloomFactor should range from 0 to 1
     */
    public MadParticleBufferBuilder bloomFactor(float bloomFactor) {
        var vertexformatelement = this.currentElement();
        if (vertexformatelement.getUsage() != MadParticleRenderTypes.BLOOM_FACTOR) {
            return this;
        } else if (vertexformatelement.getType() == VertexFormatElement.Type.FLOAT && vertexformatelement.getCount() == 1) {
            this.putFloat(0,bloomFactor);
            this.nextElement();
            return this;
        }else{
            throw new IllegalStateException();
        }
    }

    /**
     * don't use this
     */
    @Override
    @Deprecated
    public MadParticleBufferBuilder color(int pColorARGB) {
        return (MadParticleBufferBuilder) super.color(pColorARGB);
    }

    @Override
    public void vertex(float pX, float pY, float pZ, float pRed, float pGreen, float pBlue, float pAlpha, float pTexU, float pTexV, int pOverlayUV, int pLightmapUV, float pNormalX, float pNormalY, float pNormalZ) {
        if (this.defaultColorSet) {
            throw new IllegalStateException();
        } else if (this.fastFormat) {
            this.putFloat(0, pX);
            this.putFloat(4, pY);
            this.putFloat(8, pZ);
            this.putFloat(12, pRed);
            this.putFloat(16, pGreen);
            this.putFloat(20, pBlue);
            this.putFloat(24, pAlpha);
            this.putFloat(28, pTexU);
            this.putFloat(32, pTexV);
            int i;
            if (this.fullFormat) {
                this.putShort(36, (short) (pOverlayUV & '\uffff'));
                this.putShort(38, (short) (pOverlayUV >> 16 & '\uffff'));
                i = 40;
            } else {
                i = 36;
            }

            this.putShort(i + 0, (short) (pLightmapUV & '\uffff'));
            this.putShort(i + 2, (short) (pLightmapUV >> 16 & '\uffff'));
            this.putByte(i + 4, BufferVertexConsumer.normalIntValue(pNormalX));
            this.putByte(i + 5, BufferVertexConsumer.normalIntValue(pNormalY));
            this.putByte(i + 6, BufferVertexConsumer.normalIntValue(pNormalZ));
            this.nextElementByte += i + 8;
            this.endVertex();
        } else {
            super.vertex(pX, pY, pZ, pRed, pGreen, pBlue, pAlpha, pTexU, pTexV, pOverlayUV, pLightmapUV, pNormalX, pNormalY, pNormalZ);
        }
    }

}
