package cn.ussshenzhou.madparticle.particle;

import cn.ussshenzhou.madparticle.MadParticle;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ParticleFactoryRegisterEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;
import java.io.File;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

/**
 * @author USS_Shenzhou
 */
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class CustomParticleRegistry {
    private static final LinkedHashSet<ResourceLocation> CUSTOM_PARTICLES_RES = new LinkedHashSet<>();
    private static final LinkedHashSet<ParticleType<SimpleParticleType>> CUSTOM_PARTICLES_TYPE = new LinkedHashSet<>();
    private static final LinkedHashMap<ResourceLocation, File> SPRITE_LOCATION = new LinkedHashMap<>();

    @SubscribeEvent
    public static void registerParticleType(RegistryEvent.Register<ParticleType<?>> event) {
        File particleDir = new File(Minecraft.getInstance().gameDirectory, "customparticles");
        if (!particleDir.exists() || !particleDir.isDirectory()) {
            particleDir.mkdir();
            return;
        }
        File[] files = particleDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".png"));
        if (files == null) {
            return;
        }
        Arrays.stream(files).filter(file -> !file.isDirectory()).forEach(file -> {
            String s = getResourceName(file);
            if (s == null) {
                return;
            }
            ResourceLocation location = new ResourceLocation(s);
            CUSTOM_PARTICLES_RES.add(location);
            ParticleType<SimpleParticleType> particleType = (ParticleType<SimpleParticleType>) new SimpleParticleType(false).setRegistryName(location);
            CUSTOM_PARTICLES_TYPE.add(particleType);
            event.getRegistry().register(particleType);
        });
    }

    @SubscribeEvent
    public static void onParticleProviderRegistry(ParticleFactoryRegisterEvent event) {
        ParticleEngine particleEngine = Minecraft.getInstance().particleEngine;
        CUSTOM_PARTICLES_TYPE.forEach(particleType -> particleEngine.register(particleType, GeneralNullProvider::new));
    }

    @SubscribeEvent
    public static void onTextureStitch(TextureStitchEvent.Pre event) {
        if (!event.getAtlas().location().equals(TextureAtlas.LOCATION_PARTICLES)) {
            return;
        }
        SPRITE_LOCATION.keySet().forEach(event::addSprite);
    }

    public static final String MOD_ID_SPLIT = "~";
    public static final String AGE_SPLIT = "#";

    public static @Nullable String getResourceName(File file) {
        String name = file.getName().toLowerCase().replace(".png", "").split(AGE_SPLIT)[0];
        if (!name.contains(MOD_ID_SPLIT)) {
            name = MadParticle.MOD_ID + "~" + name;
        }
        name = name.replace(MOD_ID_SPLIT, ":");
        if (ResourceLocation.isValidResourceLocation(name)) {
            return name;
        } else {
            LogUtils.getLogger().error("Failed to register {}. This is not a valid resource location.", file.getName());
            return null;
        }
    }

    public static LinkedHashSet<ResourceLocation> getCustomParticleRes() {
        return CUSTOM_PARTICLES_RES;
    }

    public static LinkedHashSet<ParticleType<SimpleParticleType>> getCustomParticleTypes() {
        return CUSTOM_PARTICLES_TYPE;
    }

    public static LinkedHashMap<ResourceLocation, File> getSpriteLocations() {
        return SPRITE_LOCATION;
    }

    public static class GeneralNullProvider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public GeneralNullProvider(SpriteSet pSprites) {
            this.sprite = pSprites;
        }

        public Particle createParticle(SimpleParticleType pType, ClientLevel pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
            return null;
        }
    }
}
