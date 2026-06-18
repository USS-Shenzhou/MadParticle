package cn.ussshenzhou.madparticle.designer.gui.panel;

import cn.ussshenzhou.madparticle.MultiThreadedEqualObjectLinkedOpenHashSetQueue;
import cn.ussshenzhou.madparticle.MadParticleConfig;
import cn.ussshenzhou.madparticle.designer.gui.ParticleCounterHud;
import cn.ussshenzhou.madparticle.particle.enums.LightCacheRefreshInterval;
import cn.ussshenzhou.madparticle.particle.enums.TranslucentMethod;
import cn.ussshenzhou.madparticle.particle.render.MultiThreadHelper;
import cn.ussshenzhou.madparticle.particle.enums.TakeOver;
import cn.ussshenzhou.t88.config.ConfigHelper;
import cn.ussshenzhou.t88.gui.HudManager;
import cn.ussshenzhou.t88.gui.advanced.TOptionsPanel;
import cn.ussshenzhou.t88.gui.util.HorizontalAlignment;
import cn.ussshenzhou.t88.gui.widegt.TLabel;
import com.google.common.collect.Sets;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.particle.Particle;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Queue;

/**
 * @author USS_Shenzhou
 */
public class SettingPanel extends TOptionsPanel {
    TLabel ramUsage;
    public static boolean debugNonIndexed = false;

