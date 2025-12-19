package eu.wxrlds.enderbotanypots.recipe.EnderBotanyPotRecipe;


import codechicken.enderstorage.api.Frequency;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import eu.wxrlds.enderbotanypots.EnderBotanyPots;
import eu.wxrlds.enderbotanypots.util.EnderBotanyPotHelper;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
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
    public ItemStack assemble(CraftingInput inv, HolderLookup.Provider registries) {
        // Get the result defined in the JSON (The Ender Botany Pot)
        ItemStack result = super.assemble(inv, registries);

        // Loop through the grid
        for (int i = 0; i < inv.size(); i++) {
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

        private static final MapCodec<EnderBotanyPotRecipe> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
                Codec.STRING.optionalFieldOf("group", "").forGetter(EnderBotanyPotRecipe::getGroup),
                CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC).forGetter(EnderBotanyPotRecipe::category),
                ItemStack.STRICT_CODEC.fieldOf("result").forGetter(r -> r.getResultItem(RegistryAccess.EMPTY)),
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

        private static final StreamCodec<RegistryFriendlyByteBuf, EnderBotanyPotRecipe> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.STRING_UTF8, EnderBotanyPotRecipe::getGroup,
                CraftingBookCategory.STREAM_CODEC, EnderBotanyPotRecipe::category,
                ItemStack.STREAM_CODEC, r -> r.getResultItem(RegistryAccess.EMPTY),
                Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()), EnderBotanyPotRecipe::getIngredients,
                (group, category, result, ingredients) -> new EnderBotanyPotRecipe(group, category, result, NonNullList.of(Ingredient.EMPTY, ingredients.toArray(Ingredient[]::new)))
        );

        @Override
        public MapCodec<EnderBotanyPotRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, EnderBotanyPotRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
