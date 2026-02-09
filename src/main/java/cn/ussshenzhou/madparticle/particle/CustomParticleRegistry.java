package cn.ussshenzhou.madparticle.particle;

import cn.ussshenzhou.madparticle.MadParticle;
import com.mojang.logging.LogUtils;
import net.minecraft.IdentifierException;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
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
@EventBusSubscriber(value = Dist.CLIENT)
public class CustomParticleRegistry {
    public static final LinkedHashMap<Identifier, List<Identifier>> CUSTOM_PARTICLES_TYPE_NAMES_AND_TEXTURES = new LinkedHashMap<>();
    public static final LinkedHashSet<ParticleType<SimpleParticleType>> CUSTOM_PARTICLE_TYPES = new LinkedHashSet<>();
    public static final LinkedHashSet<Identifier> ALL_TEXTURES = new LinkedHashSet<>();
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
            Identifier particleTypeName = fileToParticleTypeNameResLoc(file);
            if (particleTypeName == null) {
                return;
            }
            if (CUSTOM_PARTICLES_TYPE_NAMES_AND_TEXTURES.containsKey(particleTypeName)) {
                return;
            }
            List<Identifier> textureNames = fileToTextureName(files, file);
            CUSTOM_PARTICLES_TYPE_NAMES_AND_TEXTURES.put(particleTypeName, textureNames);
            ALL_TEXTURES.addAll(textureNames);
            ParticleType<SimpleParticleType> particleType = new SimpleParticleType(false);
            CUSTOM_PARTICLE_TYPES.add(particleType);
            event.register(BuiltInRegistries.PARTICLE_TYPE.key(), particleTypeName, () -> particleType);
        });
    }

    public static final String MOD_ID_SPLIT = "~";
    public static final String AGE_SPLIT = "#";

    public static @Nullable Identifier fileToParticleTypeNameResLoc(File file) {
        String name = file.getName().toLowerCase().replace(".png", "").split(AGE_SPLIT)[0];
        if (!name.contains(MOD_ID_SPLIT)) {
            name = MadParticle.MOD_ID + "~" + name;
        }
        name = name.replace(MOD_ID_SPLIT, ":");
        try {
            return Identifier.parse(name);
        } catch (IdentifierException ignored) {
            LogUtils.getLogger().error("Failed to register particle {}. This is not a valid resource location.", file.getName());
            return null;
        }
    }

    public static List<Identifier> fileToTextureName(File[] allFile, File file) {
        String key = file.getName().replace(".png", "").split(AGE_SPLIT)[0];
        List<Identifier> locations = new ArrayList<>();
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
                Identifier location = fileToTextureResLoc(optional.get());
                if (location != null) {
                    locations.add(location);
                }
            }
        } else {
            Identifier location = fileToTextureResLoc(textures.get(0));
            if (location != null) {
                locations.add(location);
            }
        }
        return locations;
    }

    private static @Nullable Identifier fileToTextureResLoc(File file) {
        String s = file.getName().replace(".png", "").replace(MOD_ID_SPLIT, ":").replace(AGE_SPLIT, "__");
        if (!s.contains(":")) {
            s = MadParticle.MOD_ID + ":" + s;
        }
        try {
            return Identifier.parse(s);
        } catch (IdentifierException ignored) {
            LogUtils.getLogger().error("Failed to register texture {}. This is not a valid resource location.", file.getName());
            return null;
        }
    }

    public static String listToJsonString(List<Identifier> locations) {
        StringBuilder builder = new StringBuilder();
        locations.forEach(location -> builder
                .append("\"")
                .append(location.toString())
                .append("\",\n")
        );
        builder.delete(builder.length() - 2, builder.length());
        return builder.toString();
    }

    public static File textureResLocToFile(Identifier location) throws FileNotFoundException {
        String name = location.toString().replace(MadParticle.MOD_ID + ":", "").replace(":", MOD_ID_SPLIT).replace("__", "#") + ".png";
        File file = new File(gameDir, "customparticles/" + name);
        if (!file.exists()) {
            throw new FileNotFoundException();
        }
        return file;
    }

    @SubscribeEvent
    public static void onParticleProviderRegistry(RegisterParticleProvidersEvent event) {
        CUSTOM_PARTICLE_TYPES.forEach(particleType -> event.registerSpriteSet(particleType, pSprites -> new GeneralNullProvider()));
    }

    public static class GeneralNullProvider implements ParticleProvider<SimpleParticleType> {

        public GeneralNullProvider() {
        }

        @Override
        public @org.jspecify.annotations.Nullable Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double x, double y, double z, double vx, double vy, double vz, RandomSource randomSource) {
            return null;
        }
    }
}
