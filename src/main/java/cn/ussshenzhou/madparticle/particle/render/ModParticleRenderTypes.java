package cn.ussshenzhou.madparticle.particle.render;

import net.minecraft.client.particle.ParticleRenderType;

import javax.annotation.ParametersAreNonnullByDefault;


/**
 * @author USS_Shenzhou
 */
@ParametersAreNonnullByDefault
public class ModParticleRenderTypes {

    public static final ParticleRenderType INSTANCED = new ParticleRenderType("INSTANCED", "MP_IN");

    public static final ParticleRenderType INSTANCED_TERRAIN = new ParticleRenderType("INSTANCED_TERRAIN", "MP_IN_TE");

    public static final ParticleRenderType INSTANCED_ITEM = new ParticleRenderType("INSTANCED_ITEM", "MP_IN_IT");
}