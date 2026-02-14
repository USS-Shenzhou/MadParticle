package cn.ussshenzhou.madparticle;

import cn.ussshenzhou.madparticle.command.inheritable.ModCommandArgumentRegistry;
import cn.ussshenzhou.madparticle.item.ModItemsRegistry;
import cn.ussshenzhou.madparticle.item.component.ModDataComponent;
import cn.ussshenzhou.madparticle.particle.ModParticleTypeRegistry;
import cn.ussshenzhou.madparticle.particle.enums.TakeOver;
import cn.ussshenzhou.t88.config.ConfigHelper;
import com.mojang.logging.LogUtils;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import org.slf4j.Logger;

import java.lang.reflect.Method;

/**
 * @author USS_Shenzhou
 */
@Mod("madparticle")
public class MadParticle {
    public static final String MOD_ID = "madparticle";
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final boolean IS_OPTIFINE_INSTALLED = isClassFound("net.optifine.reflect.ReflectorClass");
    public static final boolean IS_IRIS_INSTALLED = ModList.get().isLoaded("iris");
    public static boolean irisOn;

    public MadParticle(IEventBus modEventBus) {
        modEventBus.addListener(this::setup);
        modEventBus.addListener(this::clientSetup);
        NeoForge.EVENT_BUS.register(this);
        ModParticleTypeRegistry.PARTICLE_TYPES.register(modEventBus);
        ModCommandArgumentRegistry.COMMAND_ARGUMENTS.register(modEventBus);
        ModItemsRegistry.ITEMS.register(modEventBus);
        ModDataComponent.DATA_COMPONENTS.register(modEventBus);
    }

    public static boolean isClassFound(String className) {
        try {
            Class.forName(className, false, Thread.currentThread().getContextClassLoader());
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private void setup(final FMLCommonSetupEvent event) {
        LOGGER.info("WelCome to the world of MadParticle!");
    }

    private void clientSetup(FMLClientSetupEvent event) {
        ConfigHelper.loadConfig(new MadParticleConfig());
        TakeOver.addFromConfig();
    }

    @SubscribeEvent
    public void onClientTick(ClientTickEvent.Pre event) {
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
