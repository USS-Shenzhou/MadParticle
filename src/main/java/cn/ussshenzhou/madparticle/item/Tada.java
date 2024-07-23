package cn.ussshenzhou.madparticle.item;

import cn.ussshenzhou.madparticle.item.component.ModDataComponent;
import cn.ussshenzhou.madparticle.item.component.TadaComponent;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

/**
 * @author USS_Shenzhou
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class Tada extends Item {

    public static final String TAG_COMMAND = "command";
    public static final String PULSE = "pulse";
    public static final String USED = "used";

    public Tada() {
        super(new Properties().stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        ItemStack itemstack = pPlayer.getItemInHand(pUsedHand);
        if (itemstack.get(ModDataComponent.TADA_COMPONENT) != null) {
            pPlayer.startUsingItem(pUsedHand);
            return InteractionResultHolder.consume(itemstack);
        } else {
            return InteractionResultHolder.fail(itemstack);
        }
    }

    @Override
    public void onUseTick(Level pLevel, LivingEntity pLivingEntity, ItemStack stack, int pRemainingUseDuration) {
        if (!pLevel.isClientSide) {
            var data = stack.getOrDefault(ModDataComponent.TADA_COMPONENT, TadaComponent.defaultValue());
            var command = data.command();
            if (data.pulse()) {
                if (!data.used()) {
                    performCommand(pLevel, pLivingEntity, command);
                    data.setByName(stack, USED, true);
                    stack.set(ModDataComponent.TADA_COMPONENT, data);
                }
            } else {
                performCommand(pLevel, pLivingEntity, command);
            }
        }
    }

    @Override
    public void onStopUsing(ItemStack stack, LivingEntity entity, int count) {
        if (!entity.level().isClientSide) {
            stack.getOrDefault(ModDataComponent.TADA_COMPONENT, TadaComponent.defaultValue()).setByName(stack, USED, false);
        }
    }

    private void performCommand(Level pLevel, LivingEntity pLivingEntity, String command) {
        pLevel.getServer().getCommands().performPrefixedCommand(pLivingEntity.createCommandSourceStack().withPermission(2), command);
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000;
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        if (stack.getOrDefault(ModDataComponent.TADA_COMPONENT, TadaComponent.defaultValue()).pulse()) {
            tooltipComponents.add(Component.translatable("item.madparticle.tada.mode.pulse"));
        } else {
            tooltipComponents.add(Component.translatable("item.madparticle.tada.mode.con"));
        }
    }
}
