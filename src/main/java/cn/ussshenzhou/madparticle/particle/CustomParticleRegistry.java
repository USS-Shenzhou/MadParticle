package cn.ussshenzhou.madparticle.particle;

import cn.ussshenzhou.madparticle.MadParticle;
import com.mojang.logging.LogUtils;
import net.minecraft.ResourceLocationException;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.RegisterEvent;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

/**
 * @author USS_Shenzhou
 * <p>
 * File name: modid~leaves.png  modid~leaves#1.png
 * <p>
 * Particle type reg name: modid:leaves
 * <p>
 * Particle texture name in json: modid:leaves modid:leaves__1
 */
@EventBusSubscriber
public class CustomParticleRegistry {
    public static final LinkedHashMap<ResourceLocation, List<ResourceLocation>> CUSTOM_PARTICLES_TYPE_NAMES_AND_TEXTURES = new LinkedHashMap<>();
    public static final LinkedHashSet<ParticleType<SimpleParticleType>> CUSTOM_PARTICLE_TYPES = new LinkedHashSet<>();
    public static final LinkedHashSet<ResourceLocation> ALL_TEXTURES = new LinkedHashSet<>();
    public static File gameDir = FMLPaths.GAMEDIR.get().toFile();

    @SubscribeEvent
    public static void registerParticleType(RegisterEvent event) {
        if (!event.getRegistryKey().equals(BuiltInRegistries.PARTICLE_TYPE.key())) {
            return;
        }
        File particleDir = new File(gameDir, "customparticles");
        if (!particleDir.exists() || !particleDir.isDirectory()) {
            particleDir.mkdir();
            return;
        }
        File[] files = particleDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".png"));
        if (files == null) {
            return;
        }
        Arrays.stream(files).filter(file -> !file.isDirectory()).forEach(file -> {
            ResourceLocation particleTypeName = fileToParticleTypeNameResLoc(file);
            if (particleTypeName == null) {
                return;
            }
            if (CUSTOM_PARTICLES_TYPE_NAMES_AND_TEXTURES.containsKey(particleTypeName)) {
                return;
            }
            List<ResourceLocation> textureNames = fileToTextureName(files, file);
            CUSTOM_PARTICLES_TYPE_NAMES_AND_TEXTURES.put(particleTypeName, textureNames);
            ALL_TEXTURES.addAll(textureNames);
            ParticleType<SimpleParticleType> particleType = new SimpleParticleType(false);
            CUSTOM_PARTICLE_TYPES.add(particleType);
            event.register(BuiltInRegistries.PARTICLE_TYPE.key(), particleTypeName, () -> particleType);
        });
    }

    public static final String MOD_ID_SPLIT = "~";
    public static final String AGE_SPLIT = "#";

    public static @Nullable ResourceLocation fileToParticleTypeNameResLoc(File file) {
        String name = file.getName().toLowerCase().replace(".png", "").split(AGE_SPLIT)[0];
        if (!name.contains(MOD_ID_SPLIT)) {
            name = MadParticle.MOD_ID + "~" + name;
        }
        name = name.replace(MOD_ID_SPLIT, ":");
        try {
            return ResourceLocation.parse(name);
        } catch (ResourceLocationException ignored) {
            LogUtils.getLogger().error("Failed to register particle {}. This is not a valid resource location.", file.getName());
            return null;
        }
    }

    public static List<ResourceLocation> fileToTextureName(File[] allFile, File file) {
        String key = file.getName().replace(".png", "").split(AGE_SPLIT)[0];
        List<ResourceLocation> locations = new ArrayList<>();
        //This will cause an n*n in the worst situation. But it's in the loading stage. Just wait.
        List<File> textures = Arrays.stream(allFile).filter(f -> f.getName().contains(key)).toList();
        if (textures.size() > 1) {
            for (int i = 0; i < textures.size(); i++) {
                String supposedName = key + "#" + i + ".png";
                Optional<File> optional = textures.stream().filter(f -> f.getName().contains(supposedName)).findFirst();
                if (optional.isEmpty()) {
                    LogUtils.getLogger().error("Failed to find texture {}. Check if it is a valid name.", supposedName);
                    continue;
                }
                ResourceLocation location = fileToTextureResLoc(optional.get());
                if (location != null) {
                    locations.add(location);
                }
            }
        } else {
            ResourceLocation location = fileToTextureResLoc(textures.get(0));
            if (location != null) {
                locations.add(location);
            }
        }
        return locations;
    }

    private static @Nullable ResourceLocation fileToTextureResLoc(File file) {
        String s = file.getName().replace(".png", "").replace(MOD_ID_SPLIT, ":").replace(AGE_SPLIT, "__");
        if (!s.contains(":")) {
            s = MadParticle.MOD_ID + ":" + s;
        }
        try {
            return ResourceLocation.parse(s);
        } catch (ResourceLocationException ignored) {
            LogUtils.getLogger().error("Failed to register texture {}. This is not a valid resource location.", file.getName());
            return null;
        }
    }

    public static String listToJsonString(List<ResourceLocation> locations) {
        StringBuilder builder = new StringBuilder();
        locations.forEach(location -> builder
                .append("\"")
                .append(location.toString())
                .append("\",\n")
        );
        builder.delete(builder.length() - 2, builder.length());
        return builder.toString();
    }

    public static File textureResLocToFile(ResourceLocation location) throws FileNotFoundException {
        String name = location.toString().replace(MadParticle.MOD_ID + ":", "").replace(":", MOD_ID_SPLIT).replace("__", "#") + ".png";
        File file = new File(gameDir, "customparticles/" + name);
        if (!file.exists()) {
            throw new FileNotFoundException();
        }
        return file;
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void onParticleProviderRegistry(RegisterParticleProvidersEvent event) {
        CUSTOM_PARTICLE_TYPES.forEach(particleType -> event.registerSpriteSet(particleType, pSprites -> new GeneralNullProvider()));
    }

    @OnlyIn(Dist.CLIENT)
    public static class GeneralNullProvider implements ParticleProvider<SimpleParticleType> {

        public GeneralNullProvider() {
        }

        @Nullable
        @Override
        public Particle createParticle(SimpleParticleType pType, ClientLevel pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
            return null;
        }
    }
}
