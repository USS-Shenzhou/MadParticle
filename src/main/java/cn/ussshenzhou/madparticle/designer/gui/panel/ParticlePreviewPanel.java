package cn.ussshenzhou.madparticle.designer.gui.panel;

import cn.ussshenzhou.madparticle.mixin.ParticleEngineAccessor;
import cn.ussshenzhou.t88.gui.widegt.TPanel;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;

/**
 * @author Tony Yu
 */
public class ParticlePreviewPanel extends TPanel {
    TextureAtlasSprite textureAtlasSprite = null;
    float r = 1, g = 1, b = 1;

    public ParticlePreviewPanel() {
        super();
        this.setBackground(0x80000000);
    }

    public void updateParticle(String particleCommandText) {
        try {
            ParticleEngineAccessor particleEngineAccessor = (ParticleEngineAccessor) Minecraft.getInstance().particleEngine;
            SpriteSet spriteSet = particleEngineAccessor.getSpriteSets().get(ResourceLocation.parse(particleCommandText));
            if (spriteSet == null) {
                throw new Exception();
            }
            textureAtlasSprite = spriteSet.get(RandomSource.create());
        } catch (Exception ignored) {
            textureAtlasSprite = null;
        }
    }

    @Override
    public void render(GuiGraphics graphics, int pMouseX, int pMouseY, float pPartialTick) {
        if (border != null) {
            renderBorder(graphics, pMouseX, pMouseY, pPartialTick);
        }
        renderBackground(graphics, pMouseX, pMouseY, pPartialTick);
        if (textureAtlasSprite != null) {
            graphics.blitSprite(RenderType::guiTextured, textureAtlasSprite, x + 4, y + 4, 0, width - 8, height - 8);
        }
        renderChildren(graphics, pMouseX, pMouseY, pPartialTick);
    }

    public float getR() {
        return r;
    }

    public void setR(float r) {
        this.r = r;
    }

    public float getG() {
        return g;
    }

    public void setG(float g) {
        this.g = g;
    }

    public float getB() {
        return b;
    }

    public void setB(float b) {
        this.b = b;
    }
}
