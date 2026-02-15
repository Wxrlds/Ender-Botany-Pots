package eu.wxrlds.enderbotanypots.util;

import codechicken.enderstorage.item.ItemEnderPouch;
import codechicken.enderstorage.item.ItemEnderStorage;
import eu.wxrlds.enderbotanypots.block.BlockEnderBotanyPot;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;


public class EnderBotanyPotHelper {
    // Checks if the item is a valid source of Frequency data.
    // Includes: Ender Chest, Ender Tank, Ender Pouch, and
    // Ender Botany Pot with all addon mods
    public static boolean isValidFrequencyItem(ItemStack stack) {
        if (stack.isEmpty()) return false;
        Item itemInHand = stack.getItem();

        // Check for Ender Storage items
        if (itemInHand instanceof ItemEnderPouch || itemInHand instanceof ItemEnderStorage) {
            return true;
        }

        // Check for Ender Botany Pot items (including tiered ones)
        if (itemInHand instanceof BlockItem blockItem && blockItem.getBlock() instanceof BlockEnderBotanyPot) {
            return true;
        }
        return false;
    }
}