package eu.wxrlds.enderbotanypots.block;


import codechicken.enderstorage.api.Frequency;
import eu.wxrlds.enderbotanypots.EnderBotanyPots;
import eu.wxrlds.enderbotanypots.util.EnderBotanyPotHelper;
import net.darkhax.botanypots.common.impl.block.BotanyPotBlock;
import net.darkhax.botanypots.common.impl.block.PotType;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class EnderBotanyPotBlock extends BotanyPotBlock {

    public EnderBotanyPotBlock() {
        // In 1.21.1 the pot has to be of type hopper, so that we can also require a harvest item
        super(MapColor.COLOR_BLACK, PotType.HOPPER);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new EnderBotanyPotBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, EnderBotanyPots.ENDER_BOTANY_POT_TILE.get(), EnderBotanyPotBlockEntity::tick);
    }

    // Places the block and uses the frequency of the items NBT
    // Overwrites the items NBT if the player is holding the item in the off-hand
    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);

        BlockEntity tile = level.getBlockEntity(pos);
        if (tile instanceof EnderBotanyPotBlockEntity pot && placer != null) {

            Frequency originalFreq = Frequency.readFromStack(stack);
            Frequency freqToSet = originalFreq;
            boolean copiedFromHand = false;

            ItemStack mainHandStack = placer.getItemInHand(InteractionHand.MAIN_HAND);
            ItemStack offHandStack = placer.getItemInHand(InteractionHand.OFF_HAND);

            if (mainHandStack.getItem() == this.asItem() && EnderBotanyPotHelper.isValidFrequencyItem(offHandStack)) {
                freqToSet = Frequency.readFromStack(offHandStack);
                copiedFromHand = true;
            }

            pot.setFrequency(freqToSet);

            boolean isDifferent = !originalFreq.toString().equals(freqToSet.toString());
            if (copiedFromHand && isDifferent && !level.isClientSide && placer instanceof Player player) {
                sendFrequencyMessage(player, freqToSet);
            }
        }
    }

    // Right click interaction with other Ender Storage items
    @NotNull
    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        // Only continues if the player is holding an Ender Pouch/Chest/Tank/Pot
        if (EnderBotanyPotHelper.isValidFrequencyItem(stack)) {
            BlockEntity tile = level.getBlockEntity(pos);
            if (tile instanceof EnderBotanyPotBlockEntity pot) {
                Frequency currentFreq = pot.getFrequency();

                // If the pot has an owner, only THAT owner can change it
                if (currentFreq.hasOwner() && !player.getUUID().equals(currentFreq.owner().orElse(null))) {
                    if (!level.isClientSide) {
                        player.sendSystemMessage(Component.translatable("enderbotanypots.chat.not_owner").withStyle(ChatFormatting.RED));
                    }
                    return ItemInteractionResult.FAIL;
                }

                Frequency newFreq = Frequency.readFromStack(stack);

                if (!currentFreq.toString().equals(newFreq.toString())) {
                    if (!level.isClientSide) {
                        pot.setFrequency(newFreq);
                        sendFrequencyMessage(player, newFreq);
                    }
                }
                return ItemInteractionResult.sidedSuccess(level.isClientSide);
            }
        }

        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }

    // Helper to send a formatted chat message when the frequency changes
    private void sendFrequencyMessage(Player player, Frequency freq) {
        var msg = Component.translatable("enderbotanypots.chat.frequency_changed");
        msg.append(Component.literal(": "));
        msg.append(freq.getTooltip());

        if (freq.hasOwner()) {
            msg.append(Component.literal(" ("));
            freq.ownerName().ifPresent(msg::append);
            msg.append(Component.literal(")"));
        }

        player.sendSystemMessage(msg);
    }

    // Adds frequency and owner information as well as a general small text
    private static final Component TOOLTIP_NORMAL = Component.translatable("enderbotanypots.tooltip.enderpot").withStyle(ChatFormatting.GRAY);

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flagIn) {
        tooltip.add(TOOLTIP_NORMAL);

        Frequency frequency = Frequency.readFromStack(stack);
        frequency.ownerName().ifPresent(tooltip::add);
        tooltip.add(frequency.getTooltip());
        super.appendHoverText(stack, context, tooltip, flagIn);
    }
}
