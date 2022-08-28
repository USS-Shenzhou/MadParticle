package cn.ussshenzhou.madparticle.command.inheritable;

import net.minecraft.commands.synchronization.ArgumentTypes;
import net.minecraft.commands.synchronization.EmptyArgumentSerializer;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

/**
 * @author USS_Shenzhou
 */
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModCommandArgumentRegistry {
    @SubscribeEvent
    public static void onCommandArgumentReg(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            ArgumentTypes.register("madparticle:inheritable_integer", InheritableIntegerArgument.class, new EmptyArgumentSerializer<>(InheritableIntegerArgument::inheritableInteger));
            ArgumentTypes.register("madparticle:inheritable_float", InheritableFloatArgument.class,new EmptyArgumentSerializer<>(InheritableFloatArgument::inheritableFloat));
        });
    }
}
