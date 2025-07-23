package cn.ussshenzhou.madparticle.particle;

import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.particle.ParticleRenderType;

import javax.annotation.ParametersAreNonnullByDefault;


/**
 * @author USS_Shenzhou
 */
@ParametersAreNonnullByDefault
public class ModParticleRenderTypes {

    public static final ParticleRenderType INSTANCED = new ParticleRenderType("INSTANCED", null);

    public static final ParticleRenderType INSTANCED_TERRAIN = new ParticleRenderType("INSTANCED_TERRAIN", null);
}