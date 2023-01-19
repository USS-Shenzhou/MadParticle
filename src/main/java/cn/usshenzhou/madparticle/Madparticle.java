package cn.usshenzhou.madparticle;

import cn.usshenzhou.madparticle.command.MadParticleCommand;
import cn.usshenzhou.madparticle.command.forge.EnumArgument;
import cn.usshenzhou.madparticle.command.inheritable.InheritableDoubleArgument;
import cn.usshenzhou.madparticle.command.inheritable.InheritableFloatArgument;
import cn.usshenzhou.madparticle.command.inheritable.InheritableIntegerArgument;
import cn.usshenzhou.madparticle.command.inheritable.InheritableVec3Argument;
import cn.usshenzhou.madparticle.particle.MadParticleType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.commands.synchronization.ArgumentTypes;
import net.minecraft.commands.synchronization.EmptyArgumentSerializer;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import org.intellij.lang.annotations.Identifier;

import java.util.function.Supplier;

/**
 * @author USS_Shenzhou
 */
public class Madparticle implements ModInitializer {
    public static final boolean isShimmerInstalled = FabricLoader.getInstance().isModLoaded("shimmer");
    public static final boolean isOptifineInstalled = isClassFound("net.optifine.reflect.ReflectorClass");
    public static final MadParticleType MAD_PARTICLE = new MadParticleType();

    @SuppressWarnings("InstantiationOfUtilityClass")
    @Override
    public void onInitialize() {
        ArgumentTypes.register("madparticle:inheritable_integer", InheritableIntegerArgument.class, new EmptyArgumentSerializer<>(InheritableIntegerArgument::inheritableInteger));
        ArgumentTypes.register("madparticle:inheritable_float", InheritableFloatArgument.class, new EmptyArgumentSerializer<>(InheritableFloatArgument::inheritableFloat));
        ArgumentTypes.register("madparticle:inheritable_double", InheritableDoubleArgument.class, new EmptyArgumentSerializer<>(InheritableDoubleArgument::inheritableDouble));
        ArgumentTypes.register("madparticle:inheritable_vec3", InheritableVec3Argument.class,new EmptyArgumentSerializer<>(InheritableVec3Argument::inheritableVec3));
        ArgumentTypes.register("froge:enum", EnumArgument.class,new EmptyArgumentSerializer<>(() -> EnumArgument.enumArgument(Enum.class)));

        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            new MadParticleCommand(dispatcher);
        });

        Registry.register(Registry.PARTICLE_TYPE,new ResourceLocation("madparticle","mad_particle"),MAD_PARTICLE);
    }

    public static boolean isClassFound(String className) {
        try {
            Class.forName(className, false, Thread.currentThread().getContextClassLoader());
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static void runOnShimmer(Supplier<Runnable> run){
        if (isShimmerInstalled){
            run.get().run();
        }
    }
}
