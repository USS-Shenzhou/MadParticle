package cn.ussshenzhou.madparticle.designer.gui.panel;

import cn.ussshenzhou.madparticle.command.CommandHelper;
import cn.ussshenzhou.madparticle.command.MadParticleCommand;
import cn.ussshenzhou.madparticle.command.inheritable.InheritableBoolean;
import cn.ussshenzhou.madparticle.designer.gui.DesignerScreen;
import cn.ussshenzhou.madparticle.designer.gui.widegt.CommandStringSelectList;
import cn.ussshenzhou.madparticle.particle.ChangeMode;
import cn.ussshenzhou.madparticle.particle.ParticleRenderTypes;
import cn.ussshenzhou.madparticle.particle.SpriteFrom;
import cn.ussshenzhou.t88.gui.advanced.TSuggestedEditBox;
import cn.ussshenzhou.t88.gui.combine.TTitledComponent;
import cn.ussshenzhou.t88.gui.event.TWidgetContentUpdatedEvent;
import cn.ussshenzhou.t88.gui.util.AccessorProxy;
import cn.ussshenzhou.t88.gui.util.LayoutHelper;
import cn.ussshenzhou.t88.gui.widegt.*;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.brigadier.context.ParsedArgument;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * @author USS_Shenzhou
 */
public class HelperModePanel extends TPanel {
    protected final TButton copy = new TButton(Component.translatable("gui.mp.de.helper.copy"));
    protected final TButton unwrap = new TButton(Component.translatable("gui.mp.de.helper.unwrap"));
    protected TSuggestedEditBox command = new TSuggestedEditBox(MadParticleCommand::new) {

        @SubscribeEvent
        public void onUpdateCalled(TWidgetContentUpdatedEvent event) {
            CompletableFuture.runAsync(() -> {
                if (canHandleCall && event.getUpdated() != this.getEditBox() && event.getUpdated().getParentInstanceOf(HelperModePanel.class) == this.getParent()) {
                    String wholeCommand = commandStringSelectList.warp();
                    synchronized (this.getEditBox()) {
                        this.getEditBox().setValue(wholeCommand);
                    }
                }
            });
        }

        @Override
        public void onFinalClose() {
            MinecraftForge.EVENT_BUS.unregister(this);
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
    protected CommandStringSelectList commandStringSelectList = new CommandStringSelectList();

    protected ParametersScrollPanel parametersScrollPanel = null;
    protected boolean canHandleCall = true;

    public HelperModePanel() {
        super();
        command.getEditBox().setFocused(false);
        command.getEditBox().setMaxLength(32500);
        this.add(copy);
        this.add(command);
        this.add(commandStringSelectList);
        this.add(unwrap);
        unwrap.setVisibleT(false);
        MinecraftForge.EVENT_BUS.register(command);
        copy.setOnPress(pButton -> {
            Minecraft.getInstance().keyboardHandler.setClipboard(command.getEditBox().getValue());
            //TODO: copied!
        });
        unwrap.setOnPress(pButton -> unwrap());
    }

    protected void switchCopyAndUnwrap() {
        copy.setVisibleT(!copy.isVisibleT());
        unwrap.setVisibleT(!unwrap.isVisibleT());
    }

    public void setParametersScrollPanel(ParametersScrollPanel parametersScrollPanel) {
        if (this.parametersScrollPanel != null) {
            this.parametersScrollPanel.setVisibleT(false);
            this.remove(this.parametersScrollPanel);
        }
        this.parametersScrollPanel = parametersScrollPanel;
        if (parametersScrollPanel != null) {
            this.add(parametersScrollPanel);
            parametersScrollPanel.setVisibleT(true);
        }
        layout();
    }

    @Override
    public void layout() {
        copy.setBounds(width - TButton.RECOMMEND_SIZE.x, (40 - TButton.RECOMMEND_SIZE.y) / 2);
        LayoutHelper.BLeftOfA(command, DesignerScreen.GAP, copy, width - copy.getWidth() - DesignerScreen.GAP, TButton.RECOMMEND_SIZE.y);
        LayoutHelper.BBottomOfA(commandStringSelectList, DesignerScreen.GAP, command,
                TButton.RECOMMEND_SIZE.x + commandStringSelectList.getComponent().getScrollbarGap() + TSelectList.SCROLLBAR_WIDTH,
                height - command.getYT() - command.getHeight() - DesignerScreen.GAP * 2 - TButton.RECOMMEND_SIZE.y - 1
        );
        if (parametersScrollPanel != null) {
            LayoutHelper.BRightOfA(parametersScrollPanel,
                    DesignerScreen.GAP + 2, commandStringSelectList,
                    width - commandStringSelectList.getWidth() - DesignerScreen.GAP - 2,
                    commandStringSelectList.getHeight() + DesignerScreen.GAP * 2 + 1 + TButton.RECOMMEND_SIZE.y);
        }
        LayoutHelper.BSameAsA(unwrap, copy);
        super.layout();
    }

    /**
     * @see MadParticleCommand#sendToPlayer(CommandContext, Collection, CommandDispatcher)
     */
    public void unwrap() {
        this.setParametersScrollPanel(null);
        canHandleCall = false;
        commandStringSelectList.getComponent().clearElement();
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
            ParametersScrollPanel panel = new ParametersScrollPanel();
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
            panel.metaPanel.unwrap(ct);
            commandStringSelectList.addElement(new CommandStringSelectList.SubCommand(panel), list1 -> {
                list1.getParentInstanceOf(HelperModePanel.class).setParametersScrollPanel(list1.getSelected().getContent().getParametersScrollPanel());
            });
        }
        commandStringSelectList.checkChild();
        this.layout();
        var commandTSelectList = this.commandStringSelectList.getComponent();
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
}
