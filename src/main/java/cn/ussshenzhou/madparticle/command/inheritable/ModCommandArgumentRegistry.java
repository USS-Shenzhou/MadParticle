package cn.ussshenzhou.madparticle.command.inheritable;

import cn.ussshenzhou.madparticle.MadParticle;
import com.mojang.brigadier.arguments.ArgumentType;
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


    public static DeferredRegister<ArgumentTypeInfo<?, ?>> COMMAND_ARGUMENTS = DeferredRegister.create(ForgeRegistries.COMMAND_ARGUMENT_TYPES, MadParticle.MOD_ID);

    public static RegistryObject<ArgumentTypeInfo<?, ?>> INHERITABLE_INT = COMMAND_ARGUMENTS.register("inheritable_integer", () -> inheritableIntegerArgumentInfo);
    public static RegistryObject<ArgumentTypeInfo<?, ?>> INHERITABLE_FLOAT = COMMAND_ARGUMENTS.register("inheritable_float", () -> inheritableFloatArgumentInfo);
    public static RegistryObject<ArgumentTypeInfo<?, ?>> INHERITABLE_DOUBLE = COMMAND_ARGUMENTS.register("inheritable_double", () -> inheritableDoubleArgumentInfo);
    public static RegistryObject<ArgumentTypeInfo<?, ?>> INHERITABLE_VEC3 = COMMAND_ARGUMENTS.register("inheritable_vec3", () -> inheritableVec3ArgumentInfo);
}
