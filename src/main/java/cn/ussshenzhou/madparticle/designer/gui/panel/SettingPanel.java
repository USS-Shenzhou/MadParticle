package cn.ussshenzhou.madparticle.designer.gui.panel;

import cn.ussshenzhou.madparticle.MadParticleConfig;
import cn.ussshenzhou.madparticle.mixin.ParticleEngineAccessor;
import cn.ussshenzhou.madparticle.particle.InstancedRenderManager;
import cn.ussshenzhou.madparticle.particle.ModParticleRenderTypes;
import cn.ussshenzhou.madparticle.particle.TakeOver;
import cn.ussshenzhou.t88.config.ConfigHelper;
import cn.ussshenzhou.t88.gui.advanced.TOptionsPanel;
import com.google.common.collect.EvictingQueue;
import com.google.common.collect.Sets;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.particle.Particle;
import net.minecraft.network.chat.Component;

import java.util.List;

/**
 * @author USS_Shenzhou
 */
public class SettingPanel extends TOptionsPanel {

    @SuppressWarnings("UnstableApiUsage")
    public SettingPanel() {
        addOptionSplitter(Component.translatable("gui.mp.de.setting.universal"));
        addOptionSliderDoubleInit(Component.translatable("gui.mp.de.setting.amount"),
                0x2000, 0xf0000,
                (component, aDouble) -> Component.literal(String.format("%d", aDouble.intValue())),
                Component.translatable("gui.mp.de.setting.amount.tooltip"),
                (s, d) -> {
                    int newAmount = (int) s.relToAbsValueLinear(d);
                    ConfigHelper.getConfigWrite(MadParticleConfig.class, madParticleConfig -> madParticleConfig.maxParticleAmountOfSingleQueue = newAmount);
                    var particles = ((ParticleEngineAccessor) (Minecraft.getInstance().particleEngine)).getParticles();
                    particles.forEach((particleRenderType, p) -> {
                        EvictingQueue<Particle> newQueue = EvictingQueue.create(newAmount);
                        newQueue.addAll(p);
                        particles.put(particleRenderType, newQueue);
                        if (particleRenderType == ModParticleRenderTypes.INSTANCED) {
                            InstancedRenderManager.reload(newQueue);
                        }
                    });
                }, ConfigHelper.getConfigRead(MadParticleConfig.class).maxParticleAmountOfSingleQueue);
        addOptionCycleButtonInit(Component.translatable("gui.mp.de.setting.real_force"),
                List.of("gui.mp.de.helper.true", "gui.mp.de.helper.false"),
                List.of(b -> ConfigHelper.getConfigWrite(MadParticleConfig.class, madParticleConfig -> madParticleConfig.limitMaxParticleGenerateDistance = true),
                        b -> ConfigHelper.getConfigWrite(MadParticleConfig.class, madParticleConfig -> madParticleConfig.limitMaxParticleGenerateDistance = false)),
                e -> e.getContent().contains(String.valueOf(ConfigHelper.getConfigRead(MadParticleConfig.class).limitMaxParticleGenerateDistance))
        )
                .getB().setTooltip(Tooltip.create(Component.translatable("gui.mp.de.setting.real_force.tooltip")));
        int amount = ConfigHelper.getConfigRead(MadParticleConfig.class).bufferFillerThreads;
        addOptionCycleButtonInit(Component.translatable("gui.mp.de.setting.threads"),
                Sets.newLinkedHashSet(List.of("gui.mp.de.setting.threads.zero", 6, 4, 8, 2, 12, amount == 1 ? 6 : amount)).stream().toList(),
                i -> b -> {
                    var c = b.getSelected().getContent();
                    int a = c instanceof String ? 1 : (Integer) c;
                    ConfigHelper.getConfigWrite(MadParticleConfig.class, madParticleConfig -> madParticleConfig.bufferFillerThreads = a);
                    InstancedRenderManager.setThreads(a);
                }, entry -> entry.getContent() instanceof String ? amount == 1 : amount == (Integer) entry.getContent()
        )
                .getB().setTooltip(Tooltip.create(Component.translatable("gui.mp.de.setting.threads.tooltip")));
        addOptionSplitter(Component.translatable("gui.mp.de.setting.additional"));
        addOptionCycleButtonInit(Component.translatable("gui.mp.de.setting.additional.takeover_render"),
                List.of(TakeOver.values()), takeOver -> b -> {
                    ConfigHelper.getConfigWrite(MadParticleConfig.class, madParticleConfig -> madParticleConfig.takeOverRendering = b.getSelected().getContent());
                    //TODO
                },
                entry -> entry.getContent() == ConfigHelper.getConfigRead(MadParticleConfig.class).takeOverRendering
        )
                .getB().setTooltip(Tooltip.create(Component.translatable("gui.mp.de.setting.additional.takeover_render.tooltip")));
        addOptionCycleButtonInit(Component.translatable("gui.mp.de.setting.additional.takeover_tick"),
                List.of(TakeOver.values()), takeOver -> b -> {
                    ConfigHelper.getConfigWrite(MadParticleConfig.class, madParticleConfig -> madParticleConfig.takeOverTicking = b.getSelected().getContent());
                    //TODO
                },
                entry -> entry.getContent() == ConfigHelper.getConfigRead(MadParticleConfig.class).takeOverTicking
        )
                .getB().setTooltip(Tooltip.create(Component.translatable("gui.mp.de.setting.additional.takeover_tick.tooltip")));
    }
}
