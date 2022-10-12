package cn.ussshenzhou.madparticle.designer.gui.widegt;

import cn.ussshenzhou.madparticle.util.ColorHelper;
import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import static com.mojang.blaze3d.vertex.DefaultVertexFormat.ELEMENT_POSITION;

/**
 * the color chooser widget<p>
 * use color space HSB<p>
 * for modes support see {@link TColorChooser.HSB_MODE}<p>
 * for get result color in rgb {@link #getRgb()}<p>
 *
 * for places where feed vertices, must use counterclockwise, or they will be clipped
 */
public class TColorChooser extends AbstractWidget {

    private enum HSB_MODE {
        H("hue"), S("saturation"), B("brightness");
        private final String name;

        HSB_MODE(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public static ShaderInstance POSITION_HSB_SHADER;

    private static final VertexFormatElement HSB_ALPHA = new VertexFormatElement(0, VertexFormatElement.Type.FLOAT, VertexFormatElement.Usage.COLOR, 4);

    public static final VertexFormat HSB_VERTEX_FORMAT = new VertexFormat(ImmutableMap.<String, VertexFormatElement>builder().put("Position", ELEMENT_POSITION).put("HSB_ALPHA", HSB_ALPHA).build());

    /**
     * the length between the main and slide
     */
    private final int gap;
    /**
     * the slide width
     */
    private final int barWidth;
    /**
     * hue component, must range from 0f to 360f
     */
    private float h = 280f;
    /**
     * saturation component, must range from 0f to 1f
     */
    private float s = 1f;
    /**
     * the brightness component, must range from 0f to 1f
     */
    private float b = 1f;
    /**
     * thr alpha used for draw main and slide
     */
    private final float alpha = 0.8f;
    /**
     * the rgb transformed from hsb color space
     * [0x00rrggbb]
     */
    private int rgb;

    private HSB_MODE mode = HSB_MODE.H;

    public TColorChooser(int x, int y, int width, int height, int gap, int barWidth, Component message) {
        super(x, y, width, height, message);
        this.gap = gap;
        this.barWidth = barWidth;
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        Matrix4f pose = poseStack.last().pose();

        BufferBuilder builder = Tesselator.getInstance().getBuilder();
        drawHsbContext(pose, builder);

        renderInfo(poseStack, builder);
    }

    /**
     * have context for render hsb content
     */
    private void drawHsbContext(Matrix4f pose, BufferBuilder builder) {
        RenderSystem.setShader(() -> POSITION_HSB_SHADER);
        builder.begin(VertexFormat.Mode.QUADS, HSB_VERTEX_FORMAT);

        renderMain(pose, builder);
        renderSlide(pose, builder);
        renderColor(pose, builder);

        builder.end();
        BufferUploader.end(builder);
    }

    /**
     * render the main color, must be called in {@link #drawHsbContext(Matrix4f, BufferBuilder)}
     */
    private void renderMain(Matrix4f pose, BufferBuilder builder) {
        float _h = 0, _s = 0, _b = 0f;

        {
            //left-up corner
            switch (mode) {
                case H -> {
                    _h = h;
                    _s = 0f;
                    _b = 1f;
                }
                case S -> {
                    _h = 0f;
                    _s = s;
                    _b = 1f;
                }
                case B -> {
                    _h = 0f;
                    _s = 1f;
                    _b = b;
                }
            }
            builder.vertex(pose, x, y, 0.0f);
            putColor(builder, _h, _s, _b, alpha).nextElement();
            builder.endVertex();
        }

        {
            //left-down corner
            switch (mode) {
                case H -> {
                    _h = h;
                    _s = 0f;
                    _b = 0f;
                }
                case S -> {
                    _h = 0f;
                    _s = s;
                    _b = 0f;
                }
                case B -> {
                    _h = 0f;
                    _s = 0;
                    _b = b;
                }
            }
            builder.vertex(pose, x, y + height, 0.0f);
            putColor(builder, _h, _s, _b, alpha).nextElement();
            builder.endVertex();
        }

        {
            //right-down corner
            switch (mode) {
                case H -> {
                    _h = h;
                    _s = 1f;
                    _b = 0f;
                }
                case S -> {
                    _h = 360f;
                    _s = s;
                    _b = 0f;
                }
                case B -> {
                    _h = 360f;
                    _s = 0f;
                    _b = b;
                }
            }
            builder.vertex(pose, x + width, y + height, 0.0f);
            putColor(builder, _h, _s, _b, alpha).nextElement();
            builder.endVertex();
        }

        {
            //right-up corner
            switch (mode) {
                case H -> {
                    _h = h;
                    _s = 1f;
                    _b = 1f;
                }
                case S -> {
                    _h = 360f;
                    _s = s;
                    _b = 1f;
                }
                case B -> {
                    _h = 360f;
                    _s = 1f;
                    _b = b;
                }
            }

            builder.vertex(pose, x + width, y, 0.0f);
            putColor(builder, _h, _s, _b, alpha).nextElement();
            builder.endVertex();
        }
    }

    /**
     * render the slide, must be called in {@link #drawHsbContext(Matrix4f, BufferBuilder)}
     */
    private void renderSlide(Matrix4f pose, BufferBuilder builder) {
        float _h = 0f, _s = 0f, _b = 0f;
        var barX = x + width + gap;

        {
            //left-down and right-down corners
            switch (mode) {
                case H -> {
                    _h = 0f;
                    _s = 1f;
                    _b = 1f;
                }
                case S -> {
                    _h = h;
                    _s = 0f;
                    _b = b;
                }
                case B -> {
                    _h = h;
                    _s = s;
                    _b = 0f;
                }
            }
            builder.vertex(pose, barX, y + height, 0.0f);
            putColor(builder, _h, _s, _b, alpha).nextElement();
            builder.endVertex();

            builder.vertex(pose, barX + barWidth, y + height, 0.0f);
            putColor(builder, _h, _s, _b, alpha).nextElement();
            builder.endVertex();
        }

        {
            //up-right and up-left corners
            switch (mode) {
                case H -> {
                    _h = 360f;
                    _s = 1f;
                    _b = 1f;
                }
                case S -> {
                    _h = h;
                    _s = 1f;
                    _b = b;
                }
                case B -> {
                    _h = h;
                    _s = s;
                    _b = 1f;
                }
            }
            builder.vertex(pose, barX + barWidth, y, 0.0f);
            putColor(builder, _h, _s, _b, alpha).nextElement();
            builder.endVertex();

            builder.vertex(pose, barX, y, 0.0f);
            putColor(builder, _h, _s, _b, alpha).nextElement();
            builder.endVertex();
        }


    }

    /**
     * render hsb/rgb/mode info
     */
    private void renderInfo(PoseStack poseStack, BufferBuilder builder) {
        Font font = Minecraft.getInstance().font;
        var strX = x + width + gap + barWidth + 10;
        var strGapY = (int) Math.max(0, (height - 6f * font.lineHeight) / 5f) + font.lineHeight;
        drawString(poseStack, font, "h:" + (int) h + "°", strX, y, 0xffffffff);
        drawString(poseStack, font, "s:" + (int) (s * 100) + "%", strX, y + strGapY, 0xffffffff);
        drawString(poseStack, font, "b:" + (int) (b * 100) + "%", strX, y + strGapY * 2, 0xffffffff);
        drawString(poseStack, font, "r:" + ((rgb >> 16) & 0xff), strX, y + strGapY * 3, 0xffffffff);
        drawString(poseStack, font, "g:" + ((rgb >> 8) & 0xff), strX, y + strGapY * 4, 0xffffffff);
        drawString(poseStack, font, "b:" + (rgb & 0xff), strX, y + strGapY * 5, 0xffffffff);
        drawString(poseStack, font, "mode:" + mode, strX, y + strGapY * 6, 0xffffffff);
    }

    /**
     * render the indicator color, must be called in {@link #drawHsbContext(Matrix4f, BufferBuilder)}
     */
    private void renderColor(Matrix4f pose, BufferBuilder builder) {
        var colorX = x + width + gap + barWidth + 10 + 30;
        var colorSideLength = 20;
        builder.vertex(pose, colorX, y, 0.0f);
        putColor(builder, h, s, b, alpha).nextElement();
        builder.endVertex();

        builder.vertex(pose, colorX, y + colorSideLength, 0.0f);
        putColor(builder, h, s, b, alpha).nextElement();
        builder.endVertex();

        builder.vertex(pose, colorX + colorSideLength, y + colorSideLength, 0.0f);
        putColor(builder, h, s, b, alpha).nextElement();
        builder.endVertex();

        builder.vertex(pose, colorX + colorSideLength, y, 0.0f);
        putColor(builder, h, s, b, alpha).nextElement();
        builder.endVertex();

    }

    /**
     * put hsb color into BufferBuilder
     */
    private BufferBuilder putColor(BufferBuilder builder, float h, float s, float b, float alpha) {
        builder.putFloat(0, h);
        builder.putFloat(4, s);
        builder.putFloat(8, b);
        builder.putFloat(12, alpha);
        return builder;
    }

    /**
     * take the slide into account
     */
    @Override
    protected boolean clicked(double mouseX, double mouseY) {
        return this.active && this.visible && mouseY >= (double) this.y && mouseY < (double) (this.y + this.height) && ((mouseX >= (double) this.x && mouseX < (double) (this.x + this.width)) || ((mouseX >= (double) this.x + this.width + this.gap) && mouseX <= (double) (this.x + this.width + this.gap + this.barWidth)));
    }

    /**
     * change mode when right click
     */
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            mode = switch (mode) {
                case H -> HSB_MODE.S;
                case S -> HSB_MODE.B;
                case B -> HSB_MODE.H;
            };
            return true;
        } else return super.mouseClicked(mouseX, mouseY, button);
    }

