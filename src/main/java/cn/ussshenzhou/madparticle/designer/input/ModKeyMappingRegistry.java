package cn.ussshenzhou.madparticle.designer.input;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;

/**
 * @author USS_Shenzhou
 */
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD,value = Dist.CLIENT)
public class ModKeyMappingRegistry {
    @SubscribeEvent
    public static void onClientSetup(RegisterKeyMappingsEvent event) {
        event.register(DesignerKeyInput.CALL_OUT_DESIGNER);
        event.register(DesignerKeyInput.CLEAR_DESIGNER);
    }
}
