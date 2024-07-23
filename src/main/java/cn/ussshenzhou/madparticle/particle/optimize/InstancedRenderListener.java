package cn.ussshenzhou.madparticle.particle.optimize;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;

/**
 * @author USS_Shenzhou
 */
@EventBusSubscriber(bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class InstancedRenderListener {

    @SubscribeEvent
    public static void onChangeDimension(LevelEvent.Unload event) {
        InstancedRenderManager.clear();
    }
}
