package eu.wxrlds.enderbotanypots.util;

import codechicken.enderstorage.item.ItemEnderPouch;
import codechicken.enderstorage.item.ItemEnderStorage;
import eu.wxrlds.enderbotanypots.EnderBotanyPots;
import net.minecraft.item.ItemStack;

public class EnderBotanyPotHelper {
    // Checks if the item is a valid source of Frequency data.
    // Includes: Ender Chest, Ender Tank, Ender Pouch, and Ender Botany Pot.
    public static boolean isValidFrequencyItem(ItemStack stack) {
        if (stack.isEmpty()) return false;

        return stack.getItem() instanceof ItemEnderPouch
                || stack.getItem() instanceof ItemEnderStorage
                || stack.getItem() == EnderBotanyPots.ENDER_BOTANY_POT_ITEM.get();
    }
}