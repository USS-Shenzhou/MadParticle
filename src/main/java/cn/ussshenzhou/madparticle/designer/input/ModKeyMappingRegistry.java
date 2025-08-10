package cn.ussshenzhou.madparticle.designer.input;

import cn.ussshenzhou.madparticle.designer.gui.DesignerScreen;
import cn.ussshenzhou.madparticle.util.CameraHelper;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.CameraType;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyModifier;
import net.neoforged.bus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

/**
 * @author USS_Shenzhou
 */
@EventBusSubscriber(value = Dist.CLIENT)
public class ModKeyMappingRegistry {
    public static final KeyMapping CALL_OUT_DESIGNER = new KeyMapping(
            "key.mp.call_de", KeyConflictContext.UNIVERSAL, KeyModifier.ALT,
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_M, "key.categories.madparticle"
    );
    public static final KeyMapping CLEAR_DESIGNER = new KeyMapping(
            "key.mp.clear_de", KeyConflictContext.UNIVERSAL, KeyModifier.CONTROL,
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_M, "key.categories.madparticle"
    );

    @SubscribeEvent
    public static void onClientSetup(RegisterKeyMappingsEvent event) {
        event.register(ModKeyMappingRegistry.CALL_OUT_DESIGNER);
        event.register(ModKeyMappingRegistry.CLEAR_DESIGNER);
    }

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        Minecraft minecraft = Minecraft.getInstance();
        var cameraType = minecraft.options.getCameraType();
        if (CALL_OUT_DESIGNER.consumeClick()) {
            var screen = DesignerScreen.getInstance(cameraType);
            if (screen == null) {
                minecraft.setScreen(DesignerScreen.newInstance(cameraType));
            } else {
                minecraft.setScreen(DesignerScreen.getInstance(cameraType));
            }
            CameraHelper.setCameraType(CameraType.THIRD_PERSON_BACK);
        } else if (CLEAR_DESIGNER.consumeClick()) {
            if (minecraft.screen instanceof DesignerScreen) {
                minecraft.setScreen(null);
                minecraft.setScreen(DesignerScreen.newInstance(cameraType));
                CameraHelper.setCameraType(CameraType.THIRD_PERSON_BACK);
            } else {
                DesignerScreen.newInstance(cameraType);
            }
        }
    }
}
