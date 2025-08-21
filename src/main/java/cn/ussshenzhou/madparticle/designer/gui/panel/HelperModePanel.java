package cn.ussshenzhou.madparticle.designer.gui.panel;

import cn.ussshenzhou.madparticle.MadParticle;
import cn.ussshenzhou.madparticle.command.CommandHelper;
import cn.ussshenzhou.madparticle.command.MadParticleCommand;
import cn.ussshenzhou.madparticle.command.inheritable.InheritableBoolean;
import cn.ussshenzhou.madparticle.designer.gui.widegt.CommandChainSelectList;
import cn.ussshenzhou.madparticle.particle.enums.ChangeMode;
import cn.ussshenzhou.madparticle.particle.enums.ParticleRenderTypes;
import cn.ussshenzhou.madparticle.particle.enums.SpriteFrom;
import cn.ussshenzhou.t88.gui.advanced.TSuggestedEditBox;
import cn.ussshenzhou.t88.gui.combine.TTitledComponent;
import cn.ussshenzhou.t88.gui.event.TWidgetContentUpdatedEvent;
import cn.ussshenzhou.t88.gui.notification.TSimpleNotification;
import cn.ussshenzhou.t88.gui.util.AccessorProxy;
import cn.ussshenzhou.t88.gui.util.Border;
import cn.ussshenzhou.t88.gui.util.LayoutHelper;
import cn.ussshenzhou.t88.gui.util.MouseHelper;
import cn.ussshenzhou.t88.gui.widegt.*;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.brigadier.context.ParsedArgument;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static cn.ussshenzhou.madparticle.designer.gui.DesignerScreen.GAP;

/**
 * @author USS_Shenzhou
 */
public class HelperModePanel extends TPanel {
    public static final int FOREGROUND = 0x80ffffff;
    public static final int BACKGROUND = 0x80000000;

    protected final TButton copy = new TButton(Component.translatable("gui.mp.de.helper.copy"));
    protected final TButton unwrap = new TButton(Component.translatable("gui.mp.de.helper.unwrap"));
    protected volatile boolean canHandleCall = true;
    protected final TSuggestedEditBox command = new TSuggestedEditBox(MadParticleCommand::new) {

        @SubscribeEvent
        public void onUpdateCalled(TWidgetContentUpdatedEvent event) {
            Thread.startVirtualThread(() -> {
                if (canHandleCall && event.getUpdated() != this.getEditBox() && event.getUpdated().getParentInstanceOf(HelperModePanel.class) == this.getParent()) {
                    String wholeCommand = commandsChain.warp();
                    synchronized (this.getEditBox()) {
                        this.getEditBox().setValue(wholeCommand);
                    }
                }
            });
        }

        @Override
        public void onFinalClose() {
            NeoForge.EVENT_BUS.unregister(this);
        }

        @Override
        public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
            if (Screen.isPaste(pKeyCode)) {
                switchCount = 0;
                callPauseCount = 0;
                switchCopyAndUnwrap();
            }
            return super.keyPressed(pKeyCode, pScanCode, pModifiers);
        }

        int callPauseCount = 0;
        int switchCount = -1;

