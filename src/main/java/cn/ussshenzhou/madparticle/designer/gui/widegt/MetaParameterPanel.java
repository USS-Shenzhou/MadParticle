package cn.ussshenzhou.madparticle.designer.gui.widegt;

import cn.ussshenzhou.madparticle.designer.gui.panel.ParametersScrollPanel;
import cn.ussshenzhou.madparticle.mixin.EditBoxAccessor;
import cn.ussshenzhou.madparticle.util.MetaKeys;
import cn.ussshenzhou.t88.gui.advanced.TSimpleConstrainedEditBox;
import cn.ussshenzhou.t88.gui.advanced.TSuggestedEditBox;
import cn.ussshenzhou.t88.gui.event.TWidgetContentUpdatedEvent;
import cn.ussshenzhou.t88.gui.util.HorizontalAlignment;
import cn.ussshenzhou.t88.gui.util.LayoutHelper;
import cn.ussshenzhou.t88.gui.widegt.*;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraftforge.common.MinecraftForge;
import org.joml.Vector2i;

import java.util.Arrays;
import java.util.LinkedList;

/**
 * @author USS_Shenzhou
 */
public class MetaParameterPanel extends TPanel {
    private final TPanel divideLine = new TPanel();
    private final TLabel title = new TLabel(Component.translatable("gui.mp.de.helper.meta.title"));
    private final LinkedList<MetaPairPanel> pairs = new LinkedList<>();
    private final TButton addNewPair = new TButton(Component.literal("+"), button -> createAPair());

    private int xGap;
    private int yGap;

    public MetaParameterPanel() {
        super();
        this.add(divideLine);
        divideLine.setBackground(0xffaaaaaa);
        this.add(title);
        title.setHorizontalAlignment(HorizontalAlignment.CENTER);
        this.add(addNewPair);
    }

    private MetaParameterPanel out() {
        return this;
    }

    public void passGap(int xGap, int yGap) {
        this.xGap = xGap;
        this.yGap = yGap;
    }

    @Override
    public void layout() {
        divideLine.setBounds(0, 0, width, 1);
        LayoutHelper.BBottomOfA(title, yGap, divideLine, width, title.getPreferredSize().y);
        int i = 0;
        for (MetaPairPanel pair : pairs) {
            if (i == 0) {
                LayoutHelper.BBottomOfA(pair, yGap, title, width, 20);
            } else {
                LayoutHelper.BBottomOfA(pair, yGap, pairs.get(i - 1));
            }
            i++;
        }
        addNewPair.setBounds(0, height - 20, width, 20);
        super.layout();
    }

    @Override
    public Vector2i getPreferredSize() {
        return new Vector2i(0, 1
                + yGap
                + title.getPreferredSize().y
                + pairs.size() * (yGap + 20)
                + yGap
                + 20
        );
    }

    public void wrap(StringBuilder builder) {
        builder.append(" {");
        int i = 0;
        for (MetaPairPanel pair : pairs) {
            if (i != 0) {
                builder.append(",");
            }
            builder.append("\"");
            builder.append(pair.key.getEditBox().getValue());
            builder.append("\":");
            boolean naked = pair.value.getArgument() instanceof IntegerArgumentType;
            if (!naked) {
                builder.append("\"");
            }
            builder.append(pair.value.getValue());
            if (!naked) {
                builder.append("\"");
            }
            i++;
        }
        builder.append("}");
    }

    public void unwrap(CommandContext<CommandSourceStack> ct) {
        CompoundTag metaTag;
        try {
            metaTag = ct.getArgument("meta", CompoundTag.class);
        } catch (IllegalArgumentException ignored) {
            return;
        }
        metaTag.tags.forEach((s, tag) -> {
            var pair = createAPair();
            pair.setKV(s, tag.getAsString());
        });
    }

    public MetaPairPanel createAPair() {
        MetaPairPanel pair = new MetaPairPanel();
        pairs.add(pair);
        out().add(pair);
        TComponent p = getParentInstanceOf(ParametersScrollPanel.class);
        p.layout();
        return pair;
    }

    public class MetaPairPanel extends TPanel {
        public final TSuggestedEditBox key = new TSuggestedEditBox(dispatcher ->
                Arrays.stream(MetaKeys.values()).forEach(key -> dispatcher.register(Commands.literal(key.get())))
        );
        public final TSimpleConstrainedEditBox value = new TSimpleConstrainedEditBox(IntegerArgumentType.integer(), true);
        public final TButton remove = new TButton(Component.literal("-"), button -> {
            pairs.remove(this);
            out().remove(this);
            out().getParentInstanceOf(ParametersScrollPanel.class).layout();
            MinecraftForge.EVENT_BUS.post(new TWidgetContentUpdatedEvent(out()));
        });

        public MetaPairPanel() {
            this.add(key);
            this.add(value);
            key.getEditBox().addPassedResponder(keyText -> {
                MetaKeys metaKey = MetaKeys.fromString(keyText);
                if (metaKey != null) {
                    value.setArgument(metaKey.inputArgument);
                } else {
                    value.setArgument(StringArgumentType.string());
                }
                value.respond(value.getValue());
            });
            this.add(remove);
        }

        @Override
        public void layout() {
            key.setBounds(0, 0, width / 7, height);
            LayoutHelper.BRightOfA(value, xGap, key, width - 2 * width / 7 - 2 * xGap, height);
            LayoutHelper.BRightOfA(remove, xGap, value, width / 7, height);
            super.layout();
        }

        @SuppressWarnings("AlibabaLowerCamelCaseVariableNaming")
        public void setKV(String keyText, String valueText) {
            key.getEditBox().setValue(keyText);
            ((EditBoxAccessor) key.getEditBox()).setDisplayPos(0);
            value.setValue(valueText);
            ((EditBoxAccessor) value).setDisplayPos(0);
        }
    }
}
