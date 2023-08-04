package cn.ussshenzhou.madparticle.item;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * @author USS_Shenzhou
 */
@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class TadaCopyCommandListener {

    public static String clipboardBuffer = null;

    @SubscribeEvent
    public static void onTooltipEvent(ItemTooltipEvent event) {
        var itemstack = event.getItemStack();
        if (itemstack.getItem() instanceof Tada) {
            if (Screen.hasShiftDown()) {
                if (itemstack.getTag() != null && itemstack.getTag().get(Tada.TAG_COMMAND) != null) {
                    var command = itemstack.getTag().getString(Tada.TAG_COMMAND);
                    if (!command.equals(clipboardBuffer)) {
                        Minecraft.getInstance().keyboardHandler.setClipboard(command);
                        clipboardBuffer = command;
                    }
                }
            } else {
                event.getToolTip().add(Component.translatable("item.madparticle.tada.tip"));
            }
        }
    }
}
