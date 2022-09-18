package cn.ussshenzhou.madparticle;

import cn.ussshenzhou.madparticle.particle.ModParticleRegistry;
import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

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

    public MadParticle() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        MinecraftForge.EVENT_BUS.register(this);
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModParticleRegistry.PARTICLE_TYPES.register(modBus);
    }

    private void setup(final FMLCommonSetupEvent event) {
        LOGGER.info("WelCome to the world of MadParticle!");
    }

}
