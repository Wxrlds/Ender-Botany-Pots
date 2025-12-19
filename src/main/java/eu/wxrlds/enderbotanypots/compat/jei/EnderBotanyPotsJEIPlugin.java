package eu.wxrlds.enderbotanypots.compat.jei;


import eu.wxrlds.enderbotanypots.EnderBotanyPots;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.darkhax.botanypots.common.addons.jei.BotanyPotsJEIPlugin;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

@JeiPlugin
public class EnderBotanyPotsJEIPlugin implements IModPlugin {
    @Override
    public ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath(EnderBotanyPots.MOD_ID, "jei");
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        // Add the description info
        registration.addIngredientInfo(
                new ItemStack(EnderBotanyPots.ENDER_BOTANY_POT.get()),
                VanillaTypes.ITEM_STACK,
                Component.translatable("enderbotanypots.jei.description")
        );
    }
}
