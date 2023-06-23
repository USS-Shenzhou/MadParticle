package cn.ussshenzhou.madparticle.item;

import cn.ussshenzhou.madparticle.MadParticle;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * @author USS_Shenzhou
 */
public class ModItemsRegistry {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MadParticle.MOD_ID);

    public static final RegistryObject<Tada> TADA = ITEMS.register("tada", Tada::new);
}
