package eu.wxrlds.enderbotanypots.recipe.EnderBotanyPotRecipe;


import codechicken.enderstorage.api.Frequency;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import eu.wxrlds.enderbotanypots.EnderBotanyPots;
import eu.wxrlds.enderbotanypots.util.EnderBotanyPotHelper;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;

import javax.annotation.Nonnull;

// Custom recipe class to copy the NBT (frequency) from an ingredient (Ender Chest/Tank/Pouch) to the result
public class EnderBotanyPotRecipe extends ShapelessRecipe {

    public EnderBotanyPotRecipe(String group, CraftingBookCategory category, ItemStack result, NonNullList<Ingredient> ingredients) {
        super(group, category, result, ingredients);
    }

    @Nonnull
    @Override
    public ItemStack assemble(CraftingContainer inv, RegistryAccess registryAccess) {
        // Get the result defined in the JSON (The Ender Botany Pot)
        ItemStack result = super.assemble(inv, registryAccess);

        // Loop through the grid
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);

            // Find the Ender Storage item.
            // If it's not empty, and it is NOT from botanypots (so it's not the hopper pot),
            // it must be the Ender Chest/Tank/Bag.
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
    public static class Serializer implements RecipeSerializer<EnderBotanyPotRecipe> {

        private static final Codec<EnderBotanyPotRecipe> CODEC = RecordCodecBuilder.create(builder -> builder.group(
                ExtraCodecs.strictOptionalField(Codec.STRING, "group", "").forGetter(ShapelessRecipe::getGroup),
                CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC).forGetter(ShapelessRecipe::category),
                ItemStack.ITEM_WITH_COUNT_CODEC.fieldOf("result").forGetter(r -> r.getResultItem(RegistryAccess.EMPTY)),
                Ingredient.CODEC_NONEMPTY.listOf().fieldOf("ingredients")
                        .flatXmap(
                                ingredients -> {
                                    Ingredient[] aingredient = ingredients.toArray(Ingredient[]::new);
                                    if (aingredient.length == 0) {
                                        return DataResult.error(() -> "No ingredients for shapeless recipe");
                                    } else if (aingredient.length > ShapedRecipePattern.getMaxHeight() * ShapedRecipePattern.getMaxWidth()) {
                                        return DataResult.error(() -> "Too many ingredients for shapeless recipe");
                                    } else {
                                        return DataResult.success(NonNullList.of(Ingredient.EMPTY, aingredient));
                                    }
                                },
                                DataResult::success
                        ).forGetter(ShapelessRecipe::getIngredients)
        ).apply(builder, EnderBotanyPotRecipe::new));


        @Override
        public Codec<EnderBotanyPotRecipe> codec() {
            return CODEC;
        }


        @Override
        public EnderBotanyPotRecipe fromNetwork(FriendlyByteBuf buffer) {
            String group = buffer.readUtf();
            CraftingBookCategory category = buffer.readEnum(CraftingBookCategory.class);
            int count = buffer.readVarInt();
            NonNullList<Ingredient> ingredients = NonNullList.withSize(count, Ingredient.EMPTY);

            ingredients.replaceAll(ignored -> Ingredient.fromNetwork(buffer));

            ItemStack result = buffer.readItem();
            return new EnderBotanyPotRecipe(group, category, result, ingredients);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, EnderBotanyPotRecipe recipe) {
            buffer.writeUtf(recipe.getGroup());
            buffer.writeEnum(recipe.category());
            buffer.writeVarInt(recipe.getIngredients().size());
            for (Ingredient ingredient : recipe.getIngredients()) {
                ingredient.toNetwork(buffer);
            }
            buffer.writeItem(recipe.getResultItem(RegistryAccess.EMPTY));
        }
    }
}
