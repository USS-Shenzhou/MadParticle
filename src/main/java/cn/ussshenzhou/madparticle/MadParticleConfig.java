package cn.ussshenzhou.madparticle;

import cn.ussshenzhou.madparticle.particle.enums.LightCacheRefreshInterval;
import cn.ussshenzhou.madparticle.particle.enums.TakeOver;
import cn.ussshenzhou.madparticle.particle.enums.TranslucentMethod;
import cn.ussshenzhou.t88.config.TConfig;

/**
 * @author USS_Shenzhou
 */
public class MadParticleConfig implements TConfig {
    public int maxParticleAmountOfSingleQueue = 100000;
    public boolean limitMaxParticleGenerateDistance = false;
    public boolean noWelcomeScreen = false;
    public int bufferFillerThreads = 4;
    public TakeOver takeOverRendering = TakeOver.VANILLA;
    public TakeOver takeOverTicking = TakeOver.VANILLA;
    public boolean optimizeCommandBlockEditScreen = true;
    @SuppressWarnings("AlibabaLowerCamelCaseVariableNaming")
    public int lightCacheXZRange = 128;
    @SuppressWarnings("AlibabaLowerCamelCaseVariableNaming")
    public int lightCacheYRange = 64;
    public boolean forceMaxLight = false;
    public TranslucentMethod translucentMethod = TranslucentMethod.OIT;
    public LightCacheRefreshInterval lightCacheRefreshInterval = LightCacheRefreshInterval.FRAME;

    public MadParticleConfig() {
    }
}
