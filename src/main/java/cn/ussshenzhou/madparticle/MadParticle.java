package cn.ussshenzhou.madparticle;

import cn.ussshenzhou.madparticle.command.inheritable.ModCommandArgumentRegistry;
import cn.ussshenzhou.madparticle.item.ModItemsRegistry;
import cn.ussshenzhou.madparticle.particle.ModParticleTypeRegistry;
import cn.ussshenzhou.t88.config.ConfigHelper;
import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

import java.lang.reflect.Method;
import java.util.function.Supplier;

/**
 * @author USS_Shenzhou
 */
@Mod("madparticle")
public class MadParticle {
    public static final String MOD_ID = "madparticle";
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final boolean IS_OPTIFINE_INSTALLED = isClassFound("net.optifine.reflect.ReflectorClass");
    public static final boolean IS_SHIMMER_INSTALLED = ModList.get().isLoaded("shimmer");
    public static boolean irisOn;

    public MadParticle() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientSetup);
        MinecraftForge.EVENT_BUS.register(this);
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModParticleTypeRegistry.PARTICLE_TYPES.register(modBus);
        ModCommandArgumentRegistry.COMMAND_ARGUMENTS.register(modBus);
        ModItemsRegistry.ITEMS.register(modBus);
    }

    public boolean isModLoaded(String modID) {
        return ModList.get().isLoaded(modID);
    }

    public static boolean isClassFound(String className) {
        try {
            Class.forName(className, false, Thread.currentThread().getContextClassLoader());
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static void runOnShimmer(Supplier<Runnable> run) {
        if (IS_SHIMMER_INSTALLED) {
            run.get().run();
        }
    }

    private void setup(final FMLCommonSetupEvent event) {
        LOGGER.info("WelCome to the world of MadParticle!");
    }

    private void clientSetup(FMLClientSetupEvent event) {
        ConfigHelper.loadConfig(new MadParticleConfig());
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            try {
                Class<?> irisApi = Class.forName("net.irisshaders.iris.api.v0.IrisApi");
                Method getInstance = irisApi.getMethod("getInstance");
                getInstance.setAccessible(true);
                Method isShaderPackInUse = irisApi.getMethod("isShaderPackInUse");
                isShaderPackInUse.setAccessible(true);
                irisOn = (boolean) isShaderPackInUse.invoke(getInstance.invoke(null));
            } catch (Exception ignored) {
                irisOn = false;
            }
        }
    }
}
