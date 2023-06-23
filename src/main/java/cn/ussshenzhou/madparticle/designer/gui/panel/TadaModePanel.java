package cn.ussshenzhou.madparticle.designer.gui.panel;

import cn.ussshenzhou.madparticle.command.CommandHelper;
import cn.ussshenzhou.madparticle.command.MadParticleCommand;
import cn.ussshenzhou.madparticle.command.inheritable.InheritableBoolean;
import cn.ussshenzhou.madparticle.designer.gui.DesignerScreen;
import cn.ussshenzhou.madparticle.designer.gui.widegt.CommandStringSelectList;
import cn.ussshenzhou.madparticle.network.MakeTadaPacket;
import cn.ussshenzhou.madparticle.particle.ChangeMode;
import cn.ussshenzhou.madparticle.particle.ParticleRenderTypes;
import cn.ussshenzhou.madparticle.particle.SpriteFrom;
import cn.ussshenzhou.t88.gui.advanced.TSuggestedEditBox;
import cn.ussshenzhou.t88.gui.event.TWidgetContentUpdatedEvent;
import cn.ussshenzhou.t88.gui.util.AccessorProxy;
import cn.ussshenzhou.t88.gui.util.LayoutHelper;
import cn.ussshenzhou.t88.gui.widegt.TButton;
import cn.ussshenzhou.t88.gui.widegt.TComponent;
import cn.ussshenzhou.t88.gui.widegt.TSelectList;
import cn.ussshenzhou.t88.gui.widegt.TWidget;
import cn.ussshenzhou.t88.network.NetworkHelper;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.brigadier.context.ParsedArgument;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * @author USS_Shenzhou
 */
public class TadaModePanel extends HelperModePanel {
    private final TButton make = new TButton(Component.translatable("gui.mp.de.mode.tada.make"));

    public TadaModePanel() {
        super();
        make.setOnPress(button -> {
            NetworkHelper.sendToServer(new MakeTadaPacket(command.getEditBox().getValue()));
        });
        this.add(make);
        initCommand();
        initList();
    }

    private void initList() {
        remove(commandStringSelectList);
        commandStringSelectList = new CommandStringSelectList() {
            @Override
            protected void initButton() {
                newCommand.setOnPress(pButton -> {
                    var list = getComponent();
                    var sub = new CommandStringSelectList.SubCommand(new TadaParametersScrollPanel());
                    add(sub.parametersScrollPanel);
                    addElement(sub, list1 -> {
                        list1.getParentInstanceOf(TadaModePanel.class).setParametersScrollPanel(list1.getSelected().getContent().parametersScrollPanel);
                    });
                    if (list.getSelected() == null) {
                        list.setSelected(list.children().get(list.children().size() - 1));
                    }
                    this.checkChild();
                });
                delete.setOnPress(pButton -> {
                    getComponent().removeElement(getComponent().getSelected());
                    delete.getParentInstanceOf(TadaModePanel.class).setParametersScrollPanel(null);
                    this.checkChild();
                });
            }
        };
        add(commandStringSelectList);
    }

    private void initCommand() {
        MinecraftForge.EVENT_BUS.unregister(command);
        remove(this.command);
        this.command = new TSuggestedEditBox(MadParticleCommand::new) {

            @SubscribeEvent
            public void onUpdateCalledTada(TWidgetContentUpdatedEvent event) {
                CompletableFuture.runAsync(() -> {
                    if (canHandleCall && event.getUpdated() != this.getEditBox() && event.getUpdated().getParentInstanceOf(TadaModePanel.class) == this.getParent()) {
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
        add(this.command);
        MinecraftForge.EVENT_BUS.register(command);
        command.getEditBox().setFocused(false);
        command.getEditBox().setMaxLength(32500);
    }

    @Override
    public void layout() {
        copy.setBounds(width - TButton.RECOMMEND_SIZE.x, (40 - TButton.RECOMMEND_SIZE.y) / 2);
        LayoutHelper.BLeftOfA(copy, DesignerScreen.GAP + 20, copy);
        LayoutHelper.BRightOfA(make, DesignerScreen.GAP, copy, copy.getWidth() + 20, copy.getHeight());
        LayoutHelper.BLeftOfA(command, DesignerScreen.GAP, copy, width - copy.getWidth() - make.getWidth() - 2 * DesignerScreen.GAP, TButton.RECOMMEND_SIZE.y);
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
        for (TWidget tWidget : children) {
            if (tWidget instanceof TComponent tComponent) {
                tComponent.layout();
            }
        }
    }

    private int i = 0;

    @Override
    protected void unwrap() {
        this.setParametersScrollPanel(null);
        canHandleCall = false;
        commandStringSelectList.getComponent().clearElement();
        String commandString = command.getEditBox().getValue();
        String[] commandStrings = commandString.split(" expireThen ");
        i = 0;
        for (String s : commandStrings) {
            if (s.startsWith("/")) {
                s = s.replaceFirst("/", "");
            }
            if (!s.startsWith("mp") && !s.startsWith("madparticle")) {
                s = "mp " + s;
            }
            ParseResults<CommandSourceStack> parseResults = MadParticleCommand.justParse(s);
            CommandContextBuilder<CommandSourceStack> ctb;
            ctb = CommandHelper.getContextBuilderHasArgument(parseResults.getContext(), "targetParticle");
            if (ctb == null) {
                return;
            }
            Map<String, ParsedArgument<CommandSourceStack, ?>> map = ctb.getArguments();
            TadaParametersScrollPanel panel = new TadaParametersScrollPanel();
            getArgAndFill(panel.target.getComponent().getEditBox(), "targetParticle", s, map);
            getArgAndFill(panel.lifeTime, "lifeTime", s, map);
            getArgAndFill(panel.amount, "amount", s, map);
            getArgAndFill(panel.whoCanSee.getComponent().getEditBox(), "whoCanSee", s, map);
            getVec3ArgAndFill(panel.xD, panel.yD, panel.zD, "spawnDiffuse", s, map);
            getVec3ArgAndFill(panel.vx, panel.vy, panel.vz, "spawnSpeed", s, map);

            List.of(panel.xPos, panel.yPos, panel.zPos).forEach(singleVec3EditBox -> singleVec3EditBox.getComponent().setValue(i == 0 ? "~" : "="));
            var a = map.get("spawnSpeed").getRange().get(s).split(" ");
            double vx = Double.parseDouble(a[0]);
            double vy = Double.parseDouble(a[1]);
            double vz = Double.parseDouble(a[2]);
            var speed = Math.sqrt(vx * vx + vy * vy + vz * vz);
            panel.speed.getComponent().setValue(String.format("%.3f", speed));
            AccessorProxy.EditBoxProxy.setDisplayPos(panel.speed.getComponent(), 0);

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
            commandStringSelectList.addElement(new CommandStringSelectList.SubCommand(panel), list1 -> {
                list1.getParentInstanceOf(HelperModePanel.class).setParametersScrollPanel(list1.getSelected().getContent().getParametersScrollPanel());
            });
            i++;
        }
        commandStringSelectList.checkChild();
        this.layout();
    }
}
