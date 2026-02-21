package cn.ussshenzhou.madparticle.item;

import cn.ussshenzhou.madparticle.item.component.ModDataComponent;
import cn.ussshenzhou.madparticle.item.component.TadaComponent;
import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.permissions.LevelBasedPermissionSet;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author USS_Shenzhou
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class Tada extends Item {

    public static final String TAG_COMMAND = "command";
    public static final String PULSE = "pulse";
    public static final String USED = "used";

    public Tada(Item.Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        if (itemstack.get(ModDataComponent.TADA_COMPONENT) != null) {
            player.startUsingItem(hand);
            return InteractionResult.CONSUME;
        } else {
            return InteractionResult.FAIL;
        }
    }

    @Override
    public void onUseTick(Level pLevel, LivingEntity pLivingEntity, ItemStack stack, int pRemainingUseDuration) {
        if (!pLevel.isClientSide()) {
            var data = stack.getOrDefault(ModDataComponent.TADA_COMPONENT, TadaComponent.defaultValue());
            var command = data.command();
            if (data.pulse()) {
                if (!data.used()) {
                    performCommand(pLevel, pLivingEntity, command);
                    stack.set(ModDataComponent.TADA_COMPONENT, (TadaComponent) data.setByName(stack, USED, true));
                }
            } else {
                performCommand(pLevel, pLivingEntity, command);
            }
        }
    }

    @Override
    public void onStopUsing(ItemStack stack, LivingEntity entity, int count) {
        if (!entity.level().isClientSide()) {
            stack.set(ModDataComponent.TADA_COMPONENT, (TadaComponent) stack.getOrDefault(ModDataComponent.TADA_COMPONENT, TadaComponent.defaultValue()).setByName(stack, USED, false));
        }
    }

    private void performCommand(Level pLevel, LivingEntity pLivingEntity, String command) {
        pLevel.getServer().getCommands().performPrefixedCommand(pLivingEntity.createCommandSourceStackForNameResolution((ServerLevel) pLevel).withPermission(LevelBasedPermissionSet.GAMEMASTER), command);
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000;
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> consumer, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, consumer, flag);
        if (stack.getOrDefault(ModDataComponent.TADA_COMPONENT, TadaComponent.defaultValue()).pulse()) {
            consumer.accept(Component.translatable("item.madparticle.tada.mode.pulse"));
        } else {
            consumer.accept(Component.translatable("item.madparticle.tada.mode.con"));
        }
    }
}
