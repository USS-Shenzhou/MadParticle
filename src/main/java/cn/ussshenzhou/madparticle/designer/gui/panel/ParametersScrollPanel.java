package cn.ussshenzhou.madparticle.designer.gui.panel;

import cn.ussshenzhou.madparticle.command.inheritable.InheritableIntegerArgument;
import cn.ussshenzhou.madparticle.designer.universal.combine.TTitledCycleButton;
import cn.ussshenzhou.madparticle.designer.universal.combine.TTitledSimpleConstrainedEditBox;
import cn.ussshenzhou.madparticle.designer.universal.util.LayoutHelper;
import cn.ussshenzhou.madparticle.designer.universal.util.Vec2i;
import cn.ussshenzhou.madparticle.designer.universal.widegt.TButton;
import cn.ussshenzhou.madparticle.designer.universal.widegt.TScrollPanel;
import cn.ussshenzhou.madparticle.particle.SpriteFrom;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.arguments.ParticleArgument;
import net.minecraft.network.chat.TranslatableComponent;

import java.util.stream.Stream;

/**
 * @author USS_Shenzhou
 */
public class ParametersScrollPanel extends TScrollPanel {
    //public static final Vec2i EDITBOX_SIZE = new Vec2i(35, 36);
    public static final Vec2i BUTTON_SIZE = TButton.RECOMMEND_SIZE;
    private boolean isChild;

    public final TTitledSimpleConstrainedEditBox target = new TTitledSimpleConstrainedEditBox(
            new TranslatableComponent("gui.mp.de.helper.target"), ParticleArgument.particle());
    public final TButton tryDefault = new TButton(new TranslatableComponent("gui.mp.de.helper.try_default"));
    public final TTitledCycleButton<SpriteFrom> spriteFrom = new TTitledCycleButton<>(new TranslatableComponent("gui.mp.de.helper.sprite"));
    public final TTitledSimpleConstrainedEditBox lifeTime = new TTitledSimpleConstrainedEditBox(
            new TranslatableComponent("gui.mp.de.helper.life"), IntegerArgumentType.integer(0));

    public ParametersScrollPanel() {
        super();
        tryDefault.setOnPress(pButton -> {
            //TODO
        });
        Stream.of(SpriteFrom.values()).forEach(s -> spriteFrom.getComponent().addElement(s));


        this.add(target);
        this.add(tryDefault);
        this.add(spriteFrom);
        this.add(lifeTime);

        this.setChild(false);
    }

    @Override
    public void layout() {
        int gap = 5;
        Vec2i buttonSize = BUTTON_SIZE.copy();
        Vec2i stdTitledEditBox = calculateStdTitledEditBox(buttonSize, gap);
        while (stdTitledEditBox.x < 35) {
            if (gap > 2) {
                gap--;
            } else if (buttonSize.x > 35) {
                buttonSize.add(-1, 0);
            } else {
                break;
            }
            stdTitledEditBox = calculateStdTitledEditBox(buttonSize, gap);
        }
        Vec2i titledButtonSize = buttonSize.copy();
        titledButtonSize.add(0, 16);
        //lane 1
        target.setBounds(gap, gap, getUsableWidth() - gap * 3 - buttonSize.x * 2, stdTitledEditBox.y);
        LayoutHelper.BRightOfA(tryDefault, gap, target, buttonSize.x * 2, buttonSize.y);
        LayoutHelper.BBottomOfA(tryDefault, -4, tryDefault);
        //lane 2
        LayoutHelper.BBottomOfA(spriteFrom, gap, target, titledButtonSize);
        LayoutHelper.BRightOfA(lifeTime, gap, spriteFrom, stdTitledEditBox);
        super.layout();
    }

    private Vec2i calculateStdTitledEditBox(Vec2i size, int gap) {
        return new Vec2i(
                (getUsableWidth() - size.x - 7 * gap) / 7,
                36
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
}
