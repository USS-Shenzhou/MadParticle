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
import cn.ussshenzhou.t88.gui.combine.TTitledComponent;
import cn.ussshenzhou.t88.gui.combine.TTitledCycleButton;
import cn.ussshenzhou.t88.gui.combine.TTitledSimpleConstrainedEditBox;
import cn.ussshenzhou.t88.gui.combine.TTitledSuggestedEditBox;
import cn.ussshenzhou.t88.gui.container.TVerticalScrollContainer;
import cn.ussshenzhou.t88.gui.util.AccessorProxy;
import cn.ussshenzhou.t88.gui.util.ArgumentSuggestionsDispatcher;
import net.neoforged.neoforge.client.ClientCommandHandler;
import org.joml.Vector2i;
import cn.ussshenzhou.t88.gui.widegt.TButton;
import cn.ussshenzhou.t88.gui.widegt.TEditBox;
import cn.ussshenzhou.t88.gui.widegt.TSlider;
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

import java.text.DecimalFormat;
import java.util.Map;
import java.util.stream.Stream;

import static cn.ussshenzhou.t88.gui.util.LayoutHelper.*;

/**
 * @author USS_Shenzhou
 */
@SuppressWarnings("AlibabaCommentsMustBeJavadocFormat")
public class ParametersPanel extends TVerticalScrollContainer {
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
            rSlider = new TSlider(Component.translatable("gui.mp.de.helper.r").getString(), 0, 1, false, null, true),
            gSlider = new TSlider(Component.translatable("gui.mp.de.helper.g").getString(), 0, 1, false, null, true),
            bSlider = new TSlider(Component.translatable("gui.mp.de.helper.b").getString(), 0, 1, false, null, true);
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

