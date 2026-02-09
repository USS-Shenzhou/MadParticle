package cn.ussshenzhou.madparticle.item;

import cn.ussshenzhou.madparticle.item.component.ModDataComponent;
import cn.ussshenzhou.madparticle.item.component.TadaComponent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;

/**
 * @author USS_Shenzhou
 */
@EventBusSubscriber(value = Dist.CLIENT)
public class TadaCopyCommandListener {

    public static String clipboardBuffer = null;

    @SubscribeEvent
    public static void onTooltipEvent(ItemTooltipEvent event) {
        var itemstack = event.getItemStack();
        if (itemstack.getItem() instanceof Tada) {
            if (event.getFlags().hasShiftDown()) {
                var command = itemstack.getOrDefault(ModDataComponent.TADA_COMPONENT, TadaComponent.defaultValue()).command();
                if (!command.equals(clipboardBuffer)) {
                    Minecraft.getInstance().keyboardHandler.setClipboard(command);
                    clipboardBuffer = command;
                }
            } else {
                event.getToolTip().add(Component.translatable("item.madparticle.tada.tip"));
            }
        }
    }
}
