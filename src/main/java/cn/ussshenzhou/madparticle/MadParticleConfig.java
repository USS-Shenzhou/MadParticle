package cn.ussshenzhou.madparticle;

import cn.ussshenzhou.t88.config.TConfig;

/**
 * @author USS_Shenzhou
 */
public class MadParticleConfig implements TConfig {
    public int maxParticleAmountOfSingleQueue = 16384;

    public boolean limitMaxParticleGenerateDistance = true;

    public MadParticleConfig() {
    }
}
