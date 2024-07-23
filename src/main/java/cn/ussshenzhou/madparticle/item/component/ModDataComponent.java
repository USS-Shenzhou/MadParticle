package cn.ussshenzhou.madparticle.item.component;

import cn.ussshenzhou.madparticle.MadParticle;
import net.minecraft.core.component.DataComponentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * @author USS_Shenzhou
 */
public class ModDataComponent {
    public static final DeferredRegister.DataComponents DATA_COMPONENTS = DeferredRegister.createDataComponents(MadParticle.MOD_ID);

    public static final DeferredHolder<DataComponentType<?>,DataComponentType<TadaComponent>> TADA_COMPONENT = DATA_COMPONENTS.registerComponentType("tada",
            builder -> builder
                    .persistent(TadaComponent.CODEC)
                    .networkSynchronized(TadaComponent.STREAM_CODEC)
    );
}
