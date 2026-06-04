package cn.ussshenzhou.madparticle.mixin.compat;

import cn.ussshenzhou.madparticle.MadParticle;
import cn.ussshenzhou.madparticle.particle.render.ModParticleRenderTypes;
import cn.ussshenzhou.madparticle.particle.render.NeoInstancedRenderManager;
import net.irisshaders.iris.pipeline.FinalPassRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author USS_Shenzhou
 */
@Mixin(FinalPassRenderer.class)
public class IrisFinalPassRendererMixin {

    @Inject(method = "renderFinalPass", at = @At(value = "INVOKE", target = "Lnet/irisshaders/iris/gl/GLDebug;popGroup()V", shift = At.Shift.BEFORE), require = 0)
    private void madparticleRender(CallbackInfo ci) {
        if (MadParticle.irisOn) {
            NeoInstancedRenderManager.forEach(NeoInstancedRenderManager::render);
        }
    }
}
