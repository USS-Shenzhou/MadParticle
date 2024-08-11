package cn.ussshenzhou.madparticle.item.component;

import cn.ussshenzhou.t88.magic.MutableDataComponent;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record TadaComponent(String command, boolean pulse, boolean used) implements MutableDataComponent<TadaComponent> {

    public TadaComponent(String command, boolean pulse, boolean used) {
        this.command = command;
        this.pulse = pulse;
        this.used = used;
    }

    public TadaComponent(String command, boolean pulse) {
        this(command, pulse, false);
    }

    public static TadaComponent defaultValue() {
        return new TadaComponent("", false, false);
    }

    public static final Codec<TadaComponent> CODEC = RecordCodecBuilder.create(ins -> ins.group(
            Codec.STRING.fieldOf("command").forGetter(o -> o.command),
            Codec.BOOL.fieldOf("pulse").forGetter(o -> o.pulse),
            Codec.BOOL.fieldOf("used").forGetter(o -> o.used)
    ).apply(ins, TadaComponent::new));

    public static final StreamCodec<ByteBuf, TadaComponent> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            TadaComponent::command,
            ByteBufCodecs.BOOL,
            TadaComponent::pulse,
            ByteBufCodecs.BOOL,
            TadaComponent::used,
            TadaComponent::new
    );

    @Override
    public DataComponentType<TadaComponent> componentType() {
        return ModDataComponent.TADA_COMPONENT.get();
    }
}
