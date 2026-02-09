package cn.ussshenzhou.madparticle.particle;

import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

/**
 * @author USS_Shenzhou
 */
@MethodsReturnNonnullByDefault
public class CustomParticle extends SingleQuadParticle {
    public CustomParticle(ClientLevel level, double x, double y, double z, TextureAtlasSprite sprite) {
        super(level, x, y, z, sprite);
        this.lifetime = 40;
    }

    public CustomParticle(ClientLevel level, double x, double y, double z, double xa, double ya, double za, TextureAtlasSprite sprite) {
        super(level, x, y, z, xa, ya, za, sprite);
        this.lifetime = 40;
    }

    @Override
    protected Layer getLayer() {
        return Layer.TRANSLUCENT;
    }
}