    @SuppressWarnings({"rawtypes", "unchecked"})
    public SettingPanel() {
        var cfg = getConfigRead();
        addOptionSplitter(Component.translatable("gui.mp.de.setting.universal"));
        addOptionSliderDoubleInit(Component.translatable("gui.mp.de.setting.amount"),
                //FIXME target value < actual value
                0x2000, Math.max(1000000, cfg.maxParticleAmountOfSingleQueue),
                (component, aDouble) -> Component.literal(String.format("%d", aDouble.intValue())),
                Component.translatable("gui.mp.de.setting.amount.tooltip"),
                (s, d) -> {
                    int newAmount = (int) s.relToAbsValueLinear(d);
                    ConfigHelper.getConfigWrite(MadParticleConfig.class, madParticleConfig -> madParticleConfig.maxParticleAmountOfSingleQueue = newAmount);
                    var particleGroups = Minecraft.getInstance().particleEngine.particles;
                    particleGroups.forEach((_, particleGroup) -> {
                        MultiThreadedEqualObjectLinkedOpenHashSetQueue<Particle> newQueue = new MultiThreadedEqualObjectLinkedOpenHashSetQueue<>(newAmount);
                        newQueue.addAll(particleGroup.particles);
                        particleGroup.particles = (Queue) newQueue;
                    });
                }, cfg.maxParticleAmountOfSingleQueue, false);
        addOptionCycleButtonInit(Component.translatable("gui.mp.de.setting.real_force"),
                List.of(Boolean.TRUE, Boolean.FALSE),
                List.of(b -> ConfigHelper.getConfigWrite(MadParticleConfig.class, madParticleConfig -> madParticleConfig.limitMaxParticleGenerateDistance = true),
                        b -> ConfigHelper.getConfigWrite(MadParticleConfig.class, madParticleConfig -> madParticleConfig.limitMaxParticleGenerateDistance = false)),
                e -> e.getContent() == cfg.limitMaxParticleGenerateDistance
        )
                .getSecond().setTooltip(Tooltip.create(Component.translatable("gui.mp.de.setting.real_force.tooltip")));
        int amount = cfg.getBufferFillerThreads();
        addOptionCycleButtonInit(Component.translatable("gui.mp.de.setting.threads"),
                //FIXME
                Sets.newLinkedHashSet(List.of("gui.mp.de.setting.threads.zero", 2, 4, 6, 8, 12, 16, amount == 1 ? 6 : amount)).stream().toList(),
                i -> b -> {
                    var c = b.getSelected().getContent();
                    int a = c instanceof String ? 1 : (Integer) c;
                    ConfigHelper.getConfigWrite(MadParticleConfig.class, madParticleConfig -> madParticleConfig.setBufferFillerThreads(a));
                    MultiThreadHelper.update(a);
                }, entry -> entry.getContent() instanceof String ? amount == 1 : amount == (Integer) entry.getContent()
        )
                .getSecond().setTooltip(Tooltip.create(Component.translatable("gui.mp.de.setting.threads.tooltip")));
        addOptionCycleButtonInit(Component.translatable("gui.mp.de.setting.universal.translucent"),
                List.of(TranslucentMethod.values()),
                method -> b -> {
                    ConfigHelper.getConfigWrite(MadParticleConfig.class, madParticleConfig -> madParticleConfig.translucentMethod = method);
                    b.setTooltip(Tooltip.create(Component.translatable(method.translateKey() + ".tooltip")));
                }, entry -> entry.getContent() == cfg.translucentMethod
        ).getSecond().setTooltip(Tooltip.create(Component.translatable(cfg.translucentMethod.translateKey() + ".tooltip")));


        addOptionSplitter(Component.translatable("gui.mp.de.setting.additional"));
        addOptionCycleButtonInit(Component.translatable("gui.mp.de.setting.additional.takeover_render"),
                List.of(TakeOver.values()), takeOver -> b -> ConfigHelper.getConfigWrite(MadParticleConfig.class, madParticleConfig -> madParticleConfig.takeOverRendering = b.getSelected().getContent()),
                entry -> entry.getContent() == cfg.takeOverRendering
        )
                .getSecond().setTooltip(Tooltip.create(Component.translatable("gui.mp.de.setting.additional.takeover_render.tooltip")));
        addOptionCycleButtonInit(Component.translatable("gui.mp.de.setting.additional.takeover_tick"),
                List.of(TakeOver.values()), takeOver -> b -> ConfigHelper.getConfigWrite(MadParticleConfig.class, madParticleConfig -> madParticleConfig.takeOverTicking = b.getSelected().getContent()),
                entry -> entry.getContent() == cfg.takeOverTicking
        )
                .getSecond().setTooltip(Tooltip.create(Component.translatable("gui.mp.de.setting.additional.takeover_tick.tooltip")));
        addOptionCycleButtonInit(Component.translatable("gui.mp.de.setting.additional.optimize_command_block"),
                List.of(Boolean.TRUE, Boolean.FALSE),
                bool -> b -> ConfigHelper.getConfigWrite(MadParticleConfig.class, madParticleConfig -> madParticleConfig.optimizeCommandBlockEditScreen = bool),
                entry -> entry.getContent() == cfg.optimizeCommandBlockEditScreen
        );
        addOptionCycleButton(Component.translatable("gui.mp.de.setting.additional.counter"),
                List.of(Boolean.FALSE, Boolean.TRUE),
                bool -> b -> {
                    if (b.getSelected().getContent()) {
                        HudManager.addIfSameClassNotExist(new ParticleCounterHud());
                    } else {
                        HudManager.getChildren().stream().filter(t -> t instanceof ParticleCounterHud).findFirst().ifPresent(HudManager::remove);
                    }
                });


        addOptionSplitter(Component.translatable("gui.mp.de.setting.light"));
        addOptionCycleButtonInit(Component.translatable("gui.mp.de.setting.light.hor"),
                Sets.newLinkedHashSet(List.of(16, 64, 128, 256, 512, cfg.lightCacheXZRange)).stream().toList(),
                integer -> i -> ConfigHelper.getConfigWrite(MadParticleConfig.class, madParticleConfig -> madParticleConfig.lightCacheXZRange = i.getSelected().getContent()),
                entry -> entry.getContent() == cfg.lightCacheXZRange).getSecond()
                .setTooltip(Tooltip.create(Component.translatable("gui.mp.de.setting.light.hor.tooltip")));
        addOptionCycleButtonInit(Component.translatable("gui.mp.de.setting.light.ver"),
                Sets.newLinkedHashSet(List.of(16, 64, 128, 256, 512, cfg.lightCacheYRange)).stream().toList(),
                integer -> i -> ConfigHelper.getConfigWrite(MadParticleConfig.class, madParticleConfig -> madParticleConfig.lightCacheYRange = i.getSelected().getContent()),
                entry -> entry.getContent() == cfg.lightCacheYRange).getSecond()
                .setTooltip(Tooltip.create(Component.translatable("gui.mp.de.setting.light.ver.tooltip")));
        ramUsage = addOption(Component.translatable("gui.mp.de.setting.light.ram"), new TLabel()).getSecond()
                .setHorizontalAlignment(HorizontalAlignment.LEFT);
        addOptionCycleButtonInit(Component.translatable("gui.mp.de.setting.light.force"),
                List.of(Boolean.TRUE, Boolean.FALSE),
                bool -> b -> ConfigHelper.getConfigWrite(MadParticleConfig.class, madParticleConfig -> madParticleConfig.forceMaxLight = bool),
                entry -> entry.getContent() == cfg.forceMaxLight).getSecond()
                .setTooltip(Tooltip.create(Component.translatable("gui.mp.de.setting.light.force.tooltip")));
        addOptionCycleButtonInit(Component.translatable("gui.mp.de.setting.light.update"),
                List.of(LightCacheRefreshInterval.values()),
                interval -> b -> ConfigHelper.getConfigWrite(MadParticleConfig.class, madParticleConfig -> madParticleConfig.lightCacheRefreshInterval = interval),
                entry -> entry.getContent() == cfg.lightCacheRefreshInterval
        )
                .getSecond().setTooltip(Tooltip.create(Component.translatable("gui.mp.de.setting.light.update.tooltip")));

        addOptionSplitter(Component.translatable("gui.mp.de.setting.debug"));
        addOptionCycleButtonInit(Component.translatable("gui.mp.de.setting.debug.non_indexed"),
                List.of(Boolean.TRUE, Boolean.FALSE),
                b -> button -> debugNonIndexed = button.getSelected().getContent(),
                entry -> entry.getContent() == debugNonIndexed);

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
