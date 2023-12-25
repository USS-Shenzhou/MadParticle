package cn.ussshenzhou.madparticle.particle;

import cn.ussshenzhou.madparticle.MadParticleConfig;
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

    private static final HashSet<ParticleRenderType> ACCEPT = Sets.newHashSet(net.minecraft.client.particle.ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT, net.minecraft.client.particle.ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT);
    @SuppressWarnings("unchecked")
    public static final HashSet<Class<? extends Particle>> VANILLA_AND_MADPARTICLE = Sets.newHashSet(
            MadParticle.class,
            BlockMarker.class,
            TerrainParticle.class,
            DustColorTransitionParticle.class,
            HugeExplosionSeedParticle.class,
            HugeExplosionParticle.class,
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
            EnchantmentTableParticle.class,
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
    private static final HashSet<Class<? extends TextureSheetParticle>> VANILLA_TRANS_OPAQUE = Sets.newHashSet(
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
            EnchantmentTableParticle.class,
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
    public static final HashSet<Class<? extends TextureSheetParticle>> CUSTOM_LIGHT = Sets.newHashSet(
            EnchantmentTableParticle.class,
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

    public static ParticleRenderType check(Particle particle) {
        var originalType = particle.getRenderType();
        if (originalType == ModParticleRenderTypes.INSTANCED) {
            return ModParticleRenderTypes.INSTANCED;
        }
        return switch (ConfigHelper.getConfigRead(MadParticleConfig.class).takeOverRendering) {
            case NONE -> originalType;
            case ALL -> ACCEPT.contains(originalType) ? ModParticleRenderTypes.INSTANCED : originalType;
            case VANILLA ->
                    VANILLA_TRANS_OPAQUE.contains(particle.getClass()) ? ModParticleRenderTypes.INSTANCED : originalType;
        };
    }
}
