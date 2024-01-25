package cn.ussshenzhou.madparticle.designer.gui;

import cn.ussshenzhou.madparticle.MadParticleConfig;
import cn.ussshenzhou.madparticle.designer.input.DesignerKeyInput;
import cn.ussshenzhou.t88.config.ConfigHelper;
import cn.ussshenzhou.t88.gui.advanced.TLabelButton;
import cn.ussshenzhou.t88.gui.screen.TScreen;
import cn.ussshenzhou.t88.gui.util.HorizontalAlignment;
import cn.ussshenzhou.t88.gui.util.LayoutHelper;
import cn.ussshenzhou.t88.gui.widegt.TButton;
import cn.ussshenzhou.t88.gui.widegt.TLabel;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;

/**
 * @author USS_Shenzhou
 */
public class WelcomeScreen extends TScreen {
    private static final String LINK = "https://holojaneway.uss-shenzhou.cn/madparticle";

    private final TLabel welcome = new TLabel(Component.translatable("gui.mp.wel.title"));
    private final TLabel text1 = new TLabel(Component.translatable("gui.mp.wel.text1",
            DesignerKeyInput.CALL_OUT_DESIGNER.getKeyModifier().getCombinedName(
                    DesignerKeyInput.CALL_OUT_DESIGNER.getKey(),
                    () -> DesignerKeyInput.CALL_OUT_DESIGNER.getKey().getDisplayName()).getString()
    ));
    private final TLabel text2 = new TLabel(Component.translatable("gui.mp.wel.text2"));
    private final TLabelButton text3 = new TLabelButton(Component.translatable("gui.mp.wel.text3"),
            pButton -> Util.getPlatform().openUri(LINK)
    );
    private final TButton ok = new TButton(Component.translatable("gui.mp.wel.ok"), pButton -> {
        this.onClose(true);
    });
    private final TButton hide = new TButton(Component.translatable("gui.mp.wel.hide"), pButton -> {
        ConfigHelper.getConfigWrite(MadParticleConfig.class, madParticleConfig ->
                madParticleConfig.noWelcomeScreen = true
        );
        this.onClose(true);
    });

    public WelcomeScreen() {
        super(Component.literal("madparticle_welcome_screen"));
        Minecraft.getInstance().mouseHandler.releaseMouse();
        welcome.setFontSize(21);
        this.add(welcome);
        text1.setLineSpacing(4);
        this.add(text1);
        text2.setLineSpacing(4);
        this.add(text2);
        text3.getButton().setTooltip(Tooltip.create(Component.literal(LINK)));
        text3.setHorizontalAlignment(HorizontalAlignment.CENTER);
        text3.setBorder(null);
        this.add(text3);
        this.add(ok);
        this.add(hide);
    }

    @Override
    public void layout() {
        welcome.setBounds((int) (width * 0.1), (int) (height * 0.1), welcome.getPreferredSize());
        LayoutHelper.BBottomOfA(text1, 10, welcome, text1.getPreferredSize());
        LayoutHelper.BBottomOfA(text2, 0, text1, text2.getPreferredSize());
        text3.setAbsBounds(
                text2.getXT() + text2.getSize().x + 2,
                text2.getYT() - 2,
                text3.getPreferredSize().add(4, 4)
        );
        //LayoutHelper.BRightOfA(text3, 6, text2, text3.getPreferredSize());
        hide.setBounds(width / 2 + 10, (int) (height * 0.8), 100, 20);
        LayoutHelper.BLeftOfA(ok, 10, hide);
        super.layout();
    }

    @Override
    protected void renderBackGround(GuiGraphics graphics, int pMouseX, int pMouseY, float pPartialTick) {
        graphics.fill(0, 0, width, height, 0x80000000);
    }
}
