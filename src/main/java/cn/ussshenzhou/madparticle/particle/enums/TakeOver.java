package cn.ussshenzhou.madparticle.particle.enums;

import cn.ussshenzhou.madparticle.MadParticleConfig;
import cn.ussshenzhou.madparticle.particle.ModParticleRenderTypes;
import cn.ussshenzhou.t88.config.ConfigHelper;
import cn.ussshenzhou.t88.gui.util.ITranslatable;
import com.google.common.collect.Sets;
import net.minecraft.client.particle.*;

import java.util.HashSet;

/**
 * @author USS_Shenzhou
 */

public enum TakeOver implements ITranslatable {
    NONE("gui.mp.de.setting.additional.takeover.none"),
    VANILLA("gui.mp.de.setting.additional.takeover.vanilla"),
    ALL("gui.mp.de.setting.additional.takeover.all");

    @SuppressWarnings("unchecked")
    public static final HashSet<Class<? extends Particle>> SYNC_TICK_VANILLA_AND_MADPARTICLE = Sets.newHashSet(
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
    public static final HashSet<Class<? extends Particle>> ASYNC_TICK_VANILLA_AND_MADPARTICLE = Sets.newHashSet(
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
    @SuppressWarnings("unchecked")
    private static final HashSet<Class<? extends TextureSheetParticle>> RENDER_VANILLA_TRANS_OPAQUE = Sets.newHashSet(
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
    public static final HashSet<Class<? extends TextureSheetParticle>> RENDER_CUSTOM_LIGHT = Sets.newHashSet(
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
        var originalType = particle.getRenderType();
        if (!(particle instanceof TextureSheetParticle)) {
            return originalType;
        }
        if (originalType == ModParticleRenderTypes.INSTANCED || originalType == ModParticleRenderTypes.INSTANCED_TERRAIN) {
            return originalType;
        }
        var takeover = ConfigHelper.getConfigRead(MadParticleConfig.class).takeOverRendering;
        return switch (originalType.name()) {
            case "INSTANCED" -> ModParticleRenderTypes.INSTANCED;
            case "INSTANCED_TERRAIN" -> ModParticleRenderTypes.INSTANCED_TERRAIN;
            case "TERRAIN_OPAQUE", "TERRAIN_SHEET" -> takeover == NONE ? originalType : ModParticleRenderTypes.INSTANCED_TERRAIN;
            case "PARTICLE_SHEET_OPAQUE", "PARTICLE_SHEET_TRANSLUCENT" -> switch (ConfigHelper.getConfigRead(MadParticleConfig.class).takeOverRendering) {
                case NONE -> originalType;
                case ALL -> ModParticleRenderTypes.INSTANCED;
                case VANILLA -> RENDER_VANILLA_TRANS_OPAQUE.contains(particle.getClass()) ? ModParticleRenderTypes.INSTANCED : originalType;
            };
            default -> originalType;
        };


    }

    public enum TickType {
        SYNC,
        ASYNC,
        UNKNOWN
    }
}
