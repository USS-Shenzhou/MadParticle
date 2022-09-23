package cn.ussshenzhou.madparticle.designer.gui.panel;

import cn.ussshenzhou.madparticle.command.inheritable.InheritableIntegerArgument;
import cn.ussshenzhou.madparticle.designer.gui.DesignerScreen;
import cn.ussshenzhou.madparticle.designer.universal.combine.TTitledCycleButton;
import cn.ussshenzhou.madparticle.designer.universal.combine.TTitledSimpleConstrainedEditBox;
import cn.ussshenzhou.madparticle.designer.universal.combine.TTitledSuggestedEditBox;
import cn.ussshenzhou.madparticle.designer.universal.util.ArgumentSuggestionsDispatcher;
import cn.ussshenzhou.madparticle.designer.universal.util.LayoutHelper;
import cn.ussshenzhou.madparticle.designer.universal.util.Vec2i;
import cn.ussshenzhou.madparticle.designer.universal.widegt.TButton;
import cn.ussshenzhou.madparticle.designer.universal.widegt.TScrollPanel;
import cn.ussshenzhou.madparticle.particle.ParticleRenderTypes;
import cn.ussshenzhou.madparticle.particle.SpriteFrom;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ParticleArgument;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

import java.util.stream.Stream;

/**
 * @author USS_Shenzhou
 */
@SuppressWarnings("AlibabaCommentsMustBeJavadocFormat")
public class ParametersScrollPanel extends TScrollPanel {
    //public static final Vec2i EDITBOX_SIZE = new Vec2i(35, 36);
    public static final Vec2i BUTTON_SIZE = TButton.RECOMMEND_SIZE;
    private boolean isChild;

    //lane 1
    public final TTitledSuggestedEditBox target = new TTitledSuggestedEditBox(
            new TranslatableComponent("gui.mp.de.helper.target"), new ArgumentSuggestionsDispatcher<>());
    public final TButton tryDefault = new TButton(new TranslatableComponent("gui.mp.de.helper.try_default"));
    //lane 2
    public final TTitledCycleButton<SpriteFrom> spriteFrom = new TTitledCycleButton<>(new TranslatableComponent("gui.mp.de.helper.sprite"));
    public final TTitledSimpleConstrainedEditBox lifeTime = new TTitledSimpleConstrainedEditBox(
            new TranslatableComponent("gui.mp.de.helper.life"), IntegerArgumentType.integer(0));
    public final TTitledCycleButton<String> alwaysRender = new TTitledCycleButton<>(new TranslatableComponent("gui.mp.de.helper.always"));
    public final TTitledSimpleConstrainedEditBox amount = new TTitledSimpleConstrainedEditBox(
            new TranslatableComponent("gui.mp.de.helper.amount"), IntegerArgumentType.integer(0));
    public final TTitledCycleButton<ParticleRenderTypes> renderType = new TTitledCycleButton<>(new TranslatableComponent("gui.mp.de.helper.render_type"));
    public final TTitledSuggestedEditBox whoCanSee = new TTitledSuggestedEditBox(
            new TranslatableComponent("gui.mp.de.helper.who_see"), new ArgumentSuggestionsDispatcher<>());
    //lane 3
    public final TTitledSimpleConstrainedEditBox
            xPos = new TTitledSimpleConstrainedEditBox(new TextComponent("X"), DoubleArgumentType.doubleArg()),
            yPos = new TTitledSimpleConstrainedEditBox(new TextComponent("Y"), DoubleArgumentType.doubleArg()),
            zPos = new TTitledSimpleConstrainedEditBox(new TextComponent("Z"), DoubleArgumentType.doubleArg()),
            xD = new TTitledSimpleConstrainedEditBox(new TranslatableComponent("gui.mp.de.helper.x_diffuse"), DoubleArgumentType.doubleArg()),
            yD = new TTitledSimpleConstrainedEditBox(new TranslatableComponent("gui.mp.de.helper.y_diffuse"), DoubleArgumentType.doubleArg()),
            zD = new TTitledSimpleConstrainedEditBox(new TranslatableComponent("gui.mp.de.helper.z_diffuse"), DoubleArgumentType.doubleArg());
    //lane 4
    public final TTitledSimpleConstrainedEditBox
            vx = new TTitledSimpleConstrainedEditBox(new TextComponent("Vx"), DoubleArgumentType.doubleArg()),
            vy = new TTitledSimpleConstrainedEditBox(new TextComponent("Vy"), DoubleArgumentType.doubleArg()),
            vz = new TTitledSimpleConstrainedEditBox(new TextComponent("Vz"), DoubleArgumentType.doubleArg()),
            vxD = new TTitledSimpleConstrainedEditBox(new TranslatableComponent("gui.mp.de.helper.vx_diffuse"), DoubleArgumentType.doubleArg()),
            vyD = new TTitledSimpleConstrainedEditBox(new TranslatableComponent("gui.mp.de.helper.vy_diffuse"), DoubleArgumentType.doubleArg()),
            vzD = new TTitledSimpleConstrainedEditBox(new TranslatableComponent("gui.mp.de.helper.vz_diffuse"), DoubleArgumentType.doubleArg());

