package cn.ussshenzhou.madparticle;

import cn.ussshenzhou.madparticle.particle.enums.LightCacheRefreshInterval;
import cn.ussshenzhou.madparticle.particle.enums.TakeOver;
import cn.ussshenzhou.madparticle.particle.enums.TranslucentMethod;
import cn.ussshenzhou.t88.config.TConfig;
import net.minecraft.util.Mth;

/**
 * @author USS_Shenzhou
 */
public class MadParticleConfig implements TConfig {
    public int maxParticleAmountOfSingleQueue = 100000;
    public boolean limitMaxParticleGenerateDistance = false;
    public boolean noWelcomeScreen = false;
    protected int bufferFillerThreads = 8;
    public TakeOver takeOverRendering = TakeOver.VANILLA;
    public TakeOver takeOverTicking = TakeOver.VANILLA;
    public boolean optimizeCommandBlockEditScreen = true;
    @SuppressWarnings("AlibabaLowerCamelCaseVariableNaming")
    public int lightCacheXZRange = 256;
    private String _comment = "Y Range MUST be even";
    @SuppressWarnings("AlibabaLowerCamelCaseVariableNaming")
    public int lightCacheYRange = 128;
    public boolean forceMaxLight = false;
    public TranslucentMethod translucentMethod = TranslucentMethod.OIT;
    public LightCacheRefreshInterval lightCacheRefreshInterval = LightCacheRefreshInterval.FRAME;

    public MadParticleConfig() {
    }

    public int getBufferFillerThreads() {
        return Mth.clamp(bufferFillerThreads, 1, 512);
    }

    public void setBufferFillerThreads(int bufferFillerThreads) {
        this.bufferFillerThreads = bufferFillerThreads;
    }
}
