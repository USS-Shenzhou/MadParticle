package cn.ussshenzhou.madparticle.network;

import cn.ussshenzhou.madparticle.MadParticle;
import cn.ussshenzhou.madparticle.particle.MadParticleOption;
import cn.ussshenzhou.madparticle.particle.ModParticleTypeRegistry;
import cn.ussshenzhou.madparticle.util.MathHelper;
import cn.ussshenzhou.t88.network.annotation.ClientHandler;
import cn.ussshenzhou.t88.network.annotation.Decoder;
import cn.ussshenzhou.t88.network.annotation.Encoder;
import cn.ussshenzhou.t88.network.annotation.NetPacket;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.Random;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * @author USS_Shenzhou
 */
@NetPacket(modid = MadParticle.MOD_ID)
public class MadParticleTadaPacket {
    private final MadParticleOption particleOption;
    private final UUID sourcePlayerUUID;

    public MadParticleTadaPacket(MadParticleOption particleOption, UUID sourcePlayerUUID) {
        this.particleOption = particleOption;
        this.sourcePlayerUUID = sourcePlayerUUID;
    }

    @Decoder
    public MadParticleTadaPacket(FriendlyByteBuf buf) {
        this.particleOption = MadParticleOption.fromNetwork(buf);
        this.sourcePlayerUUID = buf.readUUID();
    }

    @Encoder
    public void write(FriendlyByteBuf buf) {
        particleOption.writeToNetwork(buf);
        buf.writeUUID(sourcePlayerUUID);
    }

    @ClientHandler
    @OnlyIn(Dist.CLIENT)
    public void clientHandler(IPayloadContext context) {
        Level level = Minecraft.getInstance().level;
        if (level != null) {
            Random r = new Random();
            var player = Minecraft.getInstance().level.getPlayerByUUID(sourcePlayerUUID);
            if (player == null) {
                return;
            }
            var posOffset = getNozzlePosInWorld(player, 4, 0);
            var speed = getSpeedVec(player);
            for (int i = 0; i < particleOption.amount(); i++) {
                level.addParticle(
                        particleOption,
                        true,
                        particleOption.alwaysRender().get(),
                        posOffset.x + MathHelper.signedRandom(r) * particleOption.xDiffuse(),
                        posOffset.y + MathHelper.signedRandom(r) * particleOption.yDiffuse(),
                        posOffset.z + MathHelper.signedRandom(r) * particleOption.zDiffuse(),
                        speed.x + MathHelper.signedRandom(r) * particleOption.vxDiffuse(),
                        speed.y + MathHelper.signedRandom(r) * particleOption.vyDiffuse(),
                        speed.z + MathHelper.signedRandom(r) * particleOption.vzDiffuse()
                );
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    Vec3 getSpeedVec(LivingEntity player) {
        Vec3 speedVec = player.getLookAngle();
        double speed;
        if (particleOption.vx() == particleOption.vy() && particleOption.vy() == particleOption.vz()) {
            speed = particleOption.vx();
        } else {
            speed = Math.sqrt(particleOption.vx() * particleOption.vx() + particleOption.vy() * particleOption.vy() + particleOption.vz() * particleOption.vz());
        }
        return speedVec.multiply(speed, speed, speed);
    }

    @OnlyIn(Dist.CLIENT)
    Vec3 getNozzlePosInWorld(LivingEntity player, float tubeLengthIn16, float leftOrRightOffsetIn16) {
        int manualFixY = 1;
        //TODO leftHand and offHand
        //get shoulder position in world-coordinates.
        var livingEntityRenderer = (LivingEntityRenderer<?, ?, ?>) Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(player);
        HumanoidModel<?> playerModel = ((PlayerRenderer) livingEntityRenderer).getModel();
        float playerYRot = (float) (Math.PI * player.yBodyRot / 180);
        Vec3 vec3 = new Vec3(
                Math.cos(playerYRot) * (playerModel.rightArm.x - 1 + leftOrRightOffsetIn16),
                -playerModel.rightArm.y + 22 + manualFixY,
                Math.sin(playerYRot) * (playerModel.rightArm.x - 1 + leftOrRightOffsetIn16)
        );
        vec3 = vec3.multiply(1 / 16f, 1 / 16f, 1 / 16f);
        Vec3 playerPos = new Vec3(player.getX(), player.getY(), player.getZ());
        vec3 = playerPos.add(vec3);
        //add arm and item-in-hand to shoulder.
        float armLength = 10 / 16f;
        float xRot = playerModel.rightArm.xRot;
        //zRot of arm is not necessary to consider.
        tubeLengthIn16 /= 16;
        Vec3 armRot = new Vec3(
                -(armLength + tubeLengthIn16) * (Math.cos(xRot) * Math.sin(playerYRot)),

                (armLength + tubeLengthIn16) * Math.sin(xRot),
                (armLength + tubeLengthIn16) * (Math.cos(xRot) * Math.cos(playerYRot))
        );
        //first-person angle optimize
        if (Minecraft.getInstance().player == player && Minecraft.getInstance().options.getCameraType() == CameraType.FIRST_PERSON) {
            return vec3.add(player.getLookAngle().multiply(1.5, 1.5, 1.5));
        }
        return vec3.add(armRot);
    }

}
