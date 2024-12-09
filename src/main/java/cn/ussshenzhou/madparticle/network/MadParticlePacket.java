package cn.ussshenzhou.madparticle.network;

import cn.ussshenzhou.madparticle.MadParticle;
import cn.ussshenzhou.madparticle.api.AddParticleHelper;
import cn.ussshenzhou.madparticle.command.IndexedCommandManager;
import cn.ussshenzhou.madparticle.designer.gui.panel.SettingPanel;
import cn.ussshenzhou.madparticle.particle.MadParticleOption;
import cn.ussshenzhou.t88.network.annotation.ClientHandler;
import cn.ussshenzhou.t88.network.annotation.Decoder;
import cn.ussshenzhou.t88.network.annotation.Encoder;
import cn.ussshenzhou.t88.network.annotation.NetPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * @author USS_Shenzhou
 */
@NetPacket(modid = MadParticle.MOD_ID)
public class MadParticlePacket {
    private final MadParticleOption particleOption;

    public MadParticlePacket(MadParticleOption particleOption) {
        this.particleOption = particleOption;
    }

    @Decoder
    public MadParticlePacket(FriendlyByteBuf buf) {
        this.particleOption = MadParticleOption.fromNetwork(buf);
    }

    @Encoder
    public void write(FriendlyByteBuf buf) {
        particleOption.writeToNetwork(buf);
    }

    @ClientHandler
    public void clientHandler(IPayloadContext context) {
        if (SettingPanel.debugNonIndexed){
            IndexedCommandManager.preform(particleOption.px(),particleOption.py(),particleOption.pz(),"mp minecraft:ash RANDOM 200 TRUE 1 ~ ~ ~ 0.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 0.0 FALSE 0 0 0 1 1 0 0 0 0 0 0 0 FALSE 0 0 INSTANCED 3 0 0.000 10 1.0 1 INDEX 10 10 LINEAR @a {}");
            return;
        }
        AddParticleHelper.addParticleClientAsync2Async(particleOption);
    }

}
