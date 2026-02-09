package cn.ussshenzhou.madparticle.mixin;

import cn.ussshenzhou.madparticle.MadParticleConfig;
import cn.ussshenzhou.madparticle.api.AddParticleHelperC;
import cn.ussshenzhou.t88.config.ConfigHelper;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ParticleStatus;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.WritableLevelData;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * @author USS_Shenzhou
 */
@Mixin(ClientLevel.class)
public abstract class ClientLevelMixin extends Level {

    @Shadow
    @Final
    private Minecraft minecraft;

    protected ClientLevelMixin(WritableLevelData levelData, ResourceKey<Level> dimension, RegistryAccess registryAccess, Holder<DimensionType> dimensionTypeRegistration, boolean isClientSide, boolean isDebug, long biomeZoomSeed, int maxChainedNeighborUpdates) {
        super(levelData, dimension, registryAccess, dimensionTypeRegistration, isClientSide, isDebug, biomeZoomSeed, maxChainedNeighborUpdates);
    }

    @Shadow
    protected abstract ParticleStatus calculateParticleLevel(boolean pDecreased);

    @Inject(method = "doAddParticle", at = @At("HEAD"), cancellable = true)
    private void madparticleCheckParticleGenerateDistance(ParticleOptions particle, boolean overrideLimiter, boolean alwaysShowParticles, double x, double y, double z, double xd, double yd, double zd, CallbackInfo ci) {
        try {
            Camera camera = this.minecraft.gameRenderer.getMainCamera();
            ParticleStatus particleLevel = this.calculateParticleLevel(alwaysShowParticles);
            if (overrideLimiter) {
                if (ConfigHelper.getConfigRead(MadParticleConfig.class).limitMaxParticleGenerateDistance && camera.position().distanceToSqr(x, y, z) > AddParticleHelperC.getMaxParticleGenerateDistanceSqr()) {
                    return;
                }
                this.minecraft.particleEngine.createParticle(particle, x, y, z, xd, yd, zd);
            } else if (!(camera.position().distanceToSqr(x, y, z) > AddParticleHelperC.getNormalParticleGenerateDistanceSqr())) {
                if (particleLevel != ParticleStatus.MINIMAL) {
                    this.minecraft.particleEngine.createParticle(particle, x, y, z, xd, yd, zd);
                }
            }
        } catch (Throwable var19) {
            CrashReport report = CrashReport.forThrowable(var19, "Exception while adding particle");
            CrashReportCategory category = report.addCategory("Particle being added");
            category.setDetail("ID", BuiltInRegistries.PARTICLE_TYPE.getKey(particle.getType()));
            category.setDetail(
                    "Parameters", () -> ParticleTypes.CODEC.encodeStart(this.registryAccess().createSerializationContext(NbtOps.INSTANCE), particle).toString()
            );
            category.setDetail("Position", () -> CrashReportCategory.formatLocation(this, x, y, z));
            throw new ReportedException(report);
        } finally {
            ci.cancel();
        }
    }
}