    /**
     * calculate rgb color when hsb changed
     */
    private void refreshRGB() {
        rgb = ColorHelper.HSBtoRGB(h, s, b);
    }

    /**
     * helper method for normalized calculation, a period function with cycle of 2
     * <p>
     * y=x when x range from 0 to 1
     * <p>
     * y=2-x when x range from 1 to 2
     *
     * @param mouse the mouseX/Y
     * @param pos   x/y position for widget
     * @param size  width/height position for widget
     * @return the normalized user friend value
     */
    private static float normalizeMouse(double mouse, int pos, int size) {
        double x = mouse - pos;
        double y = x % size / size;
        if (y < 0) {
            x = -x;
            y = -y;
        }
        x /= size;
        return (float) (x % 2 > 1 ? 1 - y : y);
    }

    /**
     * modify when keep pressing the mouse
     */
    @Override
    public void onDrag(double mouseX, double mouseY, double dragX, double dragY) {
        float normalizedX = normalizeMouse(mouseX, x, width);
        var isBar = mouseX - x > width;
        float normalizedY = normalizeMouse(mouseY, y, height);
        switch (mode) {
            case H -> {
                if (isBar) {
                    h = (1.0f - normalizedY) * 360f;
                } else {
                    s = normalizedX;
                    b = 1.0f - normalizedY;
                }
            }
            case S -> {
                if (isBar) {
                    s = 1.0f - normalizedY;
                } else {
                    h = normalizedX * 360f;
                    b = 1.0f - normalizedY;
                }
            }
            case B -> {
                if (isBar) {
                    b = 1.0f - normalizedY;
                } else {
                    h = normalizedX * 360f;
                    s = normalizedY;
                }
            }
        }
        refreshRGB();
    }

    public float getH() {
        return h;
    }

    public float getS() {
        return s;
    }

    public float getB() {
        return b;
    }

    public int getRgb() {
        return rgb;
    }

    @Override
    public void updateNarration(NarrationElementOutput narrationElementOutput) {

    }
}