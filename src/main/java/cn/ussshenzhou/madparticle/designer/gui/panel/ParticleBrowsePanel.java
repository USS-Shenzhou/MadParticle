package cn.ussshenzhou.madparticle.designer.gui.panel;

import cn.ussshenzhou.madparticle.mixin.ParticleEngineAccessor;
import cn.ussshenzhou.t88.gui.advanced.TLabelButton;
import cn.ussshenzhou.t88.gui.container.TVerticalScrollContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;

import java.util.ArrayList;

/**
 * @author USS_Shenzhou
 */
public class ParticleBrowsePanel extends TVerticalScrollContainer {
    private final ArrayList<ParticlePreview> previews = new ArrayList<>();
    private float r = 1, g = 1, b = 1;

    public ParticleBrowsePanel() {
        VanillaRegistries.createLookup().lookup(BuiltInRegistries.PARTICLE_TYPE.key()).ifPresent(particleTypeRegistryLookup -> {
            particleTypeRegistryLookup.listElements().forEach(particleTypeRef -> {
                var preview = new ParticlePreview(particleTypeRef.key().identifier());
                if (preview.spriteSet != null) {
                    previews.add(preview);
                }
            });
        });
        previews.forEach(this::add);
    }

    @Override
    public void layout() {
        int i = 0;
        int size = 96 / Minecraft.getInstance().getWindow().getGuiScale();
        int rowSize = Mth.floor((float) getUsableWidth() / size);
        int gap = (int) ((getUsableWidth() - rowSize * size) / (rowSize + 1f));
        for (ParticlePreview preview : previews) {
            preview.setBounds((i % rowSize) * (gap + size) + gap, (i / rowSize) * (gap + size) + gap, size, size);
            i++;
        }
        super.layout();
    }

    @Override
    public void tickT() {
        this.getParentInstanceOfOptional(HelperModePanel.class).ifPresent(modePanel -> {
            if (modePanel.parametersPanel == null) {
                return;
            }
            try {
                r = Float.parseFloat(modePanel.parametersPanel.r.getComponent().getValue());
                g = Float.parseFloat(modePanel.parametersPanel.g.getComponent().getValue());
                b = Float.parseFloat(modePanel.parametersPanel.b.getComponent().getValue());
            } catch (NumberFormatException e) {
                r = 1;
                g = 1;
                b = 1;
            }
        });
        super.tickT();
    }

    public class ParticlePreview extends TLabelButton {
        private final SpriteSet spriteSet;
        private TextureAtlasSprite sprite = null;
        private int age = 0;
        private static final int LIFE = 20 * 5;

        public ParticlePreview(Identifier particleLocation) {
            super(Component.empty());
            this.setTooltip(Tooltip.create(Component.literal(particleLocation.toString())));
            this.spriteSet = Minecraft.getInstance().particleEngine.resourceManager.spriteSets.get(particleLocation);
            if (this.spriteSet == null) {
                this.setText(Component.literal("?"));
            }
            this.setOnPress(t -> this.getParentInstanceOfOptional(HelperModePanel.class).ifPresent(modePanel -> {
                if (modePanel.parametersPanel != null) {
                    modePanel.parametersPanel.target.getComponent().getEditBox().setValue(particleLocation.toString());
                }
            }));
            this.setNormalBackGround(HelperModePanel.BACKGROUND);
        }

        @Override
        public void tickT() {
            if (spriteSet != null) {
                sprite = spriteSet.get(age % LIFE, LIFE);
                age++;
            }
            super.tickT();
        }

        @Override
        public void render(GuiGraphics guigraphics, int pMouseX, int pMouseY, float pPartialTick) {
            super.render(guigraphics, pMouseX, pMouseY, pPartialTick);
            if (sprite != null) {
                guigraphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, x + 4, y + 4, width - 8, height - 8, ARGB.colorFromFloat(1, r, g, b));
            }
        }
    }
}
