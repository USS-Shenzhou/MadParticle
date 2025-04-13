package cn.ussshenzhou.madparticle.designer.gui.panel;

import cn.ussshenzhou.madparticle.command.inheritable.*;
import cn.ussshenzhou.madparticle.designer.gui.widegt.MetaParameterPanel;
import cn.ussshenzhou.madparticle.designer.gui.widegt.SingleVec3EditBox;
import cn.ussshenzhou.madparticle.mixin.EditBoxAccessor;
import cn.ussshenzhou.madparticle.mixin.ParticleAccessor;
import cn.ussshenzhou.madparticle.mixin.ParticleEngineAccessor;
import cn.ussshenzhou.madparticle.particle.enums.ChangeMode;
import cn.ussshenzhou.madparticle.particle.enums.ParticleRenderTypes;
import cn.ussshenzhou.madparticle.particle.enums.SpriteFrom;
import cn.ussshenzhou.madparticle.util.MathHelper;
import cn.ussshenzhou.t88.gui.combine.TTitledComponent;
import cn.ussshenzhou.t88.gui.combine.TTitledCycleButton;
import cn.ussshenzhou.t88.gui.combine.TTitledSimpleConstrainedEditBox;
import cn.ussshenzhou.t88.gui.combine.TTitledSuggestedEditBox;
import cn.ussshenzhou.t88.gui.util.AccessorProxy;
import cn.ussshenzhou.t88.gui.util.ArgumentSuggestionsDispatcher;
import cn.ussshenzhou.t88.gui.util.LayoutHelper;
import net.neoforged.neoforge.client.ClientCommandHandler;
import org.joml.Vector2i;
import cn.ussshenzhou.t88.gui.widegt.TButton;
import cn.ussshenzhou.t88.gui.widegt.TEditBox;
import cn.ussshenzhou.t88.gui.widegt.TScrollPanel;
import cn.ussshenzhou.t88.gui.widegt.TSlider;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.particle.Particle;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ParticleArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.neoforged.fml.ModList;

import java.text.DecimalFormat;
import java.util.Map;
import java.util.stream.Stream;

/**
 * @author USS_Shenzhou
 */
@SuppressWarnings("AlibabaCommentsMustBeJavadocFormat")
public class ParametersScrollPanel extends TScrollPanel {
    public static final Vector2i BUTTON_SIZE = TButton.RECOMMEND_SIZE;
    protected boolean isChild = false;

