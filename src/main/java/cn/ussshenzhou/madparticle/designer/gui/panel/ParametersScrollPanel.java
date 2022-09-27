package cn.ussshenzhou.madparticle.designer.gui.panel;

import cn.ussshenzhou.madparticle.command.inheritable.InheritableBoolean;
import cn.ussshenzhou.madparticle.command.inheritable.InheritableIntegerArgument;
import cn.ussshenzhou.madparticle.designer.universal.combine.TTitledCycleButton;
import cn.ussshenzhou.madparticle.designer.universal.combine.TTitledSimpleConstrainedEditBox;
import cn.ussshenzhou.madparticle.designer.universal.combine.TTitledSuggestedEditBox;
import cn.ussshenzhou.madparticle.designer.universal.util.ArgumentSuggestionsDispatcher;
import cn.ussshenzhou.madparticle.designer.universal.util.LayoutHelper;
import cn.ussshenzhou.madparticle.designer.universal.util.Vec2i;
import cn.ussshenzhou.madparticle.designer.universal.widegt.TButton;
import cn.ussshenzhou.madparticle.designer.universal.widegt.TScrollPanel;
import cn.ussshenzhou.madparticle.designer.universal.widegt.TSlider;
import cn.ussshenzhou.madparticle.particle.ChangeMode;
import cn.ussshenzhou.madparticle.particle.ParticleRenderTypes;
import cn.ussshenzhou.madparticle.particle.SpriteFrom;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
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
    private boolean isChild = false;

    //lane 1
    public final TTitledSuggestedEditBox target = new TTitledSuggestedEditBox(
            new TranslatableComponent("gui.mp.de.helper.target"), new ArgumentSuggestionsDispatcher<>());
    public final TButton tryDefault = new TButton(new TranslatableComponent("gui.mp.de.helper.try_default"));
    //lane 2
    public final TTitledCycleButton<SpriteFrom> spriteFrom = new TTitledCycleButton<>(new TranslatableComponent("gui.mp.de.helper.sprite"));
    public final TTitledSimpleConstrainedEditBox lifeTime = new TTitledSimpleConstrainedEditBox(
            new TranslatableComponent("gui.mp.de.helper.life"), IntegerArgumentType.integer(0));
    public final TTitledCycleButton<InheritableBoolean> alwaysRender = new TTitledCycleButton<>(new TranslatableComponent("gui.mp.de.helper.always"));
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
    public final ParticlePreviewPanel particlePreview = new ParticlePreviewPanel();
    //lane 4
    public final TTitledSimpleConstrainedEditBox
            vx = new TTitledSimpleConstrainedEditBox(new TextComponent("Vx"), DoubleArgumentType.doubleArg()),
            vy = new TTitledSimpleConstrainedEditBox(new TextComponent("Vy"), DoubleArgumentType.doubleArg()),
            vz = new TTitledSimpleConstrainedEditBox(new TextComponent("Vz"), DoubleArgumentType.doubleArg()),
            vxD = new TTitledSimpleConstrainedEditBox(new TranslatableComponent("gui.mp.de.helper.vx_diffuse"), DoubleArgumentType.doubleArg()),
            vyD = new TTitledSimpleConstrainedEditBox(new TranslatableComponent("gui.mp.de.helper.vy_diffuse"), DoubleArgumentType.doubleArg()),
            vzD = new TTitledSimpleConstrainedEditBox(new TranslatableComponent("gui.mp.de.helper.vz_diffuse"), DoubleArgumentType.doubleArg());
    //lane 5
    public final TTitledSimpleConstrainedEditBox
            r = new TTitledSimpleConstrainedEditBox(new TextComponent("R"), FloatArgumentType.floatArg()),
            g = new TTitledSimpleConstrainedEditBox(new TextComponent("G"), FloatArgumentType.floatArg()),
            b = new TTitledSimpleConstrainedEditBox(new TextComponent("B"), FloatArgumentType.floatArg());
    public final TSlider
            rSlider = new TSlider(0, 1, 0.01f, new TranslatableComponent("gui.mp.de.helper.r")),
            gSlider = new TSlider(0, 1, 0.01f, new TranslatableComponent("gui.mp.de.helper.g")),
            bSlider = new TSlider(0, 1, 0.01f, new TranslatableComponent("gui.mp.de.helper.b"));
    //lane 6
    public final TTitledCycleButton<InheritableBoolean> interact = new TTitledCycleButton<>(new TranslatableComponent("gui.mp.de.helper.interact"));
    public final TTitledSimpleConstrainedEditBox
            horizontalInteract = new TTitledSimpleConstrainedEditBox(
            new TranslatableComponent("gui.mp.de.helper.horizontal_interact"), DoubleArgumentType.doubleArg()),
            verticalInteract = new TTitledSimpleConstrainedEditBox(
                    new TranslatableComponent("gui.mp.de.helper.vertical_interact"), DoubleArgumentType.doubleArg()),
            friction = new TTitledSimpleConstrainedEditBox(
                    new TranslatableComponent("gui.mp.de.helper.friction"), FloatArgumentType.floatArg()),
            friction2 = new TTitledSimpleConstrainedEditBox(
                    new TranslatableComponent("gui.mp.de.helper.friction_after"), FloatArgumentType.floatArg()),
            gravity = new TTitledSimpleConstrainedEditBox(
                    new TranslatableComponent("gui.mp.de.helper.gravity"), FloatArgumentType.floatArg()),
            gravity2 = new TTitledSimpleConstrainedEditBox(
                    new TranslatableComponent("gui.mp.de.helper.gravity_after"), FloatArgumentType.floatArg());
    //lane 7
    public final TTitledCycleButton<InheritableBoolean> collision = new TTitledCycleButton<>(new TranslatableComponent("gui.mp.de.helper.collision"));
    public final TTitledSimpleConstrainedEditBox
            horizontalCollision = new TTitledSimpleConstrainedEditBox(
            new TranslatableComponent("gui.mp.de.helper.horizontal_diffuse"), DoubleArgumentType.doubleArg()),
            verticalCollision = new TTitledSimpleConstrainedEditBox(
                    new TranslatableComponent("gui.mp.de.helper.vertical_bounce"), DoubleArgumentType.doubleArg()),
            collisionTime = new TTitledSimpleConstrainedEditBox(
                    new TranslatableComponent("gui.mp.de.helper.collision_time"), IntegerArgumentType.integer(0)),
            xDeflection = new TTitledSimpleConstrainedEditBox(
                    new TranslatableComponent("gui.mp.de.helper.x_deflection"), FloatArgumentType.floatArg()),
            xDeflection2 = new TTitledSimpleConstrainedEditBox(
                    new TranslatableComponent("gui.mp.de.helper.x_deflection_after"), FloatArgumentType.floatArg()),
            zDeflection = new TTitledSimpleConstrainedEditBox(
                    new TranslatableComponent("gui.mp.de.helper.z_deflection"), FloatArgumentType.floatArg()),
            zDeflection2 = new TTitledSimpleConstrainedEditBox(
                    new TranslatableComponent("gui.mp.de.helper.z_deflection_after"), FloatArgumentType.floatArg());
    //lane 8
    public final TTitledCycleButton<ChangeMode>
            alpha = new TTitledCycleButton<>(new TranslatableComponent("gui.mp.de.helper.alpha")),
            scale = new TTitledCycleButton<>(new TranslatableComponent("gui.mp.de.helper.scale"));
    public final TTitledSimpleConstrainedEditBox
            roll = new TTitledSimpleConstrainedEditBox(new TranslatableComponent("gui.mp.de.helper.roll_speed"), FloatArgumentType.floatArg()),
            alphaBegin = new TTitledSimpleConstrainedEditBox(new TranslatableComponent("gui.mp.de.helper.alpha_begin"), FloatArgumentType.floatArg()),
            alphaEnd = new TTitledSimpleConstrainedEditBox(new TranslatableComponent("gui.mp.de.helper.alpha_end"), FloatArgumentType.floatArg()),
            scaleBegin = new TTitledSimpleConstrainedEditBox(new TranslatableComponent("gui.mp.de.helper.scale_begin"), FloatArgumentType.floatArg()),
            scaleEnd = new TTitledSimpleConstrainedEditBox(new TranslatableComponent("gui.mp.de.helper.scale_end"), FloatArgumentType.floatArg());


    public ParametersScrollPanel() {
        super();
        init1();
        init2();
        init3();
        init4();
        init5();
        init6();
        init7();
        init8();
        setChild(false);
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
        Stream.of(InheritableBoolean.values()).forEach(alwaysRender::addElement);
        Stream.of(ParticleRenderTypes.values()).forEach(renderType::addElement);
        ((ArgumentSuggestionsDispatcher<EntitySelector>) whoCanSee.getComponent().getEditBox().getDispatcher())
                .register(Commands.argument("p", EntityArgument.players()));
        this.addAll(spriteFrom, lifeTime, alwaysRender, amount, renderType, whoCanSee);
    }

    public void init3() {
        this.addAll(xPos, yPos, zPos, xD, yD, zD, particlePreview);
    }

    public void init4() {
        this.addAll(vx, vy, vz, vxD, vyD, vzD);
    }

    public void init5() {
        this.addAll(r, g, b, rSlider, gSlider, bSlider);
    }

    public void init6() {
        Stream.of(InheritableBoolean.values()).forEach(interact::addElement);
        this.addAll(interact, horizontalInteract, verticalInteract, friction, friction2, gravity, gravity2);
    }

    public void init7() {
        Stream.of(InheritableBoolean.values()).forEach(collision::addElement);
        this.addAll(collision, horizontalCollision, verticalCollision, collisionTime, xDeflection, xDeflection2, zDeflection, zDeflection2);
    }

    public void init8() {
        Stream.of(ChangeMode.values()).forEach(alpha::addElement);
        Stream.of(ChangeMode.values()).forEach(scale::addElement);
        this.addAll(alpha, scale, roll, alphaBegin, alphaEnd, scaleBegin, scaleEnd);
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
        int w = Minecraft.getInstance().screen.width;
        LayoutHelper.BRightOfA(whoCanSee, xGap, renderType,
                w - renderType.getX() - renderType.getWidth() - 2 * xGap - (w - getX() - getUsableWidth()), stdTitledEditBox.y);
        //lane3
        LayoutHelper.BBottomOfA(xPos, yGap, spriteFrom, stdTitledEditBox);
        LayoutHelper.BRightOfA(yPos, xGap, xPos);
        LayoutHelper.BRightOfA(zPos, xGap, yPos);
        LayoutHelper.BRightOfA(xD, xGap, zPos);
        LayoutHelper.BRightOfA(yD, xGap, xD);
        LayoutHelper.BRightOfA(zD, xGap, yD);
        int previewW = getX() + getUsableWidth() - zD.getX() - zD.getWidth() - 2 * xGap;
        int previewH = stdTitledEditBox.y * 2 + yGap;
        int previewL = Math.min(previewH, previewW);
        LayoutHelper.BRightOfA(particlePreview, xGap, zD, previewL, previewL);
        //lane 4
        LayoutHelper.BBottomOfA(vx, yGap, xPos);
        LayoutHelper.BRightOfA(vy, xGap, vx);
        LayoutHelper.BRightOfA(vz, xGap, vy);
        LayoutHelper.BRightOfA(vxD, xGap, vz);
        LayoutHelper.BRightOfA(vyD, xGap, vxD);
        LayoutHelper.BRightOfA(vzD, xGap, vyD);
        //lane 5
        int l = (getUsableWidth() - 7 * xGap - 3 * stdTitledEditBox.x) / 3;
        LayoutHelper.BBottomOfA(r, yGap, vx, stdTitledEditBox);
        LayoutHelper.BRightOfA(g, xGap, r, l, stdTitledEditBox.y);
        LayoutHelper.BRightOfA(g, xGap, g, stdTitledEditBox);
        LayoutHelper.BRightOfA(b, xGap, g, l, stdTitledEditBox.y);
        LayoutHelper.BRightOfA(b, xGap, b, stdTitledEditBox);

        LayoutHelper.BRightOfA(rSlider, xGap, r, l, rSlider.getPreferredSize().y);
        LayoutHelper.BBottomOfA(rSlider, 12 - rSlider.getPreferredSize().y, rSlider);
        LayoutHelper.BRightOfA(gSlider, xGap, g, l, gSlider.getPreferredSize().y);
        LayoutHelper.BBottomOfA(gSlider, 12 - gSlider.getPreferredSize().y, gSlider);
        LayoutHelper.BRightOfA(bSlider, xGap, b, l, bSlider.getPreferredSize().y);
        LayoutHelper.BBottomOfA(bSlider, 12 - bSlider.getPreferredSize().y, bSlider);
        //lane 6
        LayoutHelper.BBottomOfA(interact, yGap, r, stdTitledButton);
        LayoutHelper.BRightOfA(horizontalInteract, xGap, interact, stdTitledEditBox);
        LayoutHelper.BRightOfA(verticalInteract, xGap, horizontalInteract);
        LayoutHelper.BRightOfA(friction, xGap, verticalInteract);
        LayoutHelper.BRightOfA(friction, xGap, friction);
        LayoutHelper.BRightOfA(friction2, xGap, friction);
        LayoutHelper.BRightOfA(gravity, xGap, friction2);
        LayoutHelper.BRightOfA(gravity2, xGap, gravity);
        //lane 7
        LayoutHelper.BBottomOfA(collision, yGap, interact);
        LayoutHelper.BRightOfA(horizontalCollision, xGap, collision, stdTitledEditBox);
        LayoutHelper.BRightOfA(verticalCollision, xGap, horizontalCollision);
        LayoutHelper.BRightOfA(collisionTime, xGap, verticalCollision);
        LayoutHelper.BRightOfA(xDeflection, xGap, collisionTime);
        LayoutHelper.BRightOfA(xDeflection2, xGap, xDeflection);
        LayoutHelper.BRightOfA(zDeflection, xGap, xDeflection2);
        LayoutHelper.BRightOfA(zDeflection2, xGap, zDeflection);
        //lane 8
        LayoutHelper.BBottomOfA(roll, yGap, collision, stdTitledEditBox);
        LayoutHelper.BBottomOfA(scaleEnd, yGap, zDeflection2, stdTitledEditBox);
        LayoutHelper.BLeftOfA(scaleBegin, xGap, scaleEnd);
        LayoutHelper.BLeftOfA(scale, xGap, scaleBegin, stdTitledButton);
        LayoutHelper.BLeftOfA(alphaEnd, xGap, scale, stdTitledEditBox);
        LayoutHelper.BLeftOfA(alphaBegin, xGap, alphaEnd);
        LayoutHelper.BLeftOfA(alpha, xGap, alphaBegin, stdTitledButton);

        super.layout();
    }

    private Vec2i calculateStdTitledEditBox(Vec2i size, int gap) {
        return new Vec2i(
                (getUsableWidth() - size.x - 8 * gap) / 7,
                20 + 12
        );
    }

    public boolean isChild() {
        return isChild;
    }

    public void setChild(boolean child) {
        //TODO
        isChild = child;
        if (child) {
            spriteFrom.addElement(SpriteFrom.INHERIT);
            lifeTime.getComponent().setArgument(InheritableIntegerArgument.inheritableInteger(0, Integer.MAX_VALUE));
            alwaysRender.addElement(InheritableBoolean.INHERIT);
        } else {
            spriteFrom.removeElement(SpriteFrom.INHERIT);
            lifeTime.getComponent().setArgument(IntegerArgumentType.integer(0));
            alwaysRender.removeElement(InheritableBoolean.INHERIT);
        }
    }
}
