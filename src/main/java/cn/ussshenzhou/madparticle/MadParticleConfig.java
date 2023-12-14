package cn.ussshenzhou.madparticle;

import cn.ussshenzhou.madparticle.particle.TakeOver;
import cn.ussshenzhou.t88.config.TConfig;

/**
 * @author USS_Shenzhou
 */
public class MadParticleConfig implements TConfig {
    public int maxParticleAmountOfSingleQueue = 32768;
    public boolean limitMaxParticleGenerateDistance = false;
    public boolean noWelcomeScreen = false;
    public int bufferFillerThreads = 4;
    public TakeOver takeOverRendering = TakeOver.VANILLA;
    public TakeOver takeOverTicking = TakeOver.VANILLA;

    public MadParticleConfig() {
    }
}
