package eu.wxrlds.enderbotanypots.compat.botanypotstiers;

import com.ultramega.botanypotstiers.PotTiers;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;

import static eu.wxrlds.enderbotanypots.EnderBotanyPots.MOD_ID;

public class BotanyPotsTiersCompat {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MOD_ID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MOD_ID);

    public static final RegistryObject<Block> ELITE_ENDER_BOTANY_POT = BLOCKS.register("elite_ender_botany_pot", () -> new BlockTieredEnderBotanyPot(PotTiers.ELITE));
    public static final RegistryObject<Block> ULTRA_ENDER_BOTANY_POT = BLOCKS.register("ultra_ender_botany_pot", () -> new BlockTieredEnderBotanyPot(PotTiers.ULTRA));
    public static final RegistryObject<Block> CREATIVE_ENDER_BOTANY_POT = BLOCKS.register("creative_ender_botany_pot", () -> new BlockTieredEnderBotanyPot(PotTiers.CREATIVE));

    public static final RegistryObject<Item> ELITE_ENDER_BOTANY_POT_ITEM = ITEMS.register("elite_ender_botany_pot", () -> new BlockItem(ELITE_ENDER_BOTANY_POT.get(), new Item.Properties()));
    public static final RegistryObject<Item> ULTRA_ENDER_BOTANY_POT_ITEM = ITEMS.register("ultra_ender_botany_pot", () -> new BlockItem(ULTRA_ENDER_BOTANY_POT.get(), new Item.Properties()));
    public static final RegistryObject<Item> CREATIVE_ENDER_BOTANY_POT_ITEM = ITEMS.register("creative_ender_botany_pot", () -> new BlockItem(CREATIVE_ENDER_BOTANY_POT.get(), new Item.Properties()));

    public static List<Block> getTieredPots() {
        return List.of(ELITE_ENDER_BOTANY_POT.get(), ULTRA_ENDER_BOTANY_POT.get(), CREATIVE_ENDER_BOTANY_POT.get());
    }

    public static List<ItemLike> getTabItems() {
        return List.of(ELITE_ENDER_BOTANY_POT_ITEM.get(), ULTRA_ENDER_BOTANY_POT_ITEM.get(), CREATIVE_ENDER_BOTANY_POT_ITEM.get());
    }

    public static void init(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
    }
}
