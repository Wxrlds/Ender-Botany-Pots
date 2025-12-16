package eu.wxrlds.enderbotanypots;


import eu.wxrlds.enderbotanypots.block.BlockEnderBotanyPot;
import eu.wxrlds.enderbotanypots.block.BlockEntityEnderBotanyPot;
import eu.wxrlds.enderbotanypots.compat.top.EnderBotanyPotsTOPPlugin;
import eu.wxrlds.enderbotanypots.recipe.EnderBotanyPotRecipe.EnderBotanyPotRecipe;
import net.darkhax.botanypots.block.BotanyPotRenderer;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(EnderBotanyPots.MOD_ID)
public class EnderBotanyPots {
    public static final String MOD_ID = "enderbotanypots";
    private static final Logger LOGGER = LogManager.getLogger();

    // Creative Tab
    public static final CreativeModeTab ENDERBOTANYPOTS_GROUP = new CreativeModeTab("enderbotanypots") {
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(ENDER_BOTANY_POT.get());
        }
    };

    // Setup to register the Ender Botany Pot
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MOD_ID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITIES, MOD_ID);
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, MOD_ID);

    public static final RegistryObject<Block> ENDER_BOTANY_POT = BLOCKS.register("ender_botany_pot", BlockEnderBotanyPot::new);
    public static final RegistryObject<Item> ENDER_BOTANY_POT_ITEM = ITEMS.register("ender_botany_pot", () -> new BlockItem(ENDER_BOTANY_POT.get(), new Item.Properties().tab(ENDERBOTANYPOTS_GROUP)));
    public static final RegistryObject<BlockEntityType<BlockEntityEnderBotanyPot>> ENDER_BOTANY_POT_TILE = BLOCK_ENTITY.register("ender_botany_pot", () -> BlockEntityType.Builder.of(BlockEntityEnderBotanyPot::new, ENDER_BOTANY_POT.get()).build(null));
    public static final RegistryObject<RecipeSerializer<?>> ENDER_BOTANY_POT_RECIPE = RECIPE_SERIALIZERS.register("crafting_enderbotanypot", EnderBotanyPotRecipe.Serializer::new);


    public EnderBotanyPots() {
        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Register mod content
        BLOCKS.register(eventBus);
        ITEMS.register(eventBus);
        BLOCK_ENTITY.register(eventBus);
        RECIPE_SERIALIZERS.register(eventBus);

        eventBus.addListener(this::setup);
        eventBus.addListener(this::enqueueIMC);
        eventBus.addListener(this::doClientStuff);
        eventBus.addListener(this::registerRenderers);

        MinecraftForge.EVENT_BUS.register(this);
    }

    private void setup(final FMLCommonSetupEvent event) {
        LOGGER.info("Transfering Ender energy to Botany Pots");
    }

    private void doClientStuff(final FMLClientSetupEvent event) {
        ItemBlockRenderTypes.setRenderLayer(ENDER_BOTANY_POT.get(), RenderType.cutout());
    }

    private void registerRenderers(final EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ENDER_BOTANY_POT_TILE.get(), BotanyPotRenderer::new);
    }

    private void enqueueIMC(final InterModEnqueueEvent event) {
        if (ModList.get().isLoaded("theoneprobe")) {
            InterModComms.sendTo("theoneprobe", "getTheOneProbe", EnderBotanyPotsTOPPlugin::new);
        }
    }
}
