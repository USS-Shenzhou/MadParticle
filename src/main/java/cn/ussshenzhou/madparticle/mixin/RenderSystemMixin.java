package cn.ussshenzhou.madparticle.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * @author USS_Shenzhou
 */
@Mixin(RenderSystem.AutoStorageIndexBuffer.class)
public class RenderSystemMixin {

    @Redirect(method = "ensureStorage",at = @At(value = "INVOKE",target = "Lcom/mojang/blaze3d/vertex/VertexFormat$IndexType;least(I)Lcom/mojang/blaze3d/vertex/VertexFormat$IndexType;"))
    private VertexFormat.IndexType madparticleAlwaysUseInt(int pIndexCount){
        return VertexFormat.IndexType.INT;
    }
}
