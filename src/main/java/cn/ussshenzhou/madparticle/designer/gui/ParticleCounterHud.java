package cn.ussshenzhou.madparticle.designer.gui;

import cn.ussshenzhou.madparticle.particle.render.NeoInstancedRenderManager;
import cn.ussshenzhou.madparticle.particle.render.ParallelTickManager;
import cn.ussshenzhou.t88.gui.widegt.TLabel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleGroup;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.network.chat.Component;

import java.util.Map;

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
        this.setAbsBounds(4, 4, this.getPreferredSize());
        super.resizeAsHud(screenWidth, screenHeight);
    }

    @Override
    public void tickT() {
        super.tickT();
        var total = countParticles();
        int instanced = NeoInstancedRenderManager.getAllInstances()
                .mapToInt(NeoInstancedRenderManager::getAmount)
                .sum();
        int parallel = ParallelTickManager.count();
        int add = ParallelTickManager.ADD_COUNTER.get();
        int remove = ParallelTickManager.REMOVE_COUNTER.get();
        if ("zh_cn".equals(Minecraft.getInstance().getLanguageManager().getSelected())) {
            this.setText(Component.literal("粒子总数: "
                    + total
                    + "\n并行: "
                    + parallel
                    + "\n实例化: "
                    + instanced
                    + "\n+" + add + "  -" + remove
                    + "\n\n异步耗时: "
                    + String.format("%.3f ms", ParallelTickManager.asyncTimeMs)
                    + "\n\n同步耗时: "
                    + String.format("%.3f ms", ParallelTickManager.syncTimeMs)
            ));
        } else {
            this.setText(Component.literal("Particle: "
                    + total
                    + "\nParalleled: "
                    + parallel
                    + "\nInstanced: "
                    + instanced
                    + "\n+" + add + "  -" + remove
                    + "\n\nAsync Tick: "
                    + String.format("%.3f ms", ParallelTickManager.asyncTimeMs)
                    + "\n\nSync Tick: "
                    + String.format("%.3f ms", ParallelTickManager.syncTimeMs)
            ));
        }
        this.setAbsBounds(4, 4, this.getPreferredSize());
    }

    /**
     * @see ParticleEngine#countParticles()
     */
    public String countParticles() {
        StringBuilder builder = new StringBuilder("\n");
        int total = 0;

        for (Map.Entry<ParticleRenderType, ParticleGroup<?>> group : Minecraft.getInstance().particleEngine.particles.entrySet()) {
            builder.append("  ").append(group.getKey().shorthand()).append(" ");
            int size = group.getValue().size();
            builder.append(size).append("\n");
            total += size;
        }

        builder.append("  Total ").append(total).append("\n");
        return builder.toString();
    }
}
