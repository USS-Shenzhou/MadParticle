package cn.ussshenzhou.madparticle.designer.gui.panel;

import cn.ussshenzhou.madparticle.EvictingLinkedHashSetQueue;
import cn.ussshenzhou.madparticle.MadParticleConfig;
import cn.ussshenzhou.madparticle.designer.gui.ParticleCounterHud;
import cn.ussshenzhou.madparticle.mixin.ParticleEngineAccessor;
import cn.ussshenzhou.madparticle.particle.InstancedRenderManager;
import cn.ussshenzhou.madparticle.particle.ModParticleRenderTypes;
import cn.ussshenzhou.madparticle.particle.ParallelTickManager;
import cn.ussshenzhou.madparticle.particle.TakeOver;
import cn.ussshenzhou.t88.config.ConfigHelper;
import cn.ussshenzhou.t88.gui.HudManager;
import cn.ussshenzhou.t88.gui.advanced.TOptionsPanel;
import cn.ussshenzhou.t88.gui.util.HorizontalAlignment;
import cn.ussshenzhou.t88.gui.widegt.TCycleButton;
import cn.ussshenzhou.t88.gui.widegt.TLabel;
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
    TLabel ramUsage;

    public SettingPanel() {
        addOptionSplitter(Component.translatable("gui.mp.de.setting.universal"));
        addOptionSliderDoubleInit(Component.translatable("gui.mp.de.setting.amount"),
                0x2000, Math.max(1000000, getConfigRead().maxParticleAmountOfSingleQueue),
                (component, aDouble) -> Component.literal(String.format("%d", aDouble.intValue())),
                Component.translatable("gui.mp.de.setting.amount.tooltip"),
                (s, d) -> {
                    int newAmount = (int) s.relToAbsValueLinear(d);
                    ConfigHelper.getConfigWrite(MadParticleConfig.class, madParticleConfig -> madParticleConfig.maxParticleAmountOfSingleQueue = newAmount);
                    var particles = ((ParticleEngineAccessor) (Minecraft.getInstance().particleEngine)).getParticles();
                    particles.forEach((particleRenderType, p) -> {
                        EvictingLinkedHashSetQueue<Particle> newQueue = new EvictingLinkedHashSetQueue<>(newAmount);
                        newQueue.addAll(p);
                        particles.put(particleRenderType, newQueue);
                        if (particleRenderType == ModParticleRenderTypes.INSTANCED) {
                            InstancedRenderManager.reload(newQueue);
                        }
                    });
                }, getConfigRead().maxParticleAmountOfSingleQueue);
        addOptionCycleButtonInit(Component.translatable("gui.mp.de.setting.real_force"),
                List.of("gui.mp.de.helper.true", "gui.mp.de.helper.false"),
                List.of(b -> ConfigHelper.getConfigWrite(MadParticleConfig.class, madParticleConfig -> madParticleConfig.limitMaxParticleGenerateDistance = true),
                        b -> ConfigHelper.getConfigWrite(MadParticleConfig.class, madParticleConfig -> madParticleConfig.limitMaxParticleGenerateDistance = false)),
                e -> e.getContent().contains(String.valueOf(getConfigRead().limitMaxParticleGenerateDistance))
        )
                .getB().setTooltip(Tooltip.create(Component.translatable("gui.mp.de.setting.real_force.tooltip")));
        int amount = getConfigRead().bufferFillerThreads;
        addOptionCycleButtonInit(Component.translatable("gui.mp.de.setting.threads"),
                Sets.newLinkedHashSet(List.of("gui.mp.de.setting.threads.zero", 6, 4, 8, 2, 12, amount == 1 ? 6 : amount)).stream().toList(),
                i -> b -> {
                    var c = b.getSelected().getContent();
                    int a = c instanceof String ? 1 : (Integer) c;
                    ConfigHelper.getConfigWrite(MadParticleConfig.class, madParticleConfig -> madParticleConfig.bufferFillerThreads = a);
                    InstancedRenderManager.setThreads(a);
                    ParallelTickManager.setThreads(a);
                }, entry -> entry.getContent() instanceof String ? amount == 1 : amount == (Integer) entry.getContent()
        )
                .getB().setTooltip(Tooltip.create(Component.translatable("gui.mp.de.setting.threads.tooltip")));
        addOptionSplitter(Component.translatable("gui.mp.de.setting.additional"));
        addOptionCycleButtonInit(Component.translatable("gui.mp.de.setting.additional.takeover_render"),
                List.of(TakeOver.values()), takeOver -> b -> ConfigHelper.getConfigWrite(MadParticleConfig.class, madParticleConfig -> madParticleConfig.takeOverRendering = b.getSelected().getContent()),
                entry -> entry.getContent() == getConfigRead().takeOverRendering
        )
                .getB().setTooltip(Tooltip.create(Component.translatable("gui.mp.de.setting.additional.takeover_render.tooltip")));
        addOptionCycleButtonInit(Component.translatable("gui.mp.de.setting.additional.takeover_tick"),
                List.of(TakeOver.values()), takeOver -> b -> ConfigHelper.getConfigWrite(MadParticleConfig.class, madParticleConfig -> madParticleConfig.takeOverTicking = b.getSelected().getContent()),
                entry -> entry.getContent() == getConfigRead().takeOverTicking
        )
                .getB().setTooltip(Tooltip.create(Component.translatable("gui.mp.de.setting.additional.takeover_tick.tooltip")));
        addOptionCycleButtonInit(Component.translatable("gui.mp.de.setting.additional.optimize_command_block"),
                List.of(Boolean.TRUE, Boolean.FALSE),
                bool -> b -> ConfigHelper.getConfigWrite(MadParticleConfig.class, madParticleConfig -> madParticleConfig.optimizeCommandBlockEditScreen = bool),
                entry -> entry.getContent() == getConfigRead().optimizeCommandBlockEditScreen
        );
        addOptionCycleButton(Component.translatable("gui.mp.de.setting.additional.counter"),
                List.of(Boolean.FALSE, Boolean.TRUE),
                bool -> b -> {
                    if (b.getSelected().getContent()) {
                        //TODO T88 0.7
                        HudManager.add(new ParticleCounterHud());
                    } else {
                        HudManager.getChildren().stream().filter(t -> t instanceof ParticleCounterHud).findFirst().ifPresent(HudManager::remove);
                    }
                });
        addOptionSplitter(Component.translatable("gui.mp.de.setting.light"));
        addOptionCycleButtonInit(Component.translatable("gui.mp.de.setting.light.hor"),
                Sets.newLinkedHashSet(List.of(16, 64, 128, 256, 512, getConfigRead().lightCacheXZRange)).stream().toList(),
                integer -> i -> ConfigHelper.getConfigWrite(MadParticleConfig.class, madParticleConfig -> madParticleConfig.lightCacheXZRange = i.getSelected().getContent()),
                entry -> entry.getContent() == getConfigRead().lightCacheXZRange).getB()
                .setTooltip(Tooltip.create(Component.translatable("gui.mp.de.setting.light.hor.tooltip")));
        addOptionCycleButtonInit(Component.translatable("gui.mp.de.setting.light.ver"),
                Sets.newLinkedHashSet(List.of(16, 64, 128, 256, 512, getConfigRead().lightCacheYRange)).stream().toList(),
                integer -> i -> ConfigHelper.getConfigWrite(MadParticleConfig.class, madParticleConfig -> madParticleConfig.lightCacheYRange = i.getSelected().getContent()),
                entry -> entry.getContent() == getConfigRead().lightCacheYRange).getB()
                .setTooltip(Tooltip.create(Component.translatable("gui.mp.de.setting.light.ver.tooltip")));
        ramUsage = addOption(Component.translatable("gui.mo.de.setting.light.ram"), new TLabel()).getB()
                .setHorizontalAlignment(HorizontalAlignment.LEFT);
        addOptionCycleButtonInit(Component.translatable("gui.mo.de.setting.light.force"),
                List.of(Boolean.TRUE, Boolean.FALSE),
                bool -> b -> ConfigHelper.getConfigWrite(MadParticleConfig.class, madParticleConfig -> madParticleConfig.forceMaxLight = bool),
                entry -> entry.getContent() == getConfigRead().forceMaxLight).getB()
                .setTooltip(Tooltip.create(Component.translatable("gui.mo.de.setting.light.force.tooltip")));
    }

    @Override
    public void tickT() {
        long xz = getConfigRead().lightCacheXZRange;
        long y = getConfigRead().lightCacheYRange;
        long bytes = xz * 2 * xz * 2 * y * 2 + xz * xz * y;
        ramUsage.setText(Component.literal(convertBytes(bytes)));
        super.tickT();
    }

    public static String convertBytes(long bytes) {
        double k = 1024;
        double m = k * 1024;
        double g = m * 1024;

        if (bytes < k) {
            return bytes + " Byte";
        } else if (bytes < m) {
            return String.format("%.2f", bytes / k) + " KiB";
        } else if (bytes < g) {
            return String.format("%.2f", bytes / m) + " MiB";
        } else {
            return String.format("%.2f", bytes / g) + " GiB";
        }
    }

    private static MadParticleConfig getConfigRead() {
        return ConfigHelper.getConfigRead(MadParticleConfig.class);
    }
}
