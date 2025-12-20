package eu.wxrlds.enderbotanypots;


import eu.wxrlds.enderbotanypots.block.EnderBotanyPotBlock;
import eu.wxrlds.enderbotanypots.block.EnderBotanyPotBlockEntity;
import eu.wxrlds.enderbotanypots.compat.botanypotstiers.BotanyPotsTiersCompat;
import eu.wxrlds.enderbotanypots.compat.top.EnderBotanyPotsTOPPlugin;
import eu.wxrlds.enderbotanypots.recipe.EnderBotanyPotRecipe.EnderBotanyPotRecipe;
import net.darkhax.botanypots.common.impl.block.BotanyPotRenderer;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.InterModComms;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.InterModEnqueueEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

@Mod(EnderBotanyPots.MOD_ID)
public class EnderBotanyPots {
    public static final String MOD_ID = "enderbotanypots";
    private static final Logger LOGGER = LogManager.getLogger();

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Registries.BLOCK, MOD_ID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MOD_ID);
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, MOD_ID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MOD_ID);

    public static final DeferredHolder<Block, EnderBotanyPotBlock> ENDER_BOTANY_POT = BLOCKS.register("ender_botany_pot", EnderBotanyPotBlock::new);
    public static final DeferredHolder<Item, BlockItem> ENDER_BOTANY_POT_ITEM = ITEMS.register("ender_botany_pot", () -> new BlockItem(ENDER_BOTANY_POT.get(), new Item.Properties()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<EnderBotanyPotBlockEntity>> ENDER_BOTANY_POT_TILE = BLOCK_ENTITY.register("ender_botany_pot", () -> {
        List<Block> validBlocks = new ArrayList<>();
        validBlocks.add(ENDER_BOTANY_POT.get());

        if (ModList.get().isLoaded("botanypotstiers")) {
            validBlocks.addAll(BotanyPotsTiersCompat.getTieredPots());
        }

        return BlockEntityType.Builder.of(EnderBotanyPotBlockEntity::new, validBlocks.toArray(new Block[0])).build(null);
    });

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<EnderBotanyPotRecipe>> ENDER_BOTANY_POT_RECIPE = RECIPE_SERIALIZERS.register("crafting_enderbotanypot", EnderBotanyPotRecipe.Serializer::new);
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TAB = CREATIVE_TABS.register("enderbotanypots", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.enderbotanypots"))
            .icon(() -> new ItemStack(ENDER_BOTANY_POT.get()))
            .displayItems((params, output) -> {
                output.accept(ENDER_BOTANY_POT.get());
                if (ModList.get().isLoaded("botanypotstiers")) {
                    BotanyPotsTiersCompat.getTabItems().forEach(output::accept);
                }
            })
            .build());


    public EnderBotanyPots(IEventBus modEventBus) {
        modEventBus.addListener(this::commonSetup);

        // Register base mod content
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        BLOCK_ENTITY.register(modEventBus);
        RECIPE_SERIALIZERS.register(modEventBus);
        CREATIVE_TABS.register(modEventBus);

        // Register Botany Pots Tiers items
        if (ModList.get().isLoaded("botanypotstiers")) {
            BotanyPotsTiersCompat.init(modEventBus);
        }

        modEventBus.addListener(this::enqueueIMC);
        modEventBus.addListener(this::registerRenderers);

        NeoForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("Transfering Ender energy to Botany Pots");
    }

    private void registerRenderers(final EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ENDER_BOTANY_POT_TILE.get(), BotanyPotRenderer::new);
    }

    private void enqueueIMC(final InterModEnqueueEvent event) {
        if (ModList.get().isLoaded("theoneprobe")) {
            InterModComms.sendTo("theoneprobe", "getTheOneProbe", EnderBotanyPotsTOPPlugin::new);
        }
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // this is required or the game won't launch
    }
}
