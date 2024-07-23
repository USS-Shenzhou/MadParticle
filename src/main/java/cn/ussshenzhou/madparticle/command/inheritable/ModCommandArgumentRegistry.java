package cn.ussshenzhou.madparticle.command.inheritable;

import cn.ussshenzhou.madparticle.MadParticle;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;


/**
 * @author USS_Shenzhou
 */
@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
public class ModCommandArgumentRegistry {

    private static SingletonArgumentInfo<InheritableIntegerArgument> inheritableIntegerArgumentInfo = SingletonArgumentInfo.contextFree(InheritableIntegerArgument::inheritableInteger);
    private static SingletonArgumentInfo<InheritableFloatArgument> inheritableFloatArgumentInfo = SingletonArgumentInfo.contextFree(InheritableFloatArgument::inheritableFloat);
    private static SingletonArgumentInfo<InheritableDoubleArgument> inheritableDoubleArgumentInfo = SingletonArgumentInfo.contextFree(InheritableDoubleArgument::inheritableDouble);
    private static SingletonArgumentInfo<InheritableVec3Argument> inheritableVec3ArgumentInfo = SingletonArgumentInfo.contextFree(InheritableVec3Argument::inheritableVec3);

    @SubscribeEvent
    public static void onCommandArgumentReg(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            ArgumentTypeInfos.registerByClass(InheritableIntegerArgument.class, inheritableIntegerArgumentInfo);
            ArgumentTypeInfos.registerByClass(InheritableFloatArgument.class, inheritableFloatArgumentInfo);
            ArgumentTypeInfos.registerByClass(InheritableDoubleArgument.class, inheritableDoubleArgumentInfo);
            ArgumentTypeInfos.registerByClass(InheritableVec3Argument.class, inheritableVec3ArgumentInfo);
        });
    }


    public static DeferredRegister<ArgumentTypeInfo<?, ?>> COMMAND_ARGUMENTS = DeferredRegister.create(BuiltInRegistries.COMMAND_ARGUMENT_TYPE, MadParticle.MOD_ID);

    public static Supplier<ArgumentTypeInfo<?, ?>> INHERITABLE_INT = COMMAND_ARGUMENTS.register("inheritable_integer", () -> inheritableIntegerArgumentInfo);
    public static Supplier<ArgumentTypeInfo<?, ?>> INHERITABLE_FLOAT = COMMAND_ARGUMENTS.register("inheritable_float", () -> inheritableFloatArgumentInfo);
    public static Supplier<ArgumentTypeInfo<?, ?>> INHERITABLE_DOUBLE = COMMAND_ARGUMENTS.register("inheritable_double", () -> inheritableDoubleArgumentInfo);
    public static Supplier<ArgumentTypeInfo<?, ?>> INHERITABLE_VEC3 = COMMAND_ARGUMENTS.register("inheritable_vec3", () -> inheritableVec3ArgumentInfo);
}