    //lane 1
    public final TTitledSuggestedEditBox target = new TTitledSuggestedEditBox(
            Component.translatable("gui.mp.de.helper.target"), new ArgumentSuggestionsDispatcher<>()) {
        @Override
        public void render(GuiGraphics graphics, int pMouseX, int pMouseY, float pPartialTick) {
            super.render(graphics, pMouseX, pMouseY, pPartialTick);
            var list = this.getComponent().getSuggestionList();
            if (list.isVisibleT()) {
                list.render(graphics, pMouseX, pMouseY, pPartialTick);
            }
        }
    };
    public final TButton tryDefault = new TButton(Component.translatable("gui.mp.de.helper.try_default"));
    //lane 2
    public final TTitledCycleButton<SpriteFrom> spriteFrom = new TTitledCycleButton<>(Component.translatable("gui.mp.de.helper.sprite"));
    public final TTitledSimpleConstrainedEditBox lifeTime = new TTitledSimpleConstrainedEditBox(
            Component.translatable("gui.mp.de.helper.life"), IntegerArgumentType.integer(0));
    public final TTitledCycleButton<InheritableBoolean> alwaysRender = new TTitledCycleButton<>(Component.translatable("gui.mp.de.helper.always"));
    public final TTitledSimpleConstrainedEditBox amount = new TTitledSimpleConstrainedEditBox(
            Component.translatable("gui.mp.de.helper.amount"), IntegerArgumentType.integer(0));
    public final TTitledCycleButton<ParticleRenderTypes> renderType = new TTitledCycleButton<>(Component.translatable("gui.mp.de.helper.render_type"));
    public final TTitledSuggestedEditBox whoCanSee = new TTitledSuggestedEditBox(
            Component.translatable("gui.mp.de.helper.who_see"), new ArgumentSuggestionsDispatcher<>());
    //lane 3
    public final SingleVec3EditBox
            xPos = new SingleVec3EditBox(Component.literal("X")),
            yPos = new SingleVec3EditBox(Component.literal("Y")),
            zPos = new SingleVec3EditBox(Component.literal("Z")),
            xD = new SingleVec3EditBox(Component.translatable("gui.mp.de.helper.x_diffuse")),
            yD = new SingleVec3EditBox(Component.translatable("gui.mp.de.helper.y_diffuse")),
            zD = new SingleVec3EditBox(Component.translatable("gui.mp.de.helper.z_diffuse"));
    public final ParticlePreviewPanel particlePreview = new ParticlePreviewPanel();
    //lane 4
    public final SingleVec3EditBox
            vx = new SingleVec3EditBox(Component.literal("Vx")),
            vy = new SingleVec3EditBox(Component.literal("Vy")),
            vz = new SingleVec3EditBox(Component.literal("Vz")),
            vxD = new SingleVec3EditBox(Component.translatable("gui.mp.de.helper.vx_diffuse")),
            vyD = new SingleVec3EditBox(Component.translatable("gui.mp.de.helper.vy_diffuse")),
            vzD = new SingleVec3EditBox(Component.translatable("gui.mp.de.helper.vz_diffuse"));
    //lane 5
    public final TTitledSimpleConstrainedEditBox
            r = new TTitledSimpleConstrainedEditBox(Component.literal("R"), FloatArgumentType.floatArg()),
            g = new TTitledSimpleConstrainedEditBox(Component.literal("G"), FloatArgumentType.floatArg()),
            b = new TTitledSimpleConstrainedEditBox(Component.literal("B"), FloatArgumentType.floatArg());
    public final TSlider
            rSlider = new TSlider(0, 1, 0.01f, Component.translatable("gui.mp.de.helper.r")),
            gSlider = new TSlider(0, 1, 0.01f, Component.translatable("gui.mp.de.helper.g")),
            bSlider = new TSlider(0, 1, 0.01f, Component.translatable("gui.mp.de.helper.b"));
    //lane 6
    public final TTitledCycleButton<InheritableBoolean> collision = new TTitledCycleButton<>(Component.translatable("gui.mp.de.helper.collision"));
    public final TTitledSimpleConstrainedEditBox
            horizontalCollision = new TTitledSimpleConstrainedEditBox(
            Component.translatable("gui.mp.de.helper.horizontal_diffuse"), DoubleArgumentType.doubleArg()),
            verticalCollision = new TTitledSimpleConstrainedEditBox(
                    Component.translatable("gui.mp.de.helper.vertical_bounce"), DoubleArgumentType.doubleArg()),
            collisionTime = new TTitledSimpleConstrainedEditBox(
                    Component.translatable("gui.mp.de.helper.collision_time"), IntegerArgumentType.integer(0)),
            xDeflection = new TTitledSimpleConstrainedEditBox(
                    Component.translatable("gui.mp.de.helper.x_deflection"), FloatArgumentType.floatArg()),
            xDeflection2 = new TTitledSimpleConstrainedEditBox(
                    Component.translatable("gui.mp.de.helper.x_deflection_after"), FloatArgumentType.floatArg()),
            zDeflection = new TTitledSimpleConstrainedEditBox(
                    Component.translatable("gui.mp.de.helper.z_deflection"), FloatArgumentType.floatArg()),
            zDeflection2 = new TTitledSimpleConstrainedEditBox(
                    Component.translatable("gui.mp.de.helper.z_deflection_after"), FloatArgumentType.floatArg());
    //lane 7
    public final TTitledCycleButton<InheritableBoolean> interact = new TTitledCycleButton<>(Component.translatable("gui.mp.de.helper.interact"));
    public final TTitledSimpleConstrainedEditBox
            roll = new TTitledSimpleConstrainedEditBox(
            Component.translatable("gui.mp.de.helper.roll_speed"), FloatArgumentType.floatArg()),
            horizontalInteract = new TTitledSimpleConstrainedEditBox(
                    Component.translatable("gui.mp.de.helper.horizontal_interact"), DoubleArgumentType.doubleArg()),
            verticalInteract = new TTitledSimpleConstrainedEditBox(
                    Component.translatable("gui.mp.de.helper.vertical_interact"), DoubleArgumentType.doubleArg()),
            friction = new TTitledSimpleConstrainedEditBox(
                    Component.translatable("gui.mp.de.helper.friction"), FloatArgumentType.floatArg()),
            friction2 = new TTitledSimpleConstrainedEditBox(
                    Component.translatable("gui.mp.de.helper.friction_after"), FloatArgumentType.floatArg()),
            gravity = new TTitledSimpleConstrainedEditBox(
                    Component.translatable("gui.mp.de.helper.gravity"), FloatArgumentType.floatArg()),
            gravity2 = new TTitledSimpleConstrainedEditBox(
                    Component.translatable("gui.mp.de.helper.gravity_after"), FloatArgumentType.floatArg());
    //lane 8
    public final TTitledCycleButton<ChangeMode>
            alpha = new TTitledCycleButton<>(Component.translatable("gui.mp.de.helper.alpha")),
            scale = new TTitledCycleButton<>(Component.translatable("gui.mp.de.helper.scale"));
    public final TTitledSimpleConstrainedEditBox
            bloomStrength = new TTitledSimpleConstrainedEditBox(Component.translatable("gui.mp.de.helper.bloom_factor"), FloatArgumentType.floatArg(1, 255)),
            alphaBegin = new TTitledSimpleConstrainedEditBox(Component.translatable("gui.mp.de.helper.alpha_begin"), FloatArgumentType.floatArg()),
            alphaEnd = new TTitledSimpleConstrainedEditBox(Component.translatable("gui.mp.de.helper.alpha_end"), FloatArgumentType.floatArg()),
            scaleBegin = new TTitledSimpleConstrainedEditBox(Component.translatable("gui.mp.de.helper.scale_begin"), FloatArgumentType.floatArg()),
            scaleEnd = new TTitledSimpleConstrainedEditBox(Component.translatable("gui.mp.de.helper.scale_end"), FloatArgumentType.floatArg());

