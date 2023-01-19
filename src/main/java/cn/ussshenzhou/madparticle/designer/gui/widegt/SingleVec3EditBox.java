package cn.ussshenzhou.madparticle.designer.gui.widegt;

import cn.ussshenzhou.t88.gui.advanced.TSimpleConstrainedEditBox;
import cn.ussshenzhou.t88.gui.combine.TTitledSimpleConstrainedEditBox;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.network.chat.Component;

/**
 * @author Tony Yu
 */
public class SingleVec3EditBox extends TTitledSimpleConstrainedEditBox {

    public SingleVec3EditBox(Component titleText) {
        super(titleText, new TSimpleConstrainedEditBox(Vec3Argument.vec3()) {
            @Override
            public void checkAndThrow(String value) throws CommandSyntaxException {
                super.checkAndThrow(value + " 0 0");
            }
        });
    }
}
