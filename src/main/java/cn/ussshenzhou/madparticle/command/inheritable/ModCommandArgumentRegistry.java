package cn.ussshenzhou.madparticle.command.inheritable;

import cn.ussshenzhou.madparticle.MadParticle;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * @author USS_Shenzhou
 */
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModCommandArgumentRegistry {
    @SubscribeEvent
    public static void onCommandArgumentReg(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            ArgumentTypeInfos.registerByClass(InheritableIntegerArgument.class, SingletonArgumentInfo.contextFree(InheritableIntegerArgument::inheritableInteger));
            ArgumentTypeInfos.registerByClass(InheritableFloatArgument.class, SingletonArgumentInfo.contextFree(InheritableFloatArgument::inheritableFloat));
            ArgumentTypeInfos.registerByClass(InheritableDoubleArgument.class, SingletonArgumentInfo.contextFree(InheritableDoubleArgument::inheritableDouble));
            ArgumentTypeInfos.registerByClass(InheritableVec3Argument.class, SingletonArgumentInfo.contextFree(InheritableVec3Argument::inheritableVec3));
        });
    }

    public static DeferredRegister<ArgumentTypeInfo<?, ?>> COMMAND_ARGUMENTS = DeferredRegister.create(ForgeRegistries.COMMAND_ARGUMENT_TYPES, MadParticle.MOD_ID);

    public static RegistryObject<ArgumentTypeInfo<?, ?>> INHERITABLE_INT = COMMAND_ARGUMENTS.register("inheritable_integer", () -> SingletonArgumentInfo.contextFree(InheritableIntegerArgument::inheritableInteger));
    public static RegistryObject<ArgumentTypeInfo<?, ?>> INHERITABLE_FLOAT = COMMAND_ARGUMENTS.register("inheritable_float", () -> SingletonArgumentInfo.contextFree(InheritableFloatArgument::inheritableFloat));
    public static RegistryObject<ArgumentTypeInfo<?, ?>> INHERITABLE_DOUBLE = COMMAND_ARGUMENTS.register("inheritable_double", () -> SingletonArgumentInfo.contextFree(InheritableDoubleArgument::inheritableDouble));
    public static RegistryObject<ArgumentTypeInfo<?, ?>> INHERITABLE_VEC3 = COMMAND_ARGUMENTS.register("inheritable_vec3", () -> SingletonArgumentInfo.contextFree(InheritableVec3Argument::inheritableVec3));
}
