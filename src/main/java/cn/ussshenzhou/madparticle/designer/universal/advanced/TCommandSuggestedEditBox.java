package cn.ussshenzhou.madparticle.designer.universal.advanced;

import cn.ussshenzhou.madparticle.designer.universal.widegt.TPanel;
import cn.ussshenzhou.madparticle.designer.universal.widegt.TSelectList;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

public class TCommandSuggestedEditBox extends TPanel {
    private final TCommandConstrainedEditBox editBox;
    private final TSelectList<String> suggestionList = new TSelectList<>(10, 0) {

    };

    public TCommandSuggestedEditBox(Consumer<CommandDispatcher<CommandSourceStack>> consumer) {
        super();
        editBox = new TCommandConstrainedEditBox(consumer);
        //editBox.setVisible(false);
        editBox.setResponder(s -> {
            this.updateSuggestion(s);
            editBox.check(s);
        });

        this.add(editBox);
        this.add(suggestionList);
    }

    @Override
    public void layout() {
        editBox.setBounds(0, 0, width, height);
        super.layout();
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
        List<String> texts = new ArrayList<>();
        int l = 0;
        for (Suggestion suggestion : list) {
            texts.add(suggestion.getText());
            l = Math.max(l, suggestion.getText().length());
        }
        suggestionList.addElement(texts);
        if (Minecraft.getInstance().screen != null) {
            int listY;
            if (y <= Minecraft.getInstance().screen.height / 2) {
                listY = y + height + 2;
            } else {
                listY = y - 2;
            }
            suggestionList.setAbsBounds(
                    editBox.getCursorX(),
                    listY,
                    9 * l + 10,
                    Math.min(texts.size() * suggestionList.getItemHeight(), Minecraft.getInstance().screen.height - listY - 10)
            );
        }
    }

    public TCommandConstrainedEditBox getEditBox() {
        return editBox;
    }

    public TSelectList<String> getSuggestionList() {
        return suggestionList;
    }
}
