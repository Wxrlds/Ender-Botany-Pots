package eu.wxrlds.enderbotanypots.compat.botanypotstiers;

import com.ultramega.botanypotstiers.common.impl.PotTier;
import eu.wxrlds.enderbotanypots.block.EnderBotanyPotBlock;
import net.darkhax.botanypots.common.api.context.BotanyPotContext;
import net.darkhax.botanypots.common.api.data.recipes.crop.Crop;
import net.darkhax.botanypots.common.api.data.recipes.soil.Soil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TieredEnderBotanyPotBlock extends EnderBotanyPotBlock {
    private final PotTier tier;

    public TieredEnderBotanyPotBlock(PotTier tier) {
        super();
        this.tier = tier;
    }

    // This is just copy and paste from Tiered Botany Pots. Our block also needs to modify the growth rate
    @Override
    public float getGrowthModifier(BotanyPotContext context, Level level, Crop crop, @Nullable Soil soil) {
        return (float) this.tier.getSpeedMultiplier();
    }

    // This is just copy and paste from Tiered Botany Pots. Our block also needs to modify the yield modifier
    @Override
    public float getYieldModifier(final BotanyPotContext context, final Level level, final Crop crop, @Nullable final Soil soil) {
        return this.tier.getOutputMultiplier();
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flagIn) {
        super.appendHoverText(stack, context, tooltip, flagIn);
        if (Screen.hasShiftDown()) {
            tooltip.add(Component.translatable("tooltip.botanypotstiers.tiered_botany_pot.speed", tier.getSpeedMultiplier()).withStyle(ChatFormatting.AQUA));
            tooltip.add(Component.translatable("tooltip.botanypotstiers.tiered_botany_pot.multiplier", tier.getOutputMultiplier()).withStyle(ChatFormatting.AQUA));
        } else {
            tooltip.add(Component.translatable("tooltip.botanypotstiers.tiered_botany_pot.pressShiftForMore")
                    .withStyle(ChatFormatting.YELLOW));
        }
    }
}