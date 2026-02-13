package cn.ussshenzhou.madparticle.designer.gui;

import cn.ussshenzhou.madparticle.particle.render.NeoInstancedRenderManager;
import cn.ussshenzhou.madparticle.particle.render.ParallelTickManager;
import cn.ussshenzhou.t88.gui.widegt.TLabel;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

/**
 * @author USS_Shenzhou
 */
public class ParticleCounterHud extends TLabel {

    public ParticleCounterHud() {
        this.setAutoScroll(false);
        this.setBackground(0x80000000);
    }

    @Override
    public void resizeAsHud(int screenWidth, int screenHeight) {
        this.setAbsBounds(4, 4, 120, 36);
        super.resizeAsHud(screenWidth, screenHeight);
    }

    @Override
    public void tickT() {
        super.tickT();
        var total = Minecraft.getInstance().particleEngine.countParticles();
        int instanced = NeoInstancedRenderManager.getAllInstances()
                .mapToInt(NeoInstancedRenderManager::getAmount)
                .sum();
        int parallel = ParallelTickManager.count();
        int add = ParallelTickManager.addCounter.get();
        int remove = ParallelTickManager.removeCounter.get();
        if ("zh_cn".equals(Minecraft.getInstance().getLanguageManager().getSelected())){
            this.setText(Component.literal("粒子总数: "
                    + total
                    + "\n并行: "
                    + parallel
                    + "\n实例化: "
                    + instanced
                    + "\n+" + add + "  -" + remove
            ));
        } else {
            this.setText(Component.literal("Particle: "
                    + total
                    + "\nParalleled: "
                    + parallel
                    + "\nInstanced: "
                    + instanced
                    + "\n+" + add + "  -" + remove
            ));
        }
    }
}
