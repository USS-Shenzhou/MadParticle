package cn.ussshenzhou.madparticle.item;

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

import java.util.List;

/**
 * @author USS_Shenzhou
 */
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
        if (itemstack.getTag() != null && itemstack.getTag().get(TAG_COMMAND) != null) {
            pPlayer.startUsingItem(pUsedHand);
            return InteractionResultHolder.consume(itemstack);
        } else {
            return InteractionResultHolder.fail(itemstack);
        }
    }

    @Override
    public void onUseTick(Level pLevel, LivingEntity pLivingEntity, ItemStack pStack, int pRemainingUseDuration) {
        if (!pLevel.isClientSide) {
            var tag = pStack.getOrCreateTag();
            var command = tag.getString(TAG_COMMAND);
            if (!tag.contains(USED)) {
                tag.putBoolean(USED, false);
            }
            if (tag.getBoolean(PULSE)) {
                if (!tag.getBoolean(USED)) {
                    performCommand(pLevel, pLivingEntity, command);
                    tag.putBoolean(USED, true);
                }
            } else {
                performCommand(pLevel, pLivingEntity, command);
            }
        }
    }

    @Override
    public void onStopUsing(ItemStack stack, LivingEntity entity, int count) {
        if (!entity.level().isClientSide) {
            stack.getTag().putBoolean(USED, false);
        }
    }

    private void performCommand(Level pLevel, LivingEntity pLivingEntity, String command) {
        pLevel.getServer().getCommands().performPrefixedCommand(pLivingEntity.createCommandSourceStack().withPermission(2), command);
    }

    @Override
    public int getUseDuration(ItemStack pStack) {
        return 72000;
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged;
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
        if (pStack.getOrCreateTag().getBoolean(PULSE)) {
            pTooltipComponents.add(Component.translatable("item.madparticle.tada.mode.pulse"));
        } else {
            pTooltipComponents.add(Component.translatable("item.madparticle.tada.mode.con"));
        }
    }
}
