package cn.ussshenzhou.madparticle.designer.universal.widegt;

import cn.ussshenzhou.madparticle.designer.universal.util.AccessorProxy;
import cn.ussshenzhou.madparticle.designer.universal.util.HorizontalAlignment;
import cn.ussshenzhou.madparticle.designer.universal.util.Vec2i;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * @author USS_Shenzhou
 */
public class TSelectList<E> extends ObjectSelectionList<TSelectList<E>.Entry> implements TWidget {
    public static final int SCROLLBAR_WIDTH = 6;
    TComponent parent = null;
    int foreground = 0xffffffff;
    int background = 0x80000000;
    int selectedForeGround = foreground;
    boolean visible = true;
    int scrollbarGap;
    private HorizontalAlignment horizontalAlignment = HorizontalAlignment.CENTER;

    public TSelectList(int pItemHeight, int scrollbarGap) {
        super(Minecraft.getInstance(), 0, 0, 0, 0, pItemHeight);
        this.setRenderBackground(false);
        this.setRenderTopAndBottom(false);
        this.setRenderHeader(false, 0);
        this.setRenderSelection(false);
        this.scrollbarGap = scrollbarGap;
    }

    public TSelectList() {
        this(20, 0);
    }

    public void addElement(E element) {
        this.addEntry(new Entry(element));
    }

    public void addElement(E element, Consumer<TSelectList<E>> onSelected) {
        this.addEntry(new Entry(element, onSelected));
    }

    public void addElement(Collection<E> elements) {
        for (E e : elements) {
            this.addEntry(new Entry(e));
        }
    }

    public void removeElement(Entry entry) {
        this.removeEntry(entry);
    }

    public void clearElement() {
        super.children().clear();
    }

    public int getItemHeight() {
        return itemHeight;
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        this.updateScrollingState(pMouseX, pMouseY, pButton);
        if (!this.isMouseOver(pMouseX, pMouseY)) {
            return false;
        } else {
            Entry e = this.getEntryAtPosition(pMouseX, pMouseY);
            if (e != null) {
                if (e.mouseClicked(pMouseX, pMouseY, pButton)) {
                    this.setFocused(e);
                    this.setDragging(true);
                    this.setSelected(e);
                    this.ensureVisible(e);
                    return true;
                }
            } else if (pButton == 0) {
                this.clickedHeader((int) (pMouseX - (double) (this.x0 + this.width / 2 - this.getRowWidth() / 2)), (int) (pMouseY - (double) this.y0) + (int) this.getScrollAmount() - 4);
                return true;
            }
            return false;
        }
    }

    @Override
    public boolean mouseScrolled(double pMouseX, double pMouseY, double pDelta) {
        if (isInRange(pMouseX, pMouseY)) {
            return super.mouseScrolled(pMouseX, pMouseY, pDelta);
        } else {
            return false;
        }
    }

    @Override
    public boolean mouseDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY) {
        if (isInRange(pMouseX, pMouseY, 8, 8)) {
            return super.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY);
        } else {
            return false;
        }
    }

    @Override
    public void setSelected(@Nullable TSelectList<E>.Entry pSelected) {
        super.setSelected(pSelected);
        if (pSelected != null) {
            pSelected.onSelected();
        }
    }

    public void setSelected(int index) {
        this.setSelected(this.getEntry(index));
    }

    @Override
    protected int getScrollbarPosition() {
        return width + x0 - 6;
    }

    @Override
    public int getRowLeft() {
        return x0;
    }

    @Override
    public int getRowWidth() {
        return width - scrollbarGap - 6;
    }

    @Override
    protected void renderBackground(PoseStack pPoseStack) {
        fill(pPoseStack, x0, y0, x0 + width - scrollbarGap - 6, y0 + height, background);
    }

    @Override
    public boolean isVisible() {
        return visible;
    }

    @Override
    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    @Override
    public void setBounds(int x, int y, int width, int height) {
        if (parent != null) {
            this.x0 = x + parent.x;
            this.y0 = y + parent.y;
        } else {
            this.x0 = x;
            this.y0 = y;
        }
        this.x1 = x0 + width - scrollbarGap - 6;
        this.y1 = y0 + height;
        this.width = width;
        this.height = height;
    }

    @Override
    public void setAbsBounds(int x, int y, int width, int height) {
        this.x0 = x;
        this.y0 = y;
        this.x1 = x0 + width - scrollbarGap - 6;
        this.y1 = y0 + height;
        this.width = width;
        this.height = height;
    }

    /**
     * modified for compatibility with TScrollPanel
     *
     * @see net.minecraft.client.gui.components.AbstractSelectionList#render(PoseStack, int, int, float)
     */
    @Override
    public void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        this.renderBackground(pPoseStack);
        int i = this.getScrollbarPosition();
        int j = i + 6;
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tesselator.getBuilder();
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        AccessorProxy.AbstractSelectionListProxy.setHovered(this,
                this.isMouseOver((double) pMouseX, (double) pMouseY) ? this.getEntryAtPosition((double) pMouseX, (double) pMouseY) : null);
        if (AccessorProxy.AbstractSelectionListProxy.isRenderBackground(this)) {
            RenderSystem.setShaderTexture(0, GuiComponent.BACKGROUND_LOCATION);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            float f = 32.0F;
            bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
            bufferbuilder.vertex((double) this.x0, (double) this.y1, 0.0D).uv((float) this.x0 / 32.0F, (float) (this.y1 + (int) this.getScrollAmount()) / 32.0F).color(32, 32, 32, 255).endVertex();
            bufferbuilder.vertex((double) this.x1, (double) this.y1, 0.0D).uv((float) this.x1 / 32.0F, (float) (this.y1 + (int) this.getScrollAmount()) / 32.0F).color(32, 32, 32, 255).endVertex();
            bufferbuilder.vertex((double) this.x1, (double) this.y0, 0.0D).uv((float) this.x1 / 32.0F, (float) (this.y0 + (int) this.getScrollAmount()) / 32.0F).color(32, 32, 32, 255).endVertex();
            bufferbuilder.vertex((double) this.x0, (double) this.y0, 0.0D).uv((float) this.x0 / 32.0F, (float) (this.y0 + (int) this.getScrollAmount()) / 32.0F).color(32, 32, 32, 255).endVertex();
            tesselator.end();
        }

        int j1 = this.getRowLeft();
        int k = this.y0 + 4 - (int) this.getScrollAmount();
        if (AccessorProxy.AbstractSelectionListProxy.isRenderHeader(this)) {
            this.renderHeader(pPoseStack, j1, k, tesselator);
        }

        this.renderList(pPoseStack, j1, k, pMouseX, pMouseY, pPartialTick);
        if (AccessorProxy.AbstractSelectionListProxy.isRenderTopAndBottom(this)) {
            RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
            RenderSystem.setShaderTexture(0, GuiComponent.BACKGROUND_LOCATION);
            RenderSystem.enableDepthTest();
            RenderSystem.depthFunc(519);
            float f1 = 32.0F;
            int l = -100;
            bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
            bufferbuilder.vertex((double) this.x0, (double) this.y0, -100.0D).uv(0.0F, (float) this.y0 / 32.0F).color(64, 64, 64, 255).endVertex();
            bufferbuilder.vertex((double) (this.x0 + this.width), (double) this.y0, -100.0D).uv((float) this.width / 32.0F, (float) this.y0 / 32.0F).color(64, 64, 64, 255).endVertex();
            bufferbuilder.vertex((double) (this.x0 + this.width), 0.0D, -100.0D).uv((float) this.width / 32.0F, 0.0F).color(64, 64, 64, 255).endVertex();
            bufferbuilder.vertex((double) this.x0, 0.0D, -100.0D).uv(0.0F, 0.0F).color(64, 64, 64, 255).endVertex();
            bufferbuilder.vertex((double) this.x0, (double) this.height, -100.0D).uv(0.0F, (float) this.height / 32.0F).color(64, 64, 64, 255).endVertex();
            bufferbuilder.vertex((double) (this.x0 + this.width), (double) this.height, -100.0D).uv((float) this.width / 32.0F, (float) this.height / 32.0F).color(64, 64, 64, 255).endVertex();
            bufferbuilder.vertex((double) (this.x0 + this.width), (double) this.y1, -100.0D).uv((float) this.width / 32.0F, (float) this.y1 / 32.0F).color(64, 64, 64, 255).endVertex();
            bufferbuilder.vertex((double) this.x0, (double) this.y1, -100.0D).uv(0.0F, (float) this.y1 / 32.0F).color(64, 64, 64, 255).endVertex();
            tesselator.end();
            RenderSystem.depthFunc(515);
            RenderSystem.disableDepthTest();
            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE);
            RenderSystem.disableTexture();
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            int i1 = 4;
            bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
            bufferbuilder.vertex((double) this.x0, (double) (this.y0 + 4), 0.0D).color(0, 0, 0, 0).endVertex();
            bufferbuilder.vertex((double) this.x1, (double) (this.y0 + 4), 0.0D).color(0, 0, 0, 0).endVertex();
            bufferbuilder.vertex((double) this.x1, (double) this.y0, 0.0D).color(0, 0, 0, 255).endVertex();
            bufferbuilder.vertex((double) this.x0, (double) this.y0, 0.0D).color(0, 0, 0, 255).endVertex();
            bufferbuilder.vertex((double) this.x0, (double) this.y1, 0.0D).color(0, 0, 0, 255).endVertex();
            bufferbuilder.vertex((double) this.x1, (double) this.y1, 0.0D).color(0, 0, 0, 255).endVertex();
            bufferbuilder.vertex((double) this.x1, (double) (this.y1 - 4), 0.0D).color(0, 0, 0, 0).endVertex();
            bufferbuilder.vertex((double) this.x0, (double) (this.y1 - 4), 0.0D).color(0, 0, 0, 0).endVertex();
            tesselator.end();
        }

        int k1 = this.getMaxScroll();
        if (k1 > 0) {
            RenderSystem.disableTexture();
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            int l1 = (int) ((float) ((this.y1 - this.y0) * (this.y1 - this.y0)) / (float) this.getMaxPosition());
            l1 = Mth.clamp(l1, 32, this.y1 - this.y0 - 8);
            int i2 = (int) this.getScrollAmount() * (this.y1 - this.y0 - l1) / k1 + this.y0;
            if (i2 < this.y0) {
                i2 = this.y0;
            }
            //modified for compatibility with TScrollPanel
            double scrollAmount = 0;
            TScrollPanel tScrollPanel = getParentInstanceOf(TScrollPanel.class);
            if (tScrollPanel != null) {
                scrollAmount = -tScrollPanel.getScrollAmount();
            }

            bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
            bufferbuilder.vertex((double) i, scrollAmount + (double) this.y1, 0.0D).color(0, 0, 0, 255).endVertex();
            bufferbuilder.vertex((double) j, scrollAmount + (double) this.y1, 0.0D).color(0, 0, 0, 255).endVertex();
            bufferbuilder.vertex((double) j, scrollAmount + (double) this.y0, 0.0D).color(0, 0, 0, 255).endVertex();
            bufferbuilder.vertex((double) i, scrollAmount + (double) this.y0, 0.0D).color(0, 0, 0, 255).endVertex();
            bufferbuilder.vertex((double) i, scrollAmount + (double) (i2 + l1), 0.0D).color(128, 128, 128, 255).endVertex();
            bufferbuilder.vertex((double) j, scrollAmount + (double) (i2 + l1), 0.0D).color(128, 128, 128, 255).endVertex();
            bufferbuilder.vertex((double) j, scrollAmount + (double) i2, 0.0D).color(128, 128, 128, 255).endVertex();
            bufferbuilder.vertex((double) i, scrollAmount + (double) i2, 0.0D).color(128, 128, 128, 255).endVertex();
            bufferbuilder.vertex((double) i, scrollAmount + (double) (i2 + l1 - 1), 0.0D).color(192, 192, 192, 255).endVertex();
            bufferbuilder.vertex((double) (j - 1), scrollAmount + (double) (i2 + l1 - 1), 0.0D).color(192, 192, 192, 255).endVertex();
            bufferbuilder.vertex((double) (j - 1), scrollAmount + (double) i2, 0.0D).color(192, 192, 192, 255).endVertex();
            bufferbuilder.vertex((double) i, scrollAmount + (double) i2, 0.0D).color(192, 192, 192, 255).endVertex();
            tesselator.end();
        }

        this.renderDecorations(pPoseStack, pMouseX, pMouseY);
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }

    @Override
    protected void renderList(PoseStack pPoseStack, int pX, int pY, int pMouseX, int pMouseY, float pPartialTick) {
        int i = this.getItemCount();
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tesselator.getBuilder();

        for (int j = 0; j < i; ++j) {
            int k = this.getRowTop(j);
            int l = k + this.itemHeight;
            //modified not to render oust of box
            float up = k + (itemHeight - 10) / 2f;
            float low = k + 10;
            if (up >= this.y0 && low <= this.y1) {
                int i1 = pY + j * this.itemHeight + this.headerHeight;
                int j1 = this.itemHeight - 4;
                Entry e = this.getEntry(j);
                int k1 = this.getRowWidth();
                if (this.isSelectedItem(j)) {
                    //modified for compatibility with TScrollPanel
                    double scrollAmount = 0;
                    TScrollPanel tScrollPanel = getParentInstanceOf(TScrollPanel.class);
                    if (tScrollPanel != null) {
                        scrollAmount = -tScrollPanel.getScrollAmount();
                    }
                    //modified due to scrollbarGap
                    int l1 = this.x0 + (this.width - 6 - scrollbarGap) / 2 - k1 / 2;
                    int i2 = this.x0 + (this.width - 6 - scrollbarGap) / 2 + k1 / 2;
                    RenderSystem.disableTexture();
                    RenderSystem.setShader(GameRenderer::getPositionShader);
                    float f = this.isFocused() ? 1.0F : 0.5F;
                    RenderSystem.setShaderColor(f, f, f, 1.0F);
                    bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
                    bufferbuilder.vertex((double) l1, scrollAmount + (double) (i1 + j1 + 2), 0.0D).endVertex();
                    bufferbuilder.vertex((double) i2, scrollAmount + (double) (i1 + j1 + 2), 0.0D).endVertex();
                    bufferbuilder.vertex((double) i2, scrollAmount + (double) (i1 - 2), 0.0D).endVertex();
                    bufferbuilder.vertex((double) l1, scrollAmount + (double) (i1 - 2), 0.0D).endVertex();
                    tesselator.end();
                    RenderSystem.setShaderColor(0.0F, 0.0F, 0.0F, 1.0F);
                    bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
                    bufferbuilder.vertex((double) (l1 + 1), scrollAmount + (double) (i1 + j1 + 1), 0.0D).endVertex();
                    bufferbuilder.vertex((double) (i2 - 1), scrollAmount + (double) (i1 + j1 + 1), 0.0D).endVertex();
                    bufferbuilder.vertex((double) (i2 - 1), scrollAmount + (double) (i1 - 1), 0.0D).endVertex();
                    bufferbuilder.vertex((double) (l1 + 1), scrollAmount + (double) (i1 - 1), 0.0D).endVertex();
                    tesselator.end();
                    RenderSystem.enableTexture();
                }

                int j2 = this.getRowLeft();
                e.render(pPoseStack, j, k, j2, k1, j1, pMouseX, pMouseY, Objects.equals(getHovered(), e), pPartialTick);
            }
        }
        //super.renderList(pPoseStack, pX, pY, pMouseX, pMouseY, pPartialTick);
    }

    @Override
    public int getX() {
        return x0;
    }

    @Override
    public int getY() {
        return y0;
    }

    public void setScrollbarGap(int scrollbarGap) {
        this.scrollbarGap = scrollbarGap;
    }

    public int getScrollbarGap() {
        return scrollbarGap;
    }

    @Override
    public void setParent(TComponent parent) {
        this.parent = parent;
    }

    @Override
    public TComponent getParent() {
        return parent;
    }

    @Override
    public Vec2i getPreferredSize() {
        return null;
    }

    @Override
    public Vec2i getSize() {
        return new Vec2i(width, height);
    }

    @Override
    public void tick() {
    }

    public int getForeground() {
        return foreground;
    }

    public void setForeground(int foreground) {
        this.foreground = foreground;
    }

    public int getBackground() {
        return background;
    }

    public void setBackground(int background) {
        this.background = background;
    }

    public int getSelectedForeGround() {
        return selectedForeGround;
    }

    public void setSelectedForeGround(int selectedForeGround) {
        this.selectedForeGround = selectedForeGround;
    }

    public HorizontalAlignment getHorizontalAlignment() {
        return horizontalAlignment;
    }

    public void setHorizontalAlignment(HorizontalAlignment horizontalAlignment) {
        this.horizontalAlignment = horizontalAlignment;
    }

    private TSelectList<E> get() {
        return this;
    }

    public class Entry extends ObjectSelectionList.Entry<Entry> {
        E content;
        Consumer<TSelectList<E>> consumer;

        public Entry(E content, Consumer<TSelectList<E>> consumer) {
            this.content = content;
            this.consumer = consumer;
        }

        public Entry(E content) {
            this.content = content;
            this.consumer = list -> {
            };
        }

        @Override
        public Component getNarration() {
            Language language = Language.getInstance();
            if (language.has(content.toString())) {
                return new TranslatableComponent(content.toString());
            } else {
                return new TextComponent(content.toString());
            }
        }

        @Override
        public void render(PoseStack pPoseStack, int pIndex, int pTop, int pLeft, int pWidth, int pHeight, int pMouseX, int pMouseY, boolean pIsMouseOver, float pPartialTick) {
            Font font = Minecraft.getInstance().font;
            int color = getSelected() == this ? selectedForeGround : foreground;
            switch (horizontalAlignment) {
                case LEFT:
                    drawString(pPoseStack, font, getNarration(), pLeft + 1, pTop + (pHeight - font.lineHeight) / 2, color);
                    break;
                case RIGHT:
                    drawString(pPoseStack, font, getNarration(), pLeft + width - font.width(getNarration()) - 1, pTop + (pHeight - font.lineHeight) / 2, color);
                    break;
                default:
                    drawCenteredString(pPoseStack, font, getNarration(), pLeft + pWidth / 2, pTop + (pHeight - font.lineHeight) / 2, color);
            }
        }

        @Override
        public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
            return true;
        }

        public void onSelected() {
            consumer.accept(get());
        }

        public void setConsumer(Consumer<TSelectList<E>> consumer) {
            this.consumer = consumer;
        }

        public E getContent() {
            return content;
        }
    }

}