        @Override
        public void tickT() {
            if (switchCount >= 0) {
                if (switchCount >= 60) {
                    switchCount = -2;
                    switchCopyAndUnwrap();
                }
                switchCount++;
            }
            if (!canHandleCall) {
                if (callPauseCount >= 5) {
                    callPauseCount = -1;
                    canHandleCall = true;
                } else {
                    callPauseCount++;
                }
            } else {
                callPauseCount = 0;
            }
            super.tickT();
        }
    };
    protected final CommandChainSelectList commandsChain = new CommandChainSelectList();
    @Nullable
    protected ParametersPanel parametersPanel = null;
    protected final SceneControlPanel sceneControlPanel = new SceneControlPanel();
    protected final ParticleBrowsePanel particleBrowsePanel = new ParticleBrowsePanel();
    protected final TLabel tip = new TLabel();

    public HelperModePanel() {
        super();
        command.getEditBox().setFocused(false);
        command.getEditBox().setMaxLength(32500);
        NeoForge.EVENT_BUS.register(command);
        this.add(command);

        copy.setOnPress(t -> {
            Minecraft.getInstance().keyboardHandler.setClipboard(command.getEditBox().getValue());
            TSimpleNotification.fire(Component.translatable("gui.mp.de.helper.copied"), 4, TSimpleNotification.Severity.INFO);
        });
        this.add(copy);
        unwrap.setOnPress(t -> unwrap());
        unwrap.setVisibleT(false);
        this.add(unwrap);

        this.add(commandsChain);

        sceneControlPanel.setBorder(new Border(FOREGROUND, -1));
        sceneControlPanel.setBackground(BACKGROUND);
        this.add(sceneControlPanel);

        particleBrowsePanel.setBorder(new Border(FOREGROUND, -1));
        particleBrowsePanel.setBackground(BACKGROUND);
        this.add(particleBrowsePanel);

        tip.setBorder(new Border(FOREGROUND, -1));
        tip.setBackground(BACKGROUND);
        this.add(tip);
    }


    @Override
    public void layout() {
        int particleBrowsePanelHeight = (int) (height * 0.25);
        int parametersPanelWidth = (int) (width * 0.25);
        int sceneControlPanelWidth = 60;
        int sceneViewWidth = width - sceneControlPanelWidth - parametersPanelWidth;
        int sceneViewHeight = height - particleBrowsePanelHeight;
        sceneControlPanel.setBounds(0, 0, sceneControlPanelWidth, height - particleBrowsePanelHeight);
        tip.setBounds(0, height - particleBrowsePanelHeight, width - parametersPanelWidth, 12);
        LayoutHelper.BBottomOfA(particleBrowsePanel, 0, tip, width - parametersPanelWidth, particleBrowsePanelHeight - 12);
        if (parametersPanel != null) {
            parametersPanel.setAbsBounds(this.x + width - parametersPanelWidth, this.y, parametersPanelWidth, height);
        }
        command.setBounds(sceneControlPanelWidth + GAP, GAP, sceneViewWidth - 4 - TButton.RECOMMEND_SIZE.x - 4 - GAP, TButton.RECOMMEND_SIZE.y);
        LayoutHelper.BRightOfA(copy, GAP, command, TButton.RECOMMEND_SIZE);
        LayoutHelper.BSameAsA(unwrap, copy);
        LayoutHelper.BBottomOfA(commandsChain, GAP, command,
                TButton.RECOMMEND_SIZE.x + commandsChain.getComponent().getScrollbarGap() + TSelectList.SCROLLBAR_WIDTH,
                sceneViewHeight - GAP - TButton.RECOMMEND_SIZE.y - GAP - (GAP + TButton.RECOMMEND_SIZE.y) * 2 - GAP
        );
        super.layout();
    }

    protected void switchCopyAndUnwrap() {
        copy.setVisibleT(!copy.isVisibleT());
        unwrap.setVisibleT(!unwrap.isVisibleT());
    }

    public void setParametersScrollPanel(ParametersPanel parametersPanel) {
        if (this.parametersPanel != null) {
            this.parametersPanel.setVisibleT(false);
            this.remove(this.parametersPanel);
        }
        this.parametersPanel = parametersPanel;
        if (parametersPanel != null) {
            parametersPanel.setBorder(new Border(FOREGROUND, -1));
            parametersPanel.setBackground(BACKGROUND);
            parametersPanel.setVisibleT(true);
            this.add(parametersPanel);
        }
        layout();
    }

    @Override
    public void render(GuiGraphics graphics, int pMouseX, int pMouseY, float pPartialTick) {
        super.render(graphics, pMouseX, pMouseY, pPartialTick);
        if (parametersPanel == null) {
            int parametersPanelWidth = (int) (width * 0.25);
            graphics.fill(this.x + width - parametersPanelWidth, this.y, this.x + width, this.y + height, BACKGROUND);
            Border.renderBorder(graphics, FOREGROUND, -1, this.x + width - parametersPanelWidth, this.y, width, height);
        }
    }

    /**
     * @see MadParticleCommand#sendToPlayer(CommandContext, Collection, CommandDispatcher)
     */
    public void unwrap() {
        this.setParametersScrollPanel(null);
        canHandleCall = false;
        commandsChain.getComponent().clearElement();
        String commandString = command.getEditBox().getValue();
        String[] commandStrings = commandString.split(" expireThen ");
        for (String s : commandStrings) {
            if (s.startsWith("/")) {
                s = s.replaceFirst("/", "");
            }
            s = s.replace("madparticle ", "mp ");
            if (!s.startsWith("mp")) {
                s = "mp " + s;
            }
            ParseResults<CommandSourceStack> parseResults = MadParticleCommand.justParse(s);
            CommandContextBuilder<CommandSourceStack> ctb;
            ctb = CommandHelper.getContextBuilderHasArgument(parseResults.getContext(), "targetParticle");
            if (ctb == null) {
                return;
            }
            Map<String, ParsedArgument<CommandSourceStack, ?>> map = ctb.getArguments();
            ParametersPanel panel = new ParametersPanel();
            getArgAndFill(panel.target.getComponent().getEditBox(), "targetParticle", s, map);
            getArgAndFill(panel.lifeTime, "lifeTime", s, map);
            getArgAndFill(panel.amount, "amount", s, map);
            getArgAndFill(panel.whoCanSee.getComponent().getEditBox(), "whoCanSee", s, map);
            getVec3ArgAndFill(panel.xPos, panel.yPos, panel.zPos, "spawnPos", s, map);
            getVec3ArgAndFill(panel.xD, panel.yD, panel.zD, "spawnDiffuse", s, map);
            getVec3ArgAndFill(panel.vx, panel.vy, panel.vz, "spawnSpeed", s, map);
            getVec3ArgAndFill(panel.vxD, panel.vyD, panel.vzD, "speedDiffuse", s, map);
            getArgAndFill(panel.r, "r", s, map);
            getArgAndFill(panel.g, "g", s, map);
            getArgAndFill(panel.b, "b", s, map);
            getArgAndFill(panel.bloomStrength, "bloomFactor", s, map);
            getArgAndFill(panel.horizontalInteract, "horizontalInteractFactor", s, map);
            getArgAndFill(panel.verticalInteract, "verticalInteractFactor", s, map);
            getArgAndFill(panel.horizontalCollision, "horizontalRelativeCollisionDiffuse", s, map);
            getArgAndFill(panel.verticalCollision, "verticalRelativeCollisionBounce", s, map);
            getArgAndFill(panel.collisionTime, "bounceTime", s, map);
            getArgAndFill(panel.friction, "friction", s, map);
            getArgAndFill(panel.friction2, "afterCollisionFriction", s, map);
            getArgAndFill(panel.gravity, "gravity", s, map);
            getArgAndFill(panel.gravity2, "afterCollisionGravity", s, map);
            getArgAndFill(panel.xDeflection, "xDeflection", s, map);
            getArgAndFill(panel.xDeflection2, "xDeflectionAfterCollision", s, map);
            getArgAndFill(panel.zDeflection, "zDeflection", s, map);
            getArgAndFill(panel.zDeflection2, "zDeflectionAfterCollision", s, map);
            getArgAndFill(panel.roll, "rollSpeed", s, map);
            getArgAndFill(panel.alphaBegin, "beginAlpha", s, map);
            getArgAndFill(panel.alphaEnd, "endAlpha", s, map);
            getArgAndFill(panel.scaleBegin, "beginScale", s, map);
            getArgAndFill(panel.scaleEnd, "endScale", s, map);
            CommandContext<CommandSourceStack> ct = parseResults.getContext().build(s);
            getArgAndSelect(panel.spriteFrom, "spriteFrom", SpriteFrom.class, ct);
            getArgAndSelect(panel.alwaysRender, "alwaysRender", InheritableBoolean.class, ct);
            getArgAndSelect(panel.renderType, "renderType", ParticleRenderTypes.class, ct);
            getArgAndSelect(panel.interact, "interactWithEntity", InheritableBoolean.class, ct);
            getArgAndSelect(panel.collision, "collision", InheritableBoolean.class, ct);
            getArgAndSelect(panel.alpha, "alphaMode", ChangeMode.class, ct);
            getArgAndSelect(panel.scale, "scaleMode", ChangeMode.class, ct);
            commandsChain.addElement(commandsChain.addSubCommand(panel), list1 -> {
                list1.getParentInstanceOf(HelperModePanel.class).setParametersScrollPanel(list1.getSelected().getContent().getParametersScrollPanel());
            });
            panel.metaPanel.unwrap(ct);
        }
        commandsChain.checkChild();
        this.layout();
        var commandTSelectList = this.commandsChain.getComponent();
        var commandList = commandTSelectList.getElements();
        if (!commandList.isEmpty()) {
            //TODO taskHelper delay 2
            //commandTSelectList.setSelected(commandList.size() - 1);
        }
    }

    protected <E> void getArgAndSelect(TTitledComponent<? extends TCycleButton<E>> titled, String name, Class<E> clazz, CommandContext<CommandSourceStack> ct) {
        getArgAndSelect(titled.getComponent(), name, clazz, ct);
    }

    protected <E> void getArgAndSelect(TCycleButton<E> cycleButton, String name, Class<E> clazz, CommandContext<CommandSourceStack> ct) {
        try {
            cycleButton.select(ct.getArgument(name, clazz));
        } catch (IllegalArgumentException ignored) {
        }
    }

    protected void getArgAndFill(TEditBox editBox, String name, String command, Map<String, ParsedArgument<CommandSourceStack, ?>> map) {
        ParsedArgument<?, ?> parsedArgument = map.get(name);
        if (parsedArgument != null) {
            editBox.setValue(parsedArgument.getRange().get(command));
            AccessorProxy.EditBoxProxy.setDisplayPos(editBox, 0);
        }
    }

    protected void getArgAndFill(TTitledComponent<? extends TEditBox> titled, String name, String command, Map<String, ParsedArgument<CommandSourceStack, ?>> map) {
        getArgAndFill(titled.getComponent(), name, command, map);
    }

    protected void getVec3ArgAndFill(TEditBox x, TEditBox y, TEditBox z, String name, String command, Map<String, ParsedArgument<CommandSourceStack, ?>> map) {
        var parsed = map.get(name);
        if (parsed != null) {
            String s = map.get(name).getRange().get(command);
            String[] s3 = s.split(" ");
            try {
                x.setValue(s3[0]);
                AccessorProxy.EditBoxProxy.setDisplayPos(x, 0);
                y.setValue(s3[1]);
                AccessorProxy.EditBoxProxy.setDisplayPos(y, 0);
                z.setValue(s3[2]);
                AccessorProxy.EditBoxProxy.setDisplayPos(z, 0);
            } catch (IndexOutOfBoundsException ignored) {
            }
        }
    }

    protected void getVec3ArgAndFill(TTitledComponent<? extends TEditBox> x, TTitledComponent<? extends TEditBox> y, TTitledComponent<? extends TEditBox> z, String name, String command, Map<String, ParsedArgument<CommandSourceStack, ?>> map) {
        getVec3ArgAndFill(x.getComponent(), y.getComponent(), z.getComponent(), name, command, map);
    }

    public TSuggestedEditBox getCommandEditBox() {
        return command;
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        var mc = Minecraft.getInstance();
        if (!super.mouseClicked(pMouseX, pMouseY, pButton) && isInWild(pMouseX, pMouseY)) {
            if (pButton == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                InputConstants.grabOrReleaseMouse(mc.getWindow().getWindow(), 212995, pMouseX, pMouseY);
                mc.mouseHandler.setIgnoreFirstMove();
                mc.mouseHandler.mouseGrabbed = true;
                return true;
            }
        }
        if (pButton == GLFW.GLFW_MOUSE_BUTTON_RIGHT && mc.mouseHandler.isMouseGrabbed()) {
            mc.mouseHandler.releaseMouse();
            return true;
        }
        return false;
    }

    private boolean isInWild(double mouseX, double mouseY) {
        return isInRange(mouseX, mouseY) &&
                children.stream().mapToInt(w -> w.isInRange(mouseX, mouseY) ? 1 : 0).sum() == 0 &&
                // in case parametersPanel is null
                mouseX < (int) (width * 0.75);
    }

    @Override
    public boolean mouseScrolled(double pMouseX, double pMouseY, double deltaX, double deltaY) {
        var mc = Minecraft.getInstance();
        if (mc.mouseHandler.isMouseGrabbed() || (!super.mouseScrolled(pMouseX, pMouseY, deltaX, deltaY) && isInWild(pMouseX, pMouseY))) {
            if (Minecraft.getInstance().getCameraEntity() instanceof LivingEntity livingEntity) {
                var attr = livingEntity.getAttribute(Attributes.CAMERA_DISTANCE);
                var loc = ResourceLocation.fromNamespaceAndPath(MadParticle.MOD_ID, "particle_preview");
                if (attr != null) {
                    var modifier = attr.getModifier(loc);
                    double base = 0;
                    if (modifier != null) {
                        base = modifier.amount();
                    }
                    attr.addOrReplacePermanentModifier(new AttributeModifier(loc, Mth.clamp(base - 0.3 * deltaY, 0, 16), AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
                }
            }
        }
        return false;
    }

    @Override
    public void tickT() {
        var mc = Minecraft.getInstance();
        var x = MouseHelper.getMouseX();
        var y = MouseHelper.getMouseY();
        if (mc.mouseHandler.isMouseGrabbed() || isInWild(x, y)) {
            tip.setText(Component.translatable("gui.mp.de.helper.tip.mid"));
        } else if (sceneControlPanel.isInRange(x, y)) {
            tip.setText(Component.translatable("gui.mp.de.helper.tip.left"));
        } else if (particleBrowsePanel.isInRange(x, y)) {
            tip.setText(Component.translatable("gui.mp.de.helper.tip.bottom"));
        } else if (x >= (int) (width * 0.75) || (parametersPanel != null && parametersPanel.isInRange(x, y))) {
            tip.setText(Component.translatable("gui.mp.de.helper.tip.right"));
        }
        super.tickT();
    }
}
