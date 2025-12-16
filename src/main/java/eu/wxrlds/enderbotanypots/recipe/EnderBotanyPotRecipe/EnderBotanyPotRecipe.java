package eu.wxrlds.enderbotanypots.recipe.EnderBotanyPotRecipe;


import codechicken.enderstorage.api.Frequency;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import eu.wxrlds.enderbotanypots.EnderBotanyPots;
import eu.wxrlds.enderbotanypots.util.EnderBotanyPotHelper;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraftforge.registries.ForgeRegistryEntry;

import javax.annotation.Nonnull;

// Custom recipe class to copy the NBT (frequency) from an ingredient (Ender Chest/Tank/Pouch) to the result
public class EnderBotanyPotRecipe extends ShapelessRecipe {

    public EnderBotanyPotRecipe(ResourceLocation id, String group, ItemStack result, NonNullList<Ingredient> ingredients) {
        super(id, group, result, ingredients);
    }

    @Nonnull
    @Override
    public ItemStack assemble(CraftingContainer inv) {
        // Get the result defined in the JSON (The Ender Botany Pot)
        ItemStack result = super.assemble(inv);

        // Loop through the grid
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            // Find the Ender Storage item.
            if (EnderBotanyPotHelper.isValidFrequencyItem(stack)) {
                // Read the frequency from the ingredient
                Frequency freq = Frequency.readFromStack(stack);
                // Write it to the result item
                freq.writeToStack(result);
                // We stop looping through ingredients, after the first Ender Item has been found
                break;
            }
        }
        return result;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return EnderBotanyPots.ENDER_BOTANY_POT_RECIPE.get();
    }

    // Serializer for the recipe type so that it can be loaded from JSON
    // Main logic from Ender Storage / covers1624
    public static class Serializer extends ForgeRegistryEntry<RecipeSerializer<?>> implements RecipeSerializer<EnderBotanyPotRecipe> {
        @Override
        public EnderBotanyPotRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
            String group = GsonHelper.getAsString(json, "group", "");
            NonNullList<Ingredient> ingredients = readIngredients(GsonHelper.getAsJsonArray(json, "ingredients"));
            if (ingredients.isEmpty()) {
                throw new JsonParseException("No ingredients for shapeless recipe");
            } else if (ingredients.size() > 9) {
                throw new JsonParseException("Too many ingredients for shapeless recipe");
            } else {
                ItemStack result = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "result"));
                return new EnderBotanyPotRecipe(recipeId, group, result, ingredients);
            }
        }

        private static NonNullList<Ingredient> readIngredients(JsonArray jsonArray) {
            NonNullList<Ingredient> list = NonNullList.create();
            for (int i = 0; i < jsonArray.size(); ++i) {
                Ingredient ingredient = Ingredient.fromJson(jsonArray.get(i));
                if (!ingredient.isEmpty()) {
                    list.add(ingredient);
                }
            }
            return list;
        }

        @Override
        public EnderBotanyPotRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
            String group = buffer.readUtf(32767);
            int i = buffer.readVarInt();
            NonNullList<Ingredient> ingredients = NonNullList.withSize(i, Ingredient.EMPTY);
            ingredients.replaceAll(ignored -> Ingredient.fromNetwork(buffer));
            ItemStack result = buffer.readItem();
            return new EnderBotanyPotRecipe(recipeId, group, result, ingredients);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, EnderBotanyPotRecipe recipe) {
            buffer.writeUtf(recipe.getGroup());
            buffer.writeVarInt(recipe.getIngredients().size());
            for (Ingredient ingredient : recipe.getIngredients()) {
                ingredient.toNetwork(buffer);
            }
            buffer.writeItem(recipe.getResultItem());
        }
    }
}
