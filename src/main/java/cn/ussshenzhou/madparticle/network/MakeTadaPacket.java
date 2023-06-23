package cn.ussshenzhou.madparticle.network;

import cn.ussshenzhou.madparticle.MadParticle;
import cn.ussshenzhou.madparticle.item.ModItemsRegistry;
import cn.ussshenzhou.madparticle.item.Tada;
import cn.ussshenzhou.madparticle.particle.MadParticleOption;
import cn.ussshenzhou.madparticle.particle.ModParticleRegistry;
import cn.ussshenzhou.madparticle.util.MathHelper;
import cn.ussshenzhou.t88.network.annotation.Consumer;
import cn.ussshenzhou.t88.network.annotation.Decoder;
import cn.ussshenzhou.t88.network.annotation.Encoder;
import cn.ussshenzhou.t88.network.annotation.NetPacket;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.Random;
import java.util.function.Supplier;

/**
 * @author USS_Shenzhou
 */
@NetPacket(modid = MadParticle.MOD_ID)
public class MakeTadaPacket {
    private String command;

    public MakeTadaPacket(String command) {
        this.command = command;
    }

    @Decoder
    public MakeTadaPacket(FriendlyByteBuf buf) {
        command = buf.readUtf();
    }

    @Encoder
    public void write(FriendlyByteBuf buf) {
        buf.writeUtf(command);
    }


    @Consumer
    public void handler(Supplier<NetworkEvent.Context> context) {
        if (context.get().getDirection().equals(NetworkDirection.PLAY_TO_SERVER)) {
            serverHandler(context);
        } else {
        }
    }

    private void serverHandler(Supplier<NetworkEvent.Context> context) {
        var sender = context.get().getSender();
        if (sender == null) {
            return;
        }
        if (command.startsWith("/")) {
            command = command.replaceFirst("/", "");
        }
        if (command.startsWith("mp ") || command.startsWith("madparticle")) {
            if (sender.hasPermissions(2)) {
                makeTada(sender);
            } else {
                LogUtils.getLogger().warn("Player {} wants to make a madparticle:tada, but hasn't enough permission level.", sender.getName());
                return;
            }
        } else if (command.startsWith("mp_demo")) {
            makeTada(sender);
        } else {
            LogUtils.getLogger().warn("Player {} wants to make a madparticle:tada, but sent an illegal command text.", sender.getName());
            return;
        }
    }

    private void makeTada(ServerPlayer player) {
        ItemStack tada = new ItemStack(ModItemsRegistry.TADA.get());
        tada.getOrCreateTag().putString(Tada.TAG_COMMAND, command);
        player.getInventory().add(tada);
    }
}
