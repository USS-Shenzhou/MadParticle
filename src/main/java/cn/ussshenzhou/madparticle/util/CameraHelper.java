package cn.ussshenzhou.madparticle.util;

import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;

/**
 * @author USS_Shenzhou
 */
public class CameraHelper {

    public static void setCameraType(CameraType type) {
        var mc = Minecraft.getInstance();
        var prevCameratype = mc.options.getCameraType();
        mc.options.setCameraType(type);
        if (prevCameratype.isFirstPerson() != mc.options.getCameraType().isFirstPerson()) {
            mc.gameRenderer.checkEntityPostEffect(mc.options.getCameraType().isFirstPerson() ? mc.getCameraEntity() : null);
        }
        mc.levelRenderer.needsUpdate();
    }
}
