package eu.wxrlds.enderbotanypots.compat.jei;


import eu.wxrlds.enderbotanypots.EnderBotanyPots;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.darkhax.botanypots.addons.jei.JEIPlugin;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

@JeiPlugin
public class EnderBotanyPotsJEIPlugin implements IModPlugin {
    @Override
    public ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath(EnderBotanyPots.MOD_ID, "jei");
    }

    // The Ender Botany Pot should be shown alongside the other Botany Pots
    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(
                new ItemStack(EnderBotanyPots.ENDER_BOTANY_POT.get()),
                JEIPlugin.CROP
        );
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
