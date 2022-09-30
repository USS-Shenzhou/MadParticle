package cn.ussshenzhou.madparticle.mixin;

import net.minecraft.commands.arguments.coordinates.WorldCoordinate;
import net.minecraft.commands.arguments.coordinates.WorldCoordinates;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * @author Tony Yu
 */
@Mixin(WorldCoordinates.class)
public interface WorldCoordinatesAccessor {
    @Accessor("x")
    WorldCoordinate getX();

    @Accessor("y")
    WorldCoordinate getY();

    @Accessor("z")
    WorldCoordinate getZ();
}
