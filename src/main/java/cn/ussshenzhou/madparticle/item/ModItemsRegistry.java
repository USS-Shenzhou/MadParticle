package cn.ussshenzhou.madparticle.item;

import cn.ussshenzhou.madparticle.MadParticle;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;


/**
 * @author USS_Shenzhou
 */
public class ModItemsRegistry {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MadParticle.MOD_ID);

    public static final Supplier<Tada> TADA = ITEMS.registerItem("tada", t -> new Tada(t));
}