    //meta
    public final MetaParameterPanel metaPanel = new MetaParameterPanel();

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
        //setChild(true);
        initTooltip();
        this.add(metaPanel);
    }

    public void initTooltip() {
        if ("zh_cn".equals(Minecraft.getInstance().getLanguageManager().getSelected())) {
            return;
        }
        Map.ofEntries(
                Map.entry(xD, "gui.mp.de.helper.x_diffuse.tooltip"),
                Map.entry(yD, "gui.mp.de.helper.y_diffuse.tooltip"),
                Map.entry(zD, "gui.mp.de.helper.z_diffuse.tooltip"),
                Map.entry(vxD, "gui.mp.de.helper.vx_diffuse.tooltip"),
                Map.entry(vyD, "gui.mp.de.helper.vx_diffuse.tooltip"),
                Map.entry(vzD, "gui.mp.de.helper.vx_diffuse.tooltip"),
                Map.entry(horizontalCollision, "gui.mp.de.helper.horizontal_diffuse.tooltip"),
                Map.entry(verticalCollision, "gui.mp.de.helper.vertical_bounce.tooltip"),
                Map.entry(collisionTime, "gui.mp.de.helper.collision_time.tooltip"),
                Map.entry(friction, "gui.mp.de.helper.friction.tooltip"),
                Map.entry(friction2, "gui.mp.de.helper.friction_after.tooltip"),
                Map.entry(gravity, "gui.mp.de.helper.gravity.tooltip"),
                Map.entry(gravity2, "gui.mp.de.helper.gravity_after.tooltip"),
                Map.entry(roll, "gui.mp.de.helper.roll_speed.tooltip"),
                Map.entry(interact, "gui.mp.de.helper.interact.tooltip"),
                Map.entry(horizontalInteract, "gui.mp.de.helper.horizontal_interact.tooltip"),
                Map.entry(verticalInteract, "gui.mp.de.helper.vertical_interact.tooltip"),
                Map.entry(xDeflection, "gui.mp.de.helper.x_deflection.tooltip"),
                Map.entry(xDeflection2, "gui.mp.de.helper.x_deflection_after.tooltip"),
                Map.entry(zDeflection, "gui.mp.de.helper.z_deflection.tooltip"),
                Map.entry(zDeflection2, "gui.mp.de.helper.z_deflection_after.tooltip"),
                Map.entry(scaleBegin, "gui.mp.de.helper.scale_begin.tooltip"),
                Map.entry(scaleEnd, "gui.mp.de.helper.scale_end.tooltip"),
                Map.entry(alwaysRender, "gui.mp.de.helper.always.tooltip"),
                Map.entry(renderType, "gui.mp.de.helper.render_type.tooltip")
        ).forEach((w, t) -> w.getComponent().setTooltip(Tooltip.create(Component.translatable(t))));
    }

    public void init1() {
        ((ArgumentSuggestionsDispatcher<ParticleOptions>) target.getComponent().getEditBox().getDispatcher())
                .register(Commands.argument("p", ParticleArgument.particle(Commands.createValidationContext(VanillaRegistries.createLookup()))));
        target.getComponent().getEditBox().setMaxLength(255);
        target.getComponent().getEditBox().addResponder(particlePreview::updateParticle);
        tryDefault.setOnPress(pButton -> tryFillDefault());
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
        //xControlX2(xPos, xD);
        //xControlX2(yPos, yD);
        //xControlX2(zPos, zD);
    }

    public void init4() {
        this.addAll(vx, vy, vz, vxD, vyD, vzD);
        //xControlX2(vx, vxD);
        //xControlX2(vy, vyD);
        //xControlX2(vz, vzD);
    }

    private void xControlX2(TTitledSimpleConstrainedEditBox controller, TTitledSimpleConstrainedEditBox controlled) {
        controller.getComponent().addPassedResponder(s -> {
            controlled.getComponent().setEditable(!s.contains("="));
        });
    }

    public void init5() {
        this.addAll(r, g, b, rSlider, gSlider, bSlider);
        r.getComponent().addPassedResponder(s -> {
            float f = Float.parseFloat(s);
            particlePreview.setR(f);
            rSlider.setValueWithoutRespond(f);
        });
        g.getComponent().addPassedResponder(s -> {
            float f = Float.parseFloat(s);
            particlePreview.setG(f);
            gSlider.setValueWithoutRespond(f);
        });
        b.getComponent().addPassedResponder(s -> {
            float f = Float.parseFloat(s);
            particlePreview.setB(Float.parseFloat(s));
            bSlider.setValueWithoutRespond(f);
        });
        rSlider.addResponder(d -> {
            r.getComponent().setValue(String.format("%.3f", d));
            AccessorProxy.EditBoxProxy.setDisplayPos(r.getComponent(), 0);
        });
        gSlider.addResponder(d -> {
            g.getComponent().setValue(String.format("%.3f", d));
            AccessorProxy.EditBoxProxy.setDisplayPos(g.getComponent(), 0);
        });
        bSlider.addResponder(d -> {
            b.getComponent().setValue(String.format("%.3f", d));
            AccessorProxy.EditBoxProxy.setDisplayPos(b.getComponent(), 0);
        });
        rSlider.setValue(1);
        gSlider.setValue(1);
        bSlider.setValue(1);
    }

    public void init6() {
        this.addAll(collision, horizontalCollision, verticalCollision, collisionTime, xDeflection, xDeflection2, zDeflection, zDeflection2);
        collision.addElement(InheritableBoolean.TRUE, button -> {
            Stream.of(horizontalCollision, verticalCollision, collisionTime).forEach(e -> e.getComponent().setEditable(true));
        });
        collision.addElement(InheritableBoolean.FALSE, button -> {
            Stream.of(horizontalCollision, verticalCollision, collisionTime).forEach(e -> e.getComponent().setEditable(false));
        });
    }

    public void init7() {
        this.addAll(roll, interact, horizontalInteract, verticalInteract, friction, friction2, gravity, gravity2);
        interact.addElement(InheritableBoolean.TRUE, button -> {
            Stream.of(horizontalInteract, verticalInteract).forEach(e -> e.getComponent().setEditable(true));
        });
        interact.addElement(InheritableBoolean.FALSE, button -> {
            Stream.of(horizontalInteract, verticalInteract).forEach(e -> e.getComponent().setEditable(false));
        });
    }

    public void init8() {
        Stream.of(ChangeMode.values()).forEach(alpha::addElement);
        Stream.of(ChangeMode.values()).forEach(scale::addElement);
        this.addAll(bloomStrength, alpha, scale, alphaBegin, alphaEnd, scaleBegin, scaleEnd);
    }

    @Override
    public void layout() {
        int xGap = 5;
        Vector2i buttonSize = MathHelper.copy(BUTTON_SIZE);
        Vector2i stdTitledEditBox = calculateStdTitledEditBox(buttonSize, xGap);
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
        Vector2i stdTitledButton = MathHelper.copy(buttonSize);
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
        int w = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        LayoutHelper.BRightOfA(whoCanSee, xGap, renderType,
                w - renderType.getXT() - renderType.getWidth() - 2 * xGap - (w - getXT() - getUsableWidth()), stdTitledEditBox.y);
        //lane3
        LayoutHelper.BBottomOfA(xPos, yGap, spriteFrom, stdTitledEditBox);
        LayoutHelper.BRightOfA(yPos, xGap, xPos);
        LayoutHelper.BRightOfA(zPos, xGap, yPos);
        LayoutHelper.BRightOfA(xD, xGap, zPos);
        LayoutHelper.BRightOfA(yD, xGap, xD);
        LayoutHelper.BRightOfA(zD, xGap, yD);
        int previewW = getXT() + getUsableWidth() - zD.getXT() - zD.getWidth() - 2 * xGap;
        int previewH = stdTitledEditBox.y * 2 + yGap;
        int previewL = Math.min(previewH, previewW);
        LayoutHelper.BRightOfA(particlePreview, 0, zD, previewL, previewL);
        LayoutHelper.BRightOfA(particlePreview, (previewW - previewL) / 2 - previewL, particlePreview);
        LayoutHelper.BBottomOfA(particlePreview, 6 - previewL, particlePreview);
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
        LayoutHelper.BBottomOfA(collision, yGap, r, stdTitledButton);
        LayoutHelper.BRightOfA(horizontalCollision, xGap, collision, stdTitledEditBox);
        LayoutHelper.BRightOfA(verticalCollision, xGap, horizontalCollision);
        LayoutHelper.BRightOfA(collisionTime, xGap, verticalCollision);
        LayoutHelper.BRightOfA(friction, xGap, collisionTime);
        LayoutHelper.BRightOfA(friction2, xGap, friction);
        LayoutHelper.BRightOfA(gravity, xGap, friction2);
        LayoutHelper.BRightOfA(gravity2, xGap, gravity);
        //lane 7
        LayoutHelper.BBottomOfA(roll, yGap, collision, stdTitledEditBox);
        LayoutHelper.BRightOfA(interact, xGap, roll, stdTitledButton);
        LayoutHelper.BRightOfA(horizontalInteract, xGap, interact, stdTitledEditBox);
        LayoutHelper.BRightOfA(verticalInteract, xGap, horizontalInteract);
        LayoutHelper.BRightOfA(xDeflection, xGap, verticalInteract);
        LayoutHelper.BRightOfA(xDeflection2, xGap, xDeflection);
        LayoutHelper.BRightOfA(zDeflection, xGap, xDeflection2);
        LayoutHelper.BRightOfA(zDeflection2, xGap, zDeflection);
        //lane 8
        LayoutHelper.BBottomOfA(bloomStrength, yGap, roll, stdTitledEditBox);
        LayoutHelper.BBottomOfA(scaleEnd, yGap, zDeflection2, stdTitledEditBox);
        LayoutHelper.BLeftOfA(scaleBegin, xGap, scaleEnd);
        LayoutHelper.BLeftOfA(scale, xGap, scaleBegin, stdTitledButton);
        LayoutHelper.BLeftOfA(alphaEnd, xGap, scale, stdTitledEditBox);
        LayoutHelper.BLeftOfA(alphaBegin, xGap, alphaEnd);
        LayoutHelper.BLeftOfA(alpha, xGap, alphaBegin, stdTitledButton);
        //meta
        metaPanel.passGap(xGap, yGap);
        LayoutHelper.BBottomOfA(metaPanel, 2 * yGap, bloomStrength, getUsableWidth() - xGap, metaPanel.getPreferredSize().y);
        super.layout();
    }

    //TODO set bloomStrength

    protected Vector2i calculateStdTitledEditBox(Vector2i size, int gap) {
        return new Vector2i(
                (getUsableWidth() - size.x - 8 * gap) / 7,
                20 + 12
        );
    }

    public boolean isChild() {
        return isChild;
    }

    public void setChild(boolean child) {
        isChild = child;
        if (child) {
            spriteFrom.addElement(SpriteFrom.INHERIT);
            lifeTime.getComponent().setArgument(InheritableIntegerArgument.inheritableInteger(0, Integer.MAX_VALUE));
            alwaysRender.addElement(InheritableBoolean.INHERIT);
            amount.getComponent().setArgument(InheritableIntegerArgument.inheritableInteger(0, Integer.MAX_VALUE));
            Stream.of(xPos, yPos, zPos, vx, vy, vz).forEach(editBox -> editBox.getComponent().setArgument(InheritableVec3Argument.inheritableVec3()));
            Stream.of(xD, yD, zD, vxD, vyD, vzD).forEach(editBox -> editBox.getComponent().setArgument(Vec3Argument.vec3(false)));
            collision.addElement(InheritableBoolean.INHERIT, button -> {
                Stream.of(horizontalCollision, verticalCollision, collisionTime).forEach(editBox -> {
                    editBox.getComponent().setEditable(true);
                    ifClearThenSet(editBox, "=");
                });
            });
            interact.addElement(InheritableBoolean.INHERIT, button -> {
                Stream.of(horizontalInteract, verticalInteract).forEach(editBox -> {
                    editBox.getComponent().setEditable(true);
                    ifClearThenSet(editBox, "=");
                });
            });
            collisionTime.getComponent().setArgument(InheritableIntegerArgument.inheritableInteger(0, Integer.MAX_VALUE));
            Stream.of(horizontalCollision, verticalCollision, horizontalInteract, verticalInteract).forEach(editBox -> editBox.getComponent().setArgument(InheritableDoubleArgument.inheritableDouble()));
            roll.getComponent().setArgument(InheritableFloatArgument.inheritableFloat());
            interact.addElement(InheritableBoolean.INHERIT);
            Stream.of(r, g, b).forEach(editBox -> editBox.getComponent().setArgument(InheritableFloatArgument.inheritableFloat()));
            bloomStrength.getComponent().setArgument(InheritableFloatArgument.inheritableFloat(1, 255));
            Stream.of(alpha, scale).forEach(button -> button.addElement(ChangeMode.INHERIT));
        } else {
            spriteFrom.removeElement(SpriteFrom.INHERIT);
            lifeTime.getComponent().setArgument(IntegerArgumentType.integer(0));
            alwaysRender.removeElement(InheritableBoolean.INHERIT);
            amount.getComponent().setArgument(IntegerArgumentType.integer(0));
            Stream.of(xPos, yPos, zPos, xD, yD, zD, vx, vy, vz, vxD, vyD, vzD).forEach(editBox -> editBox.getComponent().setArgument(Vec3Argument.vec3()));
            collision.removeElement(InheritableBoolean.INHERIT);
            interact.removeElement(InheritableBoolean.INHERIT);
            collisionTime.getComponent().setArgument(IntegerArgumentType.integer(0));
            Stream.of(horizontalCollision, verticalCollision, horizontalInteract, verticalInteract).forEach(editBox -> editBox.getComponent().setArgument(DoubleArgumentType.doubleArg()));
            roll.getComponent().setArgument(FloatArgumentType.floatArg());
            Stream.of(r, g, b).forEach(editBox -> editBox.getComponent().setArgument(FloatArgumentType.floatArg()));
            bloomStrength.getComponent().setArgument(FloatArgumentType.floatArg(1, 255));
            Stream.of(alpha, scale).forEach(button -> button.removeElement(ChangeMode.INHERIT));
        }
    }

    protected void tryFillDefault() {
        ArgumentSuggestionsDispatcher<ParticleOptions> dispatcher = new ArgumentSuggestionsDispatcher<>();
        dispatcher.register(Commands.argument("particle", ParticleArgument.particle(Commands.createValidationContext(VanillaRegistries.createLookup()))));
        CommandSourceStack sourceStack = ClientCommandHandler.getSource();
        String value = target.getComponent().getEditBox().getValue();
        ParseResults<CommandSourceStack> parseResults = dispatcher.parse(value, sourceStack);
        CommandContext<CommandSourceStack> ct = parseResults.getContext().build(value);
        try {
            ParticleOptions particleOptions = ct.getArgument("particle", ParticleOptions.class);
            Particle particle = ((ParticleEngineAccessor) Minecraft.getInstance().particleEngine).callMakeParticle(particleOptions, 0, 0, 0, 0, 0, 0);
            if (particle != null) {
                ParticleAccessor accessor = (ParticleAccessor) particle;
                ifClearThenSet(lifeTime, particle.getLifetime());
                ifClearThenSet(amount, 5);
                alwaysRender.getComponent().select(InheritableBoolean.FALSE);
                ifClearThenSet(whoCanSee.getComponent().getEditBox(), "@a");
                Stream.of(xPos, yPos, zPos).forEach(
                        titled -> ifClearThenSet(titled, isChild ? "=" : "~")
                );
                Stream.of(vx, vy, vz).forEach(
                        titled -> ifClearThenSet(titled, isChild ? "=" : "0.0")
                );
                Stream.of(xD, yD, zD, vxD, vyD, vzD).forEach(
                        titled -> ifClearThenSet(titled, "0.0")
                );
                ifClearThenSet(r, accessor.getRCol());
                ifClearThenSet(g, accessor.getGCol());
                ifClearThenSet(b, accessor.getBCol());
                ifClearThenSet(accessor.getFriction(), friction, friction2);
                ifClearThenSet(accessor.getGravity(), gravity, gravity2);
                ifClearThenSet(gravity2, accessor.getGravity());
                collision.getComponent().select(InheritableBoolean.FALSE);
                interact.getComponent().select(InheritableBoolean.FALSE);
                Stream.of(horizontalInteract, verticalInteract, xDeflection, xDeflection2, zDeflection, zDeflection2).forEach(editBox ->
                        ifClearThenSet(editBox, 0));
                ifClearThenSet(roll, accessor.getRoll());
                ifClearThenSet(accessor.getAlpha(), alphaBegin, alphaEnd);
                ifClearThenSet(String.format("%.2f", (accessor.getBbHeight() + accessor.getBbWidth()) / 2 / 0.2), scaleBegin, scaleEnd);
                ifClearThenSet(bloomStrength, isChild ? "=" : "1");
            }
        } catch (Exception ignored) {
        }
    }

    protected <V> void ifClearThenSet(TTitledComponent<? extends TEditBox> tTitled, V value) {
        ifClearThenSet(tTitled.getComponent(), value);
    }

    private <V> void ifClearThenSet(V value, TTitledComponent<? extends TEditBox>... tTitled) {
        Stream.of(tTitled).forEach(t -> ifClearThenSet(t.getComponent(), value));
    }

    /*private <V> void ifClearThenSet(V value, TEditBox... editBoxes) {
        Stream.of(editBoxes).forEach(editBox -> ifClearThenSet(editBox, value));
    }*/

    private <V> void ifClearThenSet(TEditBox editBox, V value) {
        if (
                (editBox.getValue().isEmpty() || ((EditBoxAccessor) editBox).getTextColor() == 0x37e2ff)
                        && ((EditBoxAccessor) editBox).isIsEditable()
        ) {
            if (value instanceof Double || value instanceof Float) {
                DecimalFormat df = new DecimalFormat("#.###");
                editBox.setValue(df.format(value));
            } else {
                editBox.setValue(value.toString());
            }
            editBox.setTextColor(0x37e2ff);
        }
    }

    public String wrap() {
        StringBuilder builder = new StringBuilder();
        append(builder, target.getComponent().getEditBox());
        append(builder, spriteFrom);
        append(builder, lifeTime);
        append(builder, alwaysRender);
        append(builder, amount, 1);
        Stream.of(xPos, yPos, zPos, xD, yD, zD, vx, vy, vz, vxD, vyD, vzD).forEach(titled -> append(builder, titled, "0.0"));
        append(builder, collision);
        Stream.of(collisionTime, horizontalCollision, verticalCollision, friction, friction2, gravity, gravity2, xDeflection, xDeflection2, zDeflection, zDeflection2, roll)
                .forEach(titled -> append(builder, titled, 0));
        append(builder, interact);
        Stream.of(horizontalInteract, verticalInteract).forEach(titled -> append(builder, titled, 0));
        append(builder, renderType);
        Stream.of(r, g, b).forEach(titled -> append(builder, titled));
        append(builder, bloomStrength, 1);
        Stream.of(alphaBegin, alphaEnd).forEach(titled -> append(builder, titled));
        append(builder, alpha);
        Stream.of(scaleBegin, scaleEnd).forEach(titled -> append(builder, titled));
        append(builder, scale);
        append(builder, whoCanSee.getComponent().getEditBox(), "@a");
        metaPanel.wrap(builder);
        return builder.toString();
    }

    private void append(StringBuilder string, Object o) {
        if (o != null && !o.toString().isEmpty()) {
            string.append(" ").append(o);
        } else {
            string.append(" ?");
        }
    }

    private void append(StringBuilder string, TEditBox editBox) {
        append(string, editBox.getValue());
    }

    private void append(StringBuilder stringBuilder, TTitledComponent<? extends TEditBox> titled) {
        append(stringBuilder, titled.getComponent());
    }

    private void append(StringBuilder string, TEditBox editBox, Object defaultValue) {
        String v = editBox.getValue();
        if (v.isEmpty() || v.equals(Language.getInstance().getOrDefault("gui.t88.invalid"))) {
            append(string, defaultValue);
        } else {
            append(string, v);
        }
    }

    private void append(StringBuilder stringBuilder, TTitledComponent<? extends TEditBox> titled, Object defaultValue) {
        append(stringBuilder, titled.getComponent(), defaultValue);
    }

    private void append(StringBuilder string, TTitledCycleButton<?> button) {
        if (button.getComponent().getSelected() == null) {
            append(string, (Object) null);
        } else {
            append(string, button.getComponent().getSelected().getContent());
        }
    }
}
