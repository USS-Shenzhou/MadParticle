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
    private final TSelectList<String> suggestionList = new TSelectList<>(12, 0);

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
        try {
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
        } catch (NullPointerException ignored) {
        }
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
                int width = Minecraft.getInstance().font.width(l) + TSelectList.SCROLLBAR_WIDTH + 2;
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
            if (s.isEmpty()) {
                editBox.setValue(suggestion);
            } else {
                int a = s.lastIndexOf(" ", editBox.getCursorPosition());
                int b = s.indexOf(" ", editBox.getCursorPosition());
                if (a == -1) {
                    editBox.setValue(b == -1 ? suggestion : suggestion + s.substring(b));
                } else if (b == -1) {
                    editBox.setValue(s.substring(0, a + 1) + suggestion);
                } else {
                    editBox.setValue(s.substring(0, a + 1) + suggestion + s.substring(b));
                }
                editBox.moveCursorTo(a + 1 + suggestion.length());
            }
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
