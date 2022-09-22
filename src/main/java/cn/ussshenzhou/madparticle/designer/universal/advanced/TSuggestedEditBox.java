package cn.ussshenzhou.madparticle.designer.universal.advanced;

import cn.ussshenzhou.madparticle.designer.universal.util.HorizontalAlignment;
import cn.ussshenzhou.madparticle.designer.universal.widegt.TPanel;
import cn.ussshenzhou.madparticle.designer.universal.widegt.TSelectList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.util.Mth;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

/**
 * @author USS_Shenzhou
 */
public class TSuggestedEditBox extends TPanel {
    private final TCommandConstrainedEditBox editBox;
    private final TSelectList<String> suggestionList = new TSelectList<>(12, 0) {
        @Override
        protected void renderBackground(PoseStack pPoseStack) {
            super.renderBackground(pPoseStack);
        }
    };

    public TSuggestedEditBox(Consumer<CommandDispatcher<CommandSourceStack>> consumer) {
        super();
        editBox = new TCommandConstrainedEditBox(consumer) {
            @Override
            public void setFocus(boolean pIsFocused) {
                super.setFocus(pIsFocused);
                suggestionList.setVisible(pIsFocused);
            }
        };
        editBox.setResponder(s -> {
            this.updateSuggestion(s);
            editBox.check(s);
        });
        suggestionList.setBackground(0xff000000);
        suggestionList.setHorizontalAlignment(HorizontalAlignment.LEFT);
        suggestionList.setSelectedForeGround(0xfffcfc00);
        this.add(editBox);
        this.add(suggestionList);
    }

    public TSuggestedEditBox(CommandDispatcher<CommandSourceStack> dispatcher) {
        this(d -> {
        });
        editBox.setDispatcher(dispatcher);
    }

    @Override
    public void layout() {
        editBox.setBounds(0, 0, width, height);
        super.layout();
    }

    @Override
    protected void renderChildren(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        if (editBox.isVisible()) {
            editBox.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
        }
    }

    @Override
    public void renderTop(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        if (suggestionList.isVisible()) {
            suggestionList.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
        }
        super.renderTop(pPoseStack, pMouseX, pMouseY, pPartialTick);
    }

    public void updateSuggestion(String value) {
        CommandSourceStack sourceStack = Minecraft.getInstance().player.createCommandSourceStack();
        ParseResults<CommandSourceStack> parseResults = editBox.dispatcher.parse(value, sourceStack);
        CompletableFuture<Suggestions> suggestions = editBox.dispatcher.getCompletionSuggestions(parseResults, editBox.getCursorPosition());
        suggestions.thenRun(() -> {
            if (suggestions.isDone()) {
                try {
                    List<Suggestion> list = suggestions.get().getList();
                    updateSuggestionList(list);
                } catch (InterruptedException | ExecutionException ignored) {
                }
            }
        });
    }

    private void updateSuggestionList(List<Suggestion> list) {
        suggestionList.clearElement();
        if (!list.isEmpty()) {
            suggestionList.setVisible(true);
            List<String> texts = new ArrayList<>();
            String l = "";
            for (Suggestion suggestion : list) {
                texts.add(suggestion.getText());
                l = suggestion.getText().length() > l.length() ? suggestion.getText() : l;
            }
            suggestionList.addElement(texts);
            if (Minecraft.getInstance().screen != null) {
                int listY;
                if (y <= Minecraft.getInstance().screen.height / 2) {
                    listY = y + height + 1;
                } else {
                    listY = y - 1;
                }
                int width = Minecraft.getInstance().font.width(l) + TSelectList.SCROLLBAR_WIDTH+2;
                suggestionList.setAbsBounds(
                        calculateSuggestionX(width),
                        listY,
                        width,
                        Math.min(texts.size() * suggestionList.getItemHeight() + 4, Minecraft.getInstance().screen.height - listY - 5)
                );
            }
        } else {
            suggestionList.setVisible(false);
        }
    }

    private int calculateSuggestionX(int l) {
        int i = editBox.getCurrentWordBeginX() - 1;
        int w = Minecraft.getInstance().screen.width - 5;
        if (i + l > w) {
            return w - l;
        } else {
            return i;
        }
    }

    public void applySuggestion() {
        TSelectList<String>.Entry e = suggestionList.getSelected();
        if (e != null) {
            String suggestion = suggestionList.getSelected().getContent();
            String s = editBox.getValue();
            int a = s.lastIndexOf(" ", editBox.getCursorPosition());
            int b = s.indexOf(" ", editBox.getCursorPosition());
            if (b == editBox.getCursorPosition()) {
                b = s.indexOf(" ", Mth.clamp(editBox.getCursorPosition() - 1, 0, Integer.MAX_VALUE));
            }
            if (b == -1) {
                b = s.length() - 1;
            }
            if (b == -1) {
                b = 0;
            }
            a = Mth.clamp(a, 0, s.length() - 1);
            if (s.isEmpty()) {
                editBox.setValue(suggestion + " ");
            } else {
                editBox.setValue(s.substring(0, a)
                        + " "
                        + suggestion
                        //+ " "
                        + s.substring(
                        Mth.clamp(b + 1, 0, s.length() - 1)));
            }
            editBox.moveCursorTo(a + suggestion.length() + 2);
        }
    }

    public TCommandConstrainedEditBox getEditBox() {
        return editBox;
    }

    public TSelectList<String> getSuggestionList() {
        return suggestionList;
    }

    @Override
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        if (editBox.isFocused()) {
            if (pKeyCode == GLFW.GLFW_KEY_TAB || pKeyCode == GLFW.GLFW_KEY_ENTER) {
                applySuggestion();
                return true;
            }
            return super.keyPressed(pKeyCode, pScanCode, pModifiers);
        }
        return false;
    }
}
