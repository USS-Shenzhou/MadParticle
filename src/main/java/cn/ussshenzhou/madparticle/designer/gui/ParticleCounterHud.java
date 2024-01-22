package cn.ussshenzhou.madparticle.designer.gui;

import cn.ussshenzhou.madparticle.particle.InstancedRenderManager;
import cn.ussshenzhou.madparticle.particle.ParallelTickManager;
import cn.ussshenzhou.t88.gui.widegt.TLabel;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

/**
 * @author USS_Shenzhou
 */
public class ParticleCounterHud extends TLabel {

    public ParticleCounterHud() {
        this.setAutoScroll(false);
    }

    @Override
    public void resizeAsHud(int screenWidth, int screenHeight) {
        this.setAbsBounds(0, 0, 100, 30);
        super.resizeAsHud(screenWidth, screenHeight);
    }

    @Override
    public void tickT() {
        super.tickT();
        var total = Minecraft.getInstance().particleEngine.countParticles();
        int instanced = InstancedRenderManager.amount();
        int parallel = ParallelTickManager.count();
        this.setText(Component.literal("Particle: "
                + total
                + "\nInstanced: "
                + instanced
                +"\nParalleled: "
                +parallel
        ));
    }
}
