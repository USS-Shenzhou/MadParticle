package cn.ussshenzhou.madparticle.particle;

import net.minecraft.client.particle.ParticleRenderType;

import java.util.ArrayList;

/**
 * @author USS_Shenzhou
 */
public class ParticleRenderTypeHelper {
    private static final ArrayList<ParticleRenderType> PARTICLE_RENDER_TYPES = new ArrayList<>() {{
        add(ParticleRenderType.TERRAIN_SHEET);
        add(ParticleRenderType.PARTICLE_SHEET_OPAQUE);
        add(ParticleRenderType.PARTICLE_SHEET_LIT);
        add(ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT);
        add(ParticleRenderType.CUSTOM);
    }};

    public static int toInt(ParticleRenderType p) {
        for (int i = 0; i < PARTICLE_RENDER_TYPES.size(); i++) {
            if (PARTICLE_RENDER_TYPES.get(i).toString().equals(p.toString())) {
                return i;
            }
        }
        return 3;
    }

    public static ParticleRenderType fromInt(int i) {
        try {
            return PARTICLE_RENDER_TYPES.get(i);
        } catch (IndexOutOfBoundsException e) {
            return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
        }
    }
}
