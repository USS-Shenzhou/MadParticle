package cn.ussshenzhou.madparticle.designer.universal.widegt;

import cn.ussshenzhou.madparticle.designer.universal.util.Vec2i;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
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
    boolean visible = true;
    int scrollbarGap;

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

    public int getItemHeight(){
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
        fill(pPoseStack, x0, y0, x0 + width - scrollbarGap - 6, y0 + height, 0x80000000);
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
                    //modified due to scrollbarGap
                    int l1 = this.x0 + (this.width - 6 - scrollbarGap) / 2 - k1 / 2;
                    int i2 = this.x0 + (this.width - 6 - scrollbarGap) / 2 + k1 / 2;
                    RenderSystem.disableTexture();
                    RenderSystem.setShader(GameRenderer::getPositionShader);
                    float f = this.isFocused() ? 1.0F : 0.5F;
                    RenderSystem.setShaderColor(f, f, f, 1.0F);
                    bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
                    bufferbuilder.vertex((double) l1, (double) (i1 + j1 + 2), 0.0D).endVertex();
                    bufferbuilder.vertex((double) i2, (double) (i1 + j1 + 2), 0.0D).endVertex();
                    bufferbuilder.vertex((double) i2, (double) (i1 - 2), 0.0D).endVertex();
                    bufferbuilder.vertex((double) l1, (double) (i1 - 2), 0.0D).endVertex();
                    tesselator.end();
                    RenderSystem.setShaderColor(0.0F, 0.0F, 0.0F, 1.0F);
                    bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
                    bufferbuilder.vertex((double) (l1 + 1), (double) (i1 + j1 + 1), 0.0D).endVertex();
                    bufferbuilder.vertex((double) (i2 - 1), (double) (i1 + j1 + 1), 0.0D).endVertex();
                    bufferbuilder.vertex((double) (i2 - 1), (double) (i1 - 1), 0.0D).endVertex();
                    bufferbuilder.vertex((double) (l1 + 1), (double) (i1 - 1), 0.0D).endVertex();
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

    public void setForeground(int foreground) {
        this.foreground = foreground;
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
            //TODO
            drawCenteredString(pPoseStack, font, getNarration(), pLeft + pWidth / 2, pTop + (pHeight - font.lineHeight) / 2, foreground);
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
    }

}
