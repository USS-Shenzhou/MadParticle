package cn.ussshenzhou.madparticle.designer.universal.combine;

import cn.ussshenzhou.madparticle.designer.universal.widegt.TButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

/**
 * @author USS_Shenzhou
 */
public class TTitledEditBox extends TTitledComponent<TButton> {
    public TTitledEditBox(Component titleText, Component buttonText) {
        super(titleText, new TButton(buttonText));
    }

    public TTitledEditBox(Component titleText, Component buttonText, Button.OnPress onPress) {
        super(titleText, new TButton(buttonText, onPress));
    }
}
