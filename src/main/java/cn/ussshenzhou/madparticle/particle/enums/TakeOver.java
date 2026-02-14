package cn.ussshenzhou.madparticle.particle.enums;

import cn.ussshenzhou.madparticle.MadParticleConfig;
import cn.ussshenzhou.madparticle.particle.render.ModParticleRenderTypes;
import cn.ussshenzhou.t88.config.ConfigHelper;
import cn.ussshenzhou.t88.gui.util.ITranslatable;
import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import net.minecraft.client.particle.*;
import org.jspecify.annotations.Nullable;

import java.util.HashSet;
import java.util.Objects;

/**
 * @author USS_Shenzhou
 */

public enum TakeOver implements ITranslatable {
    NONE("gui.mp.de.setting.additional.takeover.none"),
    VANILLA("gui.mp.de.setting.additional.takeover.vanilla"),
    ALL("gui.mp.de.setting.additional.takeover.all");

    @SuppressWarnings("unchecked")
    public static final HashSet<Class<? extends Particle>> SYNC_TICK = Sets.newHashSet(
            SimpleAnimatedParticle.class,
            RisingParticle.class,
            DustParticleBase.class,
            BaseAshSmokeParticle.class,
            AshParticle.class,
            FallingLeavesParticle.class,
            LavaParticle.class,
            BreakingItemParticle.class,
            DripParticle.DripHangParticle.class,
            DripParticle.DripstoneFallAndLandParticle.class,
            DripParticle.FallAndLandParticle.class,
            DripParticle.HoneyFallAndLandParticle.class,
            FireworkParticles.Starter.class,
            HugeExplosionSeedParticle.class
    );


    @SuppressWarnings("unchecked")
    public static final HashSet<Class<? extends Particle>> ASYNC_TICK = Sets.newHashSet(
            BlockMarker.class,
            TerrainParticle.class,
            DustColorTransitionParticle.class,
            SonicBoomParticle.class,
            FallingDustParticle.class,
            FireworkParticles.OverlayParticle.class,
            SnowflakeParticle.class,
            SpitParticle.class,
            AttackSweepParticle.class,
            VibrationSignalParticle.class,
            ShriekParticle.class,
            SpellParticle.class,
            HeartParticle.class,
            BubbleParticle.class,
            BubbleColumnUpParticle.class,
            BubblePopParticle.class,
            CampfireSmokeParticle.class,
            PlayerCloudParticle.class,
            SuspendedTownParticle.class,
            CritParticle.class,
            WaterCurrentDownParticle.class,
            DragonBreathParticle.class,
            DripParticle.class,
            DustParticle.class,
            EndRodParticle.class,
            FallingDustParticle.class,
            WakeParticle.class,
            FlameParticle.class,
            SoulParticle.class,
            SculkChargeParticle.class,
            SculkChargePopParticle.class,
            LargeSmokeParticle.class,
            NoteParticle.class,
            ExplodeParticle.class,
            PortalParticle.class,
            WaterDropParticle.class,
            SmokeParticle.class,
            SplashParticle.class,
            TotemParticle.class,
            SquidInkParticle.class,
            SuspendedParticle.class,
            ReversePortalParticle.class,
            WhiteAshParticle.class,
            GlowParticle.class
    );
    private static final HashSet<Class<? extends Particle>> RENDER_BLACKLIST = new HashSet<>();
    @SuppressWarnings("unchecked")
    private static final HashSet<Class<? extends SingleQuadParticle>> RENDER_VANILLA = Sets.newHashSet(
            SnowflakeParticle.class,
            SpitParticle.class,
            SpellParticle.class,
            HeartParticle.class,
            BubbleParticle.class,
            BubbleColumnUpParticle.class,
            BubblePopParticle.class,
            CampfireSmokeParticle.class,
            PlayerCloudParticle.class,
            SuspendedTownParticle.class,
            CritParticle.class,
            WaterCurrentDownParticle.class,
            DragonBreathParticle.class,
            DripParticle.class,
            DustParticle.class,
            EndRodParticle.class,
            FallingDustParticle.class,
            WakeParticle.class,
            FlameParticle.class,
            SoulParticle.class,
            SculkChargeParticle.class,
            SculkChargePopParticle.class,
            LargeSmokeParticle.class,
            LavaParticle.class,
            NoteParticle.class,
            ExplodeParticle.class,
            PortalParticle.class,
            WaterDropParticle.class,
            SmokeParticle.class,
            SplashParticle.class,
            TotemParticle.class,
            SquidInkParticle.class,
            SuspendedParticle.class,
            ReversePortalParticle.class,
            WhiteAshParticle.class,
            GlowParticle.class
    );
    @SuppressWarnings("unchecked")
    public static final HashSet<Class<? extends SingleQuadParticle>> RENDER_CUSTOM_LIGHT = Sets.newHashSet(
            FlameParticle.class,
            SoulParticle.class,
            SculkChargeParticle.class,
            SculkChargePopParticle.class,
            LavaParticle.class,
            PortalParticle.class,
            GlowParticle.class
    );
    private final String translateKey;

    private TakeOver(String translateKey) {
        this.translateKey = translateKey;
    }

    @Override
    public String translateKey() {
        return translateKey;
    }

    public static ParticleRenderType map(Particle particle) {
        var originalType = particle.getGroup();
        if (!(particle instanceof SingleQuadParticle)) {
            return originalType;
        }
        if (originalType == ModParticleRenderTypes.INSTANCED || originalType == ModParticleRenderTypes.INSTANCED_TERRAIN) {
            return originalType;
        }
        return switch (originalType.name()) {
            case "INSTANCED" -> ModParticleRenderTypes.INSTANCED;
            case "INSTANCED_TERRAIN" -> ModParticleRenderTypes.INSTANCED_TERRAIN;
            case "SINGLE_QUADS" -> switch (ConfigHelper.getConfigRead(MadParticleConfig.class).takeOverRendering) {
                case NONE -> originalType;
                case ALL -> RENDER_BLACKLIST.contains(particle.getClass()) ? ParticleRenderType.SINGLE_QUADS : ModParticleRenderTypes.INSTANCED;
                case VANILLA -> RENDER_VANILLA.contains(particle.getClass()) ? ModParticleRenderTypes.INSTANCED : originalType;
            };
            default -> originalType;
        };
    }

    @SuppressWarnings("unchecked")
    public static void addFromConfig() {
        var cfg = ConfigHelper.getConfigRead(MadParticleConfig.class);
        cfg.takeOverTickBlackList.stream().map(TakeOver::loadClassFromName)
                .filter(Objects::nonNull)
                .forEach(clazz -> SYNC_TICK.add((Class<? extends Particle>) clazz));
        cfg.takeOverRenderBlackList.stream().map(TakeOver::loadClassFromName)
                .filter(Objects::nonNull)
                .forEach(clazz -> RENDER_BLACKLIST.add((Class<? extends Particle>) clazz));
    }

    private static @Nullable Class<?> loadClassFromName(String s) {
        try {
            var clazz = Class.forName(s);
            if (Particle.class.isAssignableFrom(clazz)) {
                return clazz;
            } else {
                throw new IllegalArgumentException(s + " is not a subclass of Particle.");
            }
        } catch (ClassNotFoundException e) {
            LogUtils.getLogger().error(s + "does not exist. An invalid name or mod not installed?");
            return null;
        } catch (Exception e) {
            LogUtils.getLogger().error("Something went wrong trying to add " + s + " to tick blacklist.");
            LogUtils.getLogger().error(e.getMessage());
            return null;
        }
    }

    public enum TickType {
        SYNC,
        ASYNC,
        UNKNOWN
    }
}
