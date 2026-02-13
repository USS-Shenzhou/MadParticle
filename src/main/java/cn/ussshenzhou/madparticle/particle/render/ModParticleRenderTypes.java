package cn.ussshenzhou.madparticle.particle.render;

import net.minecraft.client.particle.ParticleRenderType;

import javax.annotation.ParametersAreNonnullByDefault;


/**
 * @author USS_Shenzhou
 */
@ParametersAreNonnullByDefault
public class ModParticleRenderTypes {

    public static final ParticleRenderType INSTANCED = new ParticleRenderType("INSTANCED");

    public static final ParticleRenderType INSTANCED_TERRAIN = new ParticleRenderType("INSTANCED_TERRAIN");
}