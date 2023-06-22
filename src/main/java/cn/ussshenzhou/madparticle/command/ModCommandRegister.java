package cn.ussshenzhou.madparticle.command;

import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * @author USS_Shenzhou
 */
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ModCommandRegister {
    @SubscribeEvent
    public static void regCommand(RegisterCommandsEvent event) {
        new MadParticleCommand(event.getDispatcher());
        new MadParticleCommandTeaCon(event.getDispatcher());
    }
}
