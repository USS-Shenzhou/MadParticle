package cn.ussshenzhou.madparticle.designer.universal.widegt;

import net.minecraft.client.Minecraft;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.Mth;

import java.util.Collection;
import java.util.LinkedList;
import java.util.function.Consumer;

public class TCycleButton<E> extends TButton {
    LinkedList<Entry> cycleValues = new LinkedList<>();
    int cycleIndex = 0;

    public TCycleButton() {
        super(new TextComponent(""));
        this.setOnPress(pButton -> cycleOnce(1));
    }

    public TCycleButton(Collection<Entry> entries) {
        this();
        entries.forEach(this::addElement);
    }

    public void addElement(E e) {
        addElement(new Entry(e));
    }

    public void addElement(Entry e) {
        cycleValues.add(e);
        if (getMessage().getString().equals("")) {
            this.setMessage(e.getNarration());
        }
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        if (isInRange(pMouseX, pMouseY)) {
            if (pButton == 1) {
                this.playDownSound(Minecraft.getInstance().getSoundManager());
                cycleOnce(-1);
                return true;
            }
        }
        return super.mouseClicked(pMouseX, pMouseY, pButton);
    }

    private void cycleOnce(int i) {
        if (cycleValues.size() != 0) {
            this.cycleIndex = cycleIndex + i;
            if (cycleIndex < 0) {
                cycleIndex = cycleValues.size() - 1;
            } else if (cycleIndex > cycleValues.size() - 1) {
                cycleIndex = 0;
            }
            this.setMessage(cycleValues.get(cycleIndex).getNarration());
        } else {
            this.cycleIndex = 0;
            this.setMessage(new TextComponent(""));
        }
    }

    public class Entry {
        E content;
        Consumer<E> onSwitched;

        public Entry(E content) {
            this.content = content;
            this.onSwitched = e -> {
            };
        }

        public Entry(E content, Consumer<E> onSwitched) {
            this.content = content;
            this.onSwitched = onSwitched;
        }

        public Component getNarration() {
            Language language = Language.getInstance();
            if (language.has(content.toString())) {
                return new TranslatableComponent(content.toString());
            } else {
                return new TextComponent(content.toString());
            }
        }

        public E getContent() {
            return content;
        }

        public void setContent(E content) {
            this.content = content;
        }

        public Consumer<E> getOnSwitched() {
            return onSwitched;
        }

        public void setOnSwitched(Consumer<E> onSwitched) {
            this.onSwitched = onSwitched;
        }
    }
}
