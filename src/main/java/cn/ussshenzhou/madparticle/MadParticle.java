package cn.ussshenzhou.madparticle;

import cn.ussshenzhou.madparticle.command.inheritable.ModCommandArgumentRegistry;
import cn.ussshenzhou.madparticle.item.ModItemsRegistry;
import cn.ussshenzhou.madparticle.particle.ModParticleTypeRegistry;
import cn.ussshenzhou.t88.config.ConfigHelper;
import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

import java.util.function.Supplier;

/**
 * TODO:
 * <p>Designer: translocation-priority mode</p>
 *
 * @author USS_Shenzhou
 */
@Mod("madparticle")
public class MadParticle {
    public static final String MOD_ID = "madparticle";
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final boolean isOptifineInstalled = isClassFound("net.optifine.reflect.ReflectorClass");
    public static final boolean isShimmerInstalled = ModList.get().isLoaded("shimmer");

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
        if (isShimmerInstalled) {
            run.get().run();
        }
    }

    private void setup(final FMLCommonSetupEvent event) {
        LOGGER.info("WelCome to the world of MadParticle!");
    }

    private void clientSetup(FMLClientSetupEvent event){
        ConfigHelper.loadConfig(new MadParticleConfig());
    }

}