    public ParametersPanel() {
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
        tryDefault.setOnPress(b -> tryFillDefault());
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
        this.addAll(xPos, yPos, zPos, xD, yD, zD);
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
            rSlider.setAbsValueWithoutRespond(f);
        });
        g.getComponent().addPassedResponder(s -> {
            float f = Float.parseFloat(s);
            gSlider.setAbsValueWithoutRespond(f);
        });
        b.getComponent().addPassedResponder(s -> {
            float f = Float.parseFloat(s);
            bSlider.setAbsValueWithoutRespond(f);
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
        rSlider.setAbsValue(1);
        gSlider.setAbsValue(1);
        bSlider.setAbsValue(1);
    }

    public void init6() {
        this.addAll(collision, horizontalCollision, verticalCollision, collisionTime, xDeflection, xDeflection2, zDeflection, zDeflection2);
        collision.addElement(InheritableBoolean.TRUE, t -> {
            Stream.of(horizontalCollision, verticalCollision, collisionTime).forEach(e -> e.getComponent().setEditable(true));
        });
        collision.addElement(InheritableBoolean.FALSE, t -> {
            Stream.of(horizontalCollision, verticalCollision, collisionTime).forEach(e -> e.getComponent().setEditable(false));
        });
    }

    public void init7() {
        this.addAll(roll, interact, horizontalInteract, verticalInteract, friction, friction2, gravity, gravity2);
        interact.addElement(InheritableBoolean.TRUE, t -> {
            Stream.of(horizontalInteract, verticalInteract).forEach(e -> e.getComponent().setEditable(true));
        });
        interact.addElement(InheritableBoolean.FALSE, t -> {
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
        int xGap = 4;
        Vector2i size3 = new Vector2i((getUsableWidth() - 4 * xGap) / 3, 20 + 12);
        Vector2i size2 = new Vector2i((getUsableWidth() - 3 * xGap) / 2, 20 + 12);
        Vector2i size1 = new Vector2i(getUsableWidth() - 2 * xGap, 20 + 12);
        int yGap = xGap;
        //lane 1
        target.setBounds(xGap, yGap, getUsableWidth() - xGap * 3 - size3.x, size3.y);
        BRightOfA(tryDefault, xGap, target, size3.x, 20);
        BBottomOfA(tryDefault, -8, tryDefault);
        //lane 2
        BBottomOfA(lifeTime, yGap, target, size2);
        BRightOfA(amount, xGap, lifeTime);
        //lane 3
        BBottomOfA(whoCanSee, yGap, lifeTime, size1);
        //lane 4
        BBottomOfA(spriteFrom, yGap, whoCanSee, size3);
        BRightOfA(alwaysRender, xGap, spriteFrom);
        BRightOfA(renderType, xGap, alwaysRender);
        //lane 5
        BBottomOfA(xPos, yGap, spriteFrom, size3);
        BRightOfA(yPos, xGap, xPos);
        BRightOfA(zPos, xGap, yPos);
        //lane 6
        BBottomOfA(xD, yGap, xPos);
        BRightOfA(yD, xGap, xD);
        BRightOfA(zD, xGap, yD);
        //lane 7
        BBottomOfA(vx, yGap, xD);
        BRightOfA(vy, xGap, vx);
        BRightOfA(vz, xGap, vy);
        //lane 8
        BBottomOfA(vxD, xGap, vx);
        BRightOfA(vyD, xGap, vxD);
        BRightOfA(vzD, xGap, vyD);
        //lane 9
        BBottomOfA(r, yGap, vxD, size3);
        BRightOfA(rSlider, xGap, r, getUsableWidth() - size3.x - 3 * xGap, rSlider.getPreferredSize().y);
        BBottomOfA(rSlider, -8, rSlider);
        //lane 10
        BBottomOfA(g, yGap, r);
        BBottomOfA(gSlider, yGap + 12, rSlider);
        //lane 11
        BBottomOfA(b, yGap, g);
        BBottomOfA(bSlider, yGap + 12, gSlider);
        //lane 12
        BBottomOfA(collision, yGap, b, size2);
        BRightOfA(collisionTime, xGap, collision);
        //lane 13
        BBottomOfA(horizontalCollision, yGap, collision);
        BRightOfA(verticalCollision, xGap, horizontalCollision);
        //lane 14
        BBottomOfA(friction, yGap, horizontalCollision);
        BRightOfA(friction2, xGap, friction);
        //lane 15
        BBottomOfA(gravity, yGap, friction);
        BRightOfA(gravity2, xGap, gravity);
        //lane 16
        BBottomOfA(xDeflection, yGap, gravity);
        BRightOfA(xDeflection2, xGap, xDeflection);
        //lane 17
        BBottomOfA(zDeflection, yGap, xDeflection);
        BRightOfA(zDeflection2, xGap, zDeflection);
        //lane 18
        BBottomOfA(interact, yGap, zDeflection, size3);
        BRightOfA(horizontalInteract, xGap, interact);
        BRightOfA(verticalInteract, xGap, horizontalInteract);
        //lane 19
        BBottomOfA(roll, yGap, interact, size2);
        BRightOfA(bloomStrength, xGap, roll);
        //lane 20
        BBottomOfA(scale, yGap, roll, size3);
        BRightOfA(scaleBegin, xGap, scale);
        BRightOfA(scaleEnd, xGap, scaleBegin);
        //lane 21
        BBottomOfA(alpha, yGap, scale);
        BRightOfA(alphaBegin, xGap, alpha);
        BRightOfA(alphaEnd, xGap, alphaBegin);
        //meta
        metaPanel.passGap(xGap, yGap);
        BBottomOfA(metaPanel, 2 * yGap, alpha, getUsableWidth() - xGap, metaPanel.getPreferredSize().y);
        super.layout();
    }

    //TODO set bloomStrength

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
            collision.addElement(InheritableBoolean.INHERIT, t -> {
                Stream.of(horizontalCollision, verticalCollision, collisionTime).forEach(editBox -> {
                    editBox.getComponent().setEditable(true);
                    ifClearThenSet(editBox, "=");
                });
            });
            interact.addElement(InheritableBoolean.INHERIT, t -> {
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
                (editBox.getValue().isEmpty() || ((EditBoxAccessor) editBox).getTextColor() == 0xff37e2ff)
                        && ((EditBoxAccessor) editBox).isIsEditable()
        ) {
            if (value instanceof Double || value instanceof Float) {
                DecimalFormat df = new DecimalFormat("#.###");
                editBox.setValue(df.format(value));
            } else {
                editBox.setValue(value.toString());
            }
            editBox.setTextColor(0xff37e2ff);
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
