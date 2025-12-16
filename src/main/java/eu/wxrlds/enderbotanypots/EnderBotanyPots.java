package eu.wxrlds.enderbotanypots;

import eu.wxrlds.enderbotanypots.block.BlockEnderBotanyPot;
import eu.wxrlds.enderbotanypots.block.TileEnderBotanyPot;
import eu.wxrlds.enderbotanypots.compat.top.EnderBotanyPotsTOPPlugin;
import eu.wxrlds.enderbotanypots.recipe.EnderBotanyPotRecipe.EnderBotanyPotRecipe;
import net.darkhax.botanypots.block.tileentity.RendererBotanyPot;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


@Mod(EnderBotanyPots.MOD_ID)
public class EnderBotanyPots {
    public static final String MOD_ID = "enderbotanypots";
    private static final Logger LOGGER = LogManager.getLogger();

    // Creative Tab
    public static final ItemGroup ENDERBOTANYPOTS_GROUP = new net.minecraft.item.ItemGroup("enderbotanypots") {
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(ENDER_BOTANY_POT.get());
        }
    };

    // Setup to register the Ender Botany Pot
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MOD_ID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MOD_ID);
    public static final DeferredRegister<TileEntityType<?>> BLOCK_ENTITY = DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, MOD_ID);
    public static final DeferredRegister<IRecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, MOD_ID);

    public static final RegistryObject<Block> ENDER_BOTANY_POT = BLOCKS.register("ender_botany_pot", BlockEnderBotanyPot::new);
    public static final RegistryObject<Item> ENDER_BOTANY_POT_ITEM = ITEMS.register("ender_botany_pot", () -> new BlockItem(ENDER_BOTANY_POT.get(), new Item.Properties().tab(ENDERBOTANYPOTS_GROUP)));
    public static final RegistryObject<TileEntityType<TileEnderBotanyPot>> ENDER_BOTANY_POT_TILE = BLOCK_ENTITY.register("ender_botany_pot", () -> TileEntityType.Builder.of(TileEnderBotanyPot::new, ENDER_BOTANY_POT.get()).build(null));
    public static final RegistryObject<IRecipeSerializer<?>> ENDER_BOTANY_POT_RECIPE = RECIPE_SERIALIZERS.register("crafting_enderbotanypot", EnderBotanyPotRecipe.Serializer::new);


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

        MinecraftForge.EVENT_BUS.register(this);
    }

    private void setup(final FMLCommonSetupEvent event) {
        LOGGER.info("Transfering Ender energy to Botany Pots");
    }

    private void doClientStuff(final FMLClientSetupEvent event) {
        ClientRegistry.bindTileEntityRenderer(ENDER_BOTANY_POT_TILE.get(), RendererBotanyPot::new);
        RenderTypeLookup.setRenderLayer(ENDER_BOTANY_POT.get(), RenderType.cutout());
    }

    private void enqueueIMC(final InterModEnqueueEvent event) {
        if (ModList.get().isLoaded("theoneprobe")) {
            InterModComms.sendTo("theoneprobe", "getTheOneProbe", EnderBotanyPotsTOPPlugin::new);
        }
    }
}
