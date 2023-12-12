package cn.ussshenzhou.madparticle.designer.gui.panel;

import cn.ussshenzhou.madparticle.MadParticleConfig;
import cn.ussshenzhou.madparticle.mixin.ParticleEngineAccessor;
import cn.ussshenzhou.madparticle.particle.InstancedRenderManager;
import cn.ussshenzhou.madparticle.particle.ModParticleRenderTypes;
import cn.ussshenzhou.t88.config.ConfigHelper;
import cn.ussshenzhou.t88.gui.util.HorizontalAlignment;
import cn.ussshenzhou.t88.gui.util.LayoutHelper;
import cn.ussshenzhou.t88.gui.widegt.*;
import com.google.common.collect.EvictingQueue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.network.chat.Component;
import org.joml.Vector2i;

import java.util.List;

/**
 * @author USS_Shenzhou
 */
public class SettingPanel extends TPanel {
    private final OptionContainer container;
    public static final int STD_GAP = 4;

    @SuppressWarnings("UnstableApiUsage")
    public SettingPanel() {
        this.container = new OptionContainer();
        this.add(container);
        initAmountSlider();
        initRealForce();
        initThreads();
    }

    @SuppressWarnings("UnstableApiUsage")
    private void initAmountSlider() {
        var amount = new HorizontalTitledOption<>(
                Component.translatable("gui.mp.de.setting.amount"),
                new TSlider("", 0x2000, 0xf0000, (component, aDouble) -> Component.literal(String.format("%d", aDouble.intValue())), null)
        );
        amount.actioner.addResponder(d -> {
            int newAmount = (int) amount.actioner.relToAbsValueLinear(d);
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
        });
        amount.actioner.setAbsValue(ConfigHelper.getConfigRead(MadParticleConfig.class).maxParticleAmountOfSingleQueue);
        container.add(amount);
    }

    private void initRealForce() {
        var realForce = new HorizontalTitledOption<>(
                Component.translatable("gui.mp.de.setting.real_force"),
                new TCycleButton<>()
        );
        realForce.actioner.addElement("gui.mp.de.helper.true", b -> {
            ConfigHelper.getConfigWrite(MadParticleConfig.class, madParticleConfig -> madParticleConfig.limitMaxParticleGenerateDistance = true);
        });
        realForce.actioner.addElement("gui.mp.de.helper.false", b -> {
            ConfigHelper.getConfigWrite(MadParticleConfig.class, madParticleConfig -> madParticleConfig.limitMaxParticleGenerateDistance = false);
        });
        realForce.actioner.select(ConfigHelper.getConfigRead(MadParticleConfig.class).limitMaxParticleGenerateDistance ? 0 : 1);
        container.add(realForce);
    }

    private void initThreads() {
        var threads = new HorizontalTitledOption<>(
                Component.translatable("gui.mp.de.setting.threads"),
                new TCycleButton<>()
        );
        threads.actioner.addElement("gui.mp.de.setting.threads.zero", b -> {
            ConfigHelper.getConfigWrite(MadParticleConfig.class, madParticleConfig -> madParticleConfig.bufferFillerThreads = 1);
            InstancedRenderManager.setThreads(1);
        });
        var list = List.of(6, 4, 8, 2, 12);
        list.forEach(i -> addThreadsNumberElement(threads.actioner, i));
        int amount = ConfigHelper.getConfigRead(MadParticleConfig.class).bufferFillerThreads;
        if (!list.contains(amount)) {
            addThreadsNumberElement(threads.actioner, amount);
            threads.actioner.select(list.size());
        } else {
            threads.actioner.select(list.indexOf(amount));
        }
        container.add(threads);
    }

    @SuppressWarnings("unchecked")
    private void addThreadsNumberElement(TCycleButton actioner, int number) {
        actioner.addElement(String.valueOf(number), b -> {
            ConfigHelper.getConfigWrite(MadParticleConfig.class, madParticleConfig -> madParticleConfig.bufferFillerThreads = number);
            InstancedRenderManager.setThreads(number);
        });
    }

    @Override
    public void layout() {
        container.setBounds(10, 10, width - 20, height - 20);
        super.layout();
    }

    public static class OptionContainer extends TScrollPanel {
        public OptionContainer() {
        }

        @Override
        public void layout() {
            int i = 0;
            for (TWidget tWidget : this.children) {
                if (i == 0) {
                    tWidget.setBounds(STD_GAP, STD_GAP, getUsableWidth() - 2 * STD_GAP, tWidget.getPreferredSize().y);
                } else {
                    LayoutHelper.BBottomOfA(tWidget, STD_GAP, this.children.get(i - 1));
                }
                i++;
            }
            super.layout();
        }
    }

    public static class HorizontalTitledOption<W extends TWidget> extends TPanel {
        private final TLabel title;
        private final W actioner;

        public HorizontalTitledOption(Component title, W actioner) {
            super();
            this.title = new TLabel(title);
            this.add(this.title);
            this.actioner = actioner;
            this.add(actioner);

            this.title.setHorizontalAlignment(HorizontalAlignment.RIGHT);
        }

        @Override
        public void layout() {
            title.setBounds(0, 0, width / 2 - STD_GAP, height);
            actioner.setBounds(width / 2 + STD_GAP, 0, width / 2 - STD_GAP * 2, height);
            super.layout();
        }

        @Override
        public Vector2i getPreferredSize() {
            int actionY = actioner.getPreferredSize().y;
            return new Vector2i(this.width, Math.max(actionY, 20));
        }

        public TLabel getTitle() {
            return title;
        }

        public W getActioner() {
            return actioner;
        }
    }
}
