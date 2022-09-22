package cn.ussshenzhou.madparticle.designer.universal.util;

import net.minecraft.client.Minecraft;

/**
 * @author USS_Shenzhou
 */
public class MouseHelper {
    static Minecraft minecraft = Minecraft.getInstance();

    public static double getMouseX(){
        return minecraft.mouseHandler.xpos() * minecraft.getWindow().getGuiScaledWidth() / minecraft.getWindow().getScreenWidth();
    }

    public static double getMouseY(){
        return minecraft.mouseHandler.ypos() * minecraft.getWindow().getGuiScaledHeight() / minecraft.getWindow().getScreenHeight();
    }
}
