package cn.ussshenzhou.madparticle.designer;

import cn.ussshenzhou.madparticle.network.SendWelcomePacket;
import cn.ussshenzhou.t88.network.NetworkHelper;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;

/**
 * @author USS_Shenzhou
 */
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class WelcomeScreenListener {

    @SubscribeEvent
    public static void onPlayerLog(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            NetworkHelper.sendToPlayer(player, new SendWelcomePacket());
        }
    }
}