    public ParametersScrollPanel() {
        super();
        init1();
        init2();
        init3();
        init4();
        this.setChild(false);
    }

    public void init1() {
        ((ArgumentSuggestionsDispatcher<ParticleOptions>) target.getComponent().getEditBox().getDispatcher())
                .register(Commands.argument("p", ParticleArgument.particle()));

        tryDefault.setOnPress(pButton -> {
            //TODO
        });
        this.addAll(target, tryDefault);
    }

    public void init2() {
        Stream.of(SpriteFrom.values()).forEach(spriteFrom::addElement);
        alwaysRender.addElement("gui.mp.de.helper.false");
        alwaysRender.addElement("gui.mp.de.helper.true");
        Stream.of(ParticleRenderTypes.values()).forEach(renderType::addElement);
        ((ArgumentSuggestionsDispatcher<EntitySelector>) whoCanSee.getComponent().getEditBox().getDispatcher())
                .register(Commands.argument("p", EntityArgument.players()));
        this.addAll(spriteFrom, lifeTime, alwaysRender, amount, renderType, whoCanSee);
    }

    public void init3() {
        this.addAll(xPos, yPos, zPos, xD, yD, zD);
    }

    public void init4() {
        this.addAll(vx, vy, vz, vxD, vyD, vzD);
    }

    @Override
    public void layout() {
        int xGap = 5;
        Vec2i buttonSize = BUTTON_SIZE.copy();
        Vec2i stdTitledEditBox = calculateStdTitledEditBox(buttonSize, xGap);
        while (stdTitledEditBox.x < 35) {
            if (xGap > 2) {
                xGap--;
            } else if (buttonSize.x > 35) {
                buttonSize.add(-1, 0);
            } else {
                break;
            }
            stdTitledEditBox = calculateStdTitledEditBox(buttonSize, xGap);
        }
        Vec2i stdTitledButton = buttonSize.copy();
        stdTitledButton.add(0, 12);
        int yGap = xGap - 1;
        //lane 1
        target.setBounds(xGap, yGap, getUsableWidth() - xGap * 3 - buttonSize.x * 2, stdTitledEditBox.y);
        LayoutHelper.BRightOfA(tryDefault, xGap, target, buttonSize.x * 2, buttonSize.y);
        LayoutHelper.BBottomOfA(tryDefault, -8, tryDefault);
        //lane 2
        LayoutHelper.BBottomOfA(spriteFrom, yGap, target, stdTitledButton);
        LayoutHelper.BRightOfA(lifeTime, xGap, spriteFrom, stdTitledEditBox);
        LayoutHelper.BRightOfA(alwaysRender, xGap, lifeTime, stdTitledButton);
        LayoutHelper.BRightOfA(amount, xGap, alwaysRender, stdTitledEditBox);
        LayoutHelper.BRightOfA(renderType, xGap, amount, stdTitledButton);
        LayoutHelper.BRightOfA(whoCanSee, xGap, renderType,
                Minecraft.getInstance().screen.width - renderType.getX() - renderType.getWidth() - 2 * xGap - DesignerScreen.GAP, stdTitledEditBox.y);
        //lane3
        LayoutHelper.BBottomOfA(xPos, yGap, spriteFrom, stdTitledEditBox);
        LayoutHelper.BRightOfA(yPos, xGap, xPos);
        LayoutHelper.BRightOfA(zPos, xGap, yPos);
        LayoutHelper.BRightOfA(xD, xGap, zPos);
        LayoutHelper.BRightOfA(yD, xGap, xD);
        LayoutHelper.BRightOfA(zD, xGap, yD);
        //lane 4
        LayoutHelper.BBottomOfA(vx, yGap, xPos);
        LayoutHelper.BRightOfA(vy, xGap, vx);
        LayoutHelper.BRightOfA(vz, xGap, vy);
        LayoutHelper.BRightOfA(vxD, xGap, vz);
        LayoutHelper.BRightOfA(vyD, xGap, vxD);
        LayoutHelper.BRightOfA(vzD, xGap, vyD);
        super.layout();
    }

    private Vec2i calculateStdTitledEditBox(Vec2i size, int gap) {
        return new Vec2i(
                (getUsableWidth() - size.x - 7 * gap) / 7,
                20 + 12
        );
    }

    public boolean isChild() {
        return isChild;
    }

    public void setChild(boolean child) {
        isChild = child;
        if (child) {
            spriteFrom.getComponent().addElement(SpriteFrom.INHERIT);
            lifeTime.getComponent().setArgument(InheritableIntegerArgument.inheritableInteger(0, Integer.MAX_VALUE));
        } else {
            spriteFrom.getComponent().removeElement(SpriteFrom.INHERIT);
            lifeTime.getComponent().setArgument(IntegerArgumentType.integer(0));
        }
    }

    @Override
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        return super.keyPressed(pKeyCode, pScanCode, pModifiers);
    }
}
