package eu.wxrlds.enderbotanypots.compat.botanypotstiers;

import com.ultramega.botanypotstiers.PotTiers;
import eu.wxrlds.enderbotanypots.EnderBotanyPots;
import eu.wxrlds.enderbotanypots.block.BlockEnderBotanyPot;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class BlockTieredEnderBotanyPot extends BlockEnderBotanyPot {
    public final PotTiers tier;

    public BlockTieredEnderBotanyPot(PotTiers tier) {
        super();
        this.tier = tier;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntityTieredEnderBotanyPot(this.tier, pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, EnderBotanyPots.ENDER_BOTANY_POT_TILE.get(), BlockEntityTieredEnderBotanyPot::tick);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @javax.annotation.Nullable BlockGetter getter, @NotNull List<Component> components, @NotNull TooltipFlag flag) {
        super.appendHoverText(stack, getter, components, flag);
        if (Screen.hasShiftDown()) {
            components.add(Component.translatable("tooltip.botanypotstiers.tiered_botany_pot.multiplier", tier.getMultiplier()).withStyle(ChatFormatting.AQUA));
            components.add(Component.translatable("tooltip.botanypotstiers.tiered_botany_pot.speed", tier.getSpeed()).withStyle(ChatFormatting.AQUA));
        } else {
            components.add(Component.translatable("tooltip.botanypotstiers.tiered_botany_pot.pressShiftForMore").withStyle(ChatFormatting.YELLOW));
        }
    }
}
