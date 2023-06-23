package cn.ussshenzhou.madparticle.item;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * @author USS_Shenzhou
 */
public class Tada extends Item {

    public static final String TAG_COMMAND = "command";

    public Tada() {
        super(new Properties());
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
            pLevel.getServer().getCommands().performPrefixedCommand(pLivingEntity.createCommandSourceStack().withPermission(2), pStack.getTag().getString(TAG_COMMAND));
        }
    }

    @Override
    public int getUseDuration(ItemStack pStack) {
        return 72000;
    }
}
