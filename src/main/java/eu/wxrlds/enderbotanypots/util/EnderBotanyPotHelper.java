package eu.wxrlds.enderbotanypots.util;

import codechicken.enderstorage.item.ItemEnderPouch;
import codechicken.enderstorage.item.ItemEnderStorage;
import eu.wxrlds.enderbotanypots.block.EnderBotanyPotBlock;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;


public class EnderBotanyPotHelper {
    // Checks if the item is a valid source of Frequency data.
    // Includes: Ender Chest, Ender Tank, Ender Pouch, and
    // Ender Botany Pot with all addon mods, if they are of type EnderBotanyPotBlock
    public static boolean isValidFrequencyItem(ItemStack stack) {
        if (stack.isEmpty()) return false;

        // Check for Ender Storage items
        if (stack.getItem() instanceof ItemEnderPouch || stack.getItem() instanceof ItemEnderStorage) {
            return true;
        }

        // Check for Ender Botany Pot items (including tiered ones)
        if (stack.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof EnderBotanyPotBlock) {
            return true;
        }

        return false;
    }
}