package cn.ussshenzhou.madparticle.network;

import cn.ussshenzhou.madparticle.MadParticle;
import cn.ussshenzhou.madparticle.item.ModItemsRegistry;
import cn.ussshenzhou.madparticle.item.Tada;
import cn.ussshenzhou.madparticle.item.component.ModDataComponent;
import cn.ussshenzhou.madparticle.item.component.TadaComponent;
import cn.ussshenzhou.t88.network.annotation.Decoder;
import cn.ussshenzhou.t88.network.annotation.Encoder;
import cn.ussshenzhou.t88.network.annotation.NetPacket;
import cn.ussshenzhou.t88.network.annotation.ServerHandler;
import com.mojang.logging.LogUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

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

    @ServerHandler
    public void serverHandler(IPayloadContext context) {
        var sender = context.player();
        if (command.startsWith("/")) {
            command = command.replaceFirst("/", "");
        }
        if (command.startsWith("mp ") || command.startsWith("madparticle")) {
            if (sender.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER)) {
                makeTada((ServerPlayer) sender);
            } else {
                LogUtils.getLogger().warn("Player {} wants to make a madparticle:tada, but hasn't enough permission level.", sender.getName());
                return;
            }
        } else {
            LogUtils.getLogger().warn("Player {} wants to make a madparticle:tada, but sent an illegal command text.", sender.getName());
            return;
        }
    }

    private void makeTada(ServerPlayer player) {
        ItemStack tada = new ItemStack(ModItemsRegistry.TADA.get());
        tada.set(ModDataComponent.TADA_COMPONENT,new TadaComponent(command,command.contains("\"pulse\":1") || command.contains("\"pulse\":true")));
        player.getInventory().add(tada);
    }
}
