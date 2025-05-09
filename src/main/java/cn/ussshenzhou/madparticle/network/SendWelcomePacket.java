package cn.ussshenzhou.madparticle.network;

import cn.ussshenzhou.madparticle.MadParticle;
import cn.ussshenzhou.madparticle.MadParticleConfig;
import cn.ussshenzhou.madparticle.designer.gui.WelcomeScreen;
import cn.ussshenzhou.t88.config.ConfigHelper;
import cn.ussshenzhou.t88.network.annotation.ClientHandler;
import cn.ussshenzhou.t88.network.annotation.Decoder;
import cn.ussshenzhou.t88.network.annotation.Encoder;
import cn.ussshenzhou.t88.network.annotation.NetPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.ClientHooks;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.concurrent.CompletableFuture;

/**
 * @author USS_Shenzhou
 */
@NetPacket(modid = MadParticle.MOD_ID)
public class SendWelcomePacket {

    public SendWelcomePacket() {
    }

    @Decoder
    public SendWelcomePacket(FriendlyByteBuf buf) {
    }

    @Encoder
    public void write(FriendlyByteBuf buf) {
    }

    @ClientHandler
    @OnlyIn(Dist.CLIENT)
    public void clientHandler(IPayloadContext context) {
        if (!ConfigHelper.getConfigRead(MadParticleConfig.class).noWelcomeScreen) {
            Thread.startVirtualThread(() -> {
                while (Minecraft.getInstance().screen != null) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException ignored) {
                    }
                }
                Minecraft.getInstance().execute(() -> ClientHooks.pushGuiLayer(Minecraft.getInstance(), new WelcomeScreen()));
            });
        }
    }
}
