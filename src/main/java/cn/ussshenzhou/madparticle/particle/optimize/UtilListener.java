package cn.ussshenzhou.madparticle.particle.optimize;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.LevelEvent;

/**
 * @author USS_Shenzhou
 */
@EventBusSubscriber(value = Dist.CLIENT)
public class UtilListener {

    @SubscribeEvent
    public static void onLeaveLevel(LevelEvent.Unload event) {
        NeoInstancedRenderManager.getAllInstances().forEach(NeoInstancedRenderManager::clear);
    }
}
