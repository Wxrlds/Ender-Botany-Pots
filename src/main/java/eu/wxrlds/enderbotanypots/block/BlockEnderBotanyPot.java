package eu.wxrlds.enderbotanypots.block;


import codechicken.enderstorage.api.Frequency;
import eu.wxrlds.enderbotanypots.EnderBotanyPots;
import eu.wxrlds.enderbotanypots.util.EnderBotanyPotHelper;
import net.darkhax.botanypots.block.BlockBotanyPot;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;


public class BlockEnderBotanyPot extends BlockBotanyPot {

    public BlockEnderBotanyPot() {
        // False since we don't want a hopper variant.
        super(false);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntityEnderBotanyPot(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        // Use our custom tick method
        return createTickerHelper(type, EnderBotanyPots.ENDER_BOTANY_POT_TILE.get(), BlockEntityEnderBotanyPot::tick);
    }

    // Places the block and uses the frequency of the items NBT
    // Overwrites the items NBT if the player is holding the item in the off-hand
    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);

        BlockEntity tile = level.getBlockEntity(pos);
        if (tile instanceof BlockEntityEnderBotanyPot pot && placer != null) {

            // We store the original frequency to later determine if the frequency changed and to send the chat message
            Frequency originalFreq = Frequency.readFromStack(stack);
            Frequency freqToSet = originalFreq;
            boolean copiedFromHand = false;

            ItemStack mainHandStack = placer.getItemInHand(InteractionHand.MAIN_HAND);
            ItemStack offHandStack = placer.getItemInHand(InteractionHand.OFF_HAND);

            // Copy the frequency of the off-hand item onto the botany pot
            if (mainHandStack.getItem() == this.asItem() && EnderBotanyPotHelper.isValidFrequencyItem(offHandStack)) {
                freqToSet = Frequency.readFromStack(offHandStack);
                copiedFromHand = true;
            }

            // Apply the frequency to the new block
            pot.setFrequency(freqToSet);

            // Notify the player that a new frequency has been set from the off-hand
            boolean isDifferent = !originalFreq.toString().equals(freqToSet.toString());
            if (copiedFromHand && isDifferent && !level.isClientSide && placer instanceof Player) {
                sendFrequencyMessage((Player) placer, freqToSet);
            }
        }
    }

    // Right click interaction with other Ender Storage items
    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack stack = player.getItemInHand(hand);

        // Only continues if the player is holding an Ender Pouch/Chest/Tank/Pot
        if (EnderBotanyPotHelper.isValidFrequencyItem(stack)) {
            BlockEntity tile = level.getBlockEntity(pos);
            if (tile instanceof BlockEntityEnderBotanyPot pot) {
                Frequency currentFreq = pot.getFrequency();

                // Security Check: If the pot has an owner, only THAT owner can change it.
                if (currentFreq.hasOwner() && !player.getUUID().equals(currentFreq.getOwner())) {
                    if (!level.isClientSide) {
                        player.sendSystemMessage(Component.translatable("enderbotanypots.chat.not_owner").withStyle(ChatFormatting.RED));
                    }
                    return InteractionResult.FAIL;
                }

                Frequency newFreq = Frequency.readFromStack(stack);

                // Only update the frequency, if it is actually different
                if (!currentFreq.toString().equals(newFreq.toString())) {
                    if (!level.isClientSide) {
                        pot.setFrequency(newFreq);
                        sendFrequencyMessage(player, newFreq);
                    }
                }

                return InteractionResult.sidedSuccess(level.isClientSide);
            }
        }

        return super.use(state, level, pos, player, hand, hit);
    }

    // Helper to send a formatted chat message when the frequency changes
    private void sendFrequencyMessage(Player player, Frequency freq) {
        var msg = Component.translatable("enderbotanypots.chat.frequency_changed");
        msg.append(Component.literal(": "));
        msg.append(freq.getTooltip());

        if (freq.hasOwner()) {
            msg.append(Component.literal(" ("));
            msg.append(freq.getOwnerName());
            msg.append(Component.literal(")"));
        }

        player.sendSystemMessage(msg);
    }

    // Adds frequency and owner information as well as a general small text
    private static final Component TOOLTIP_NORMAL = Component.translatable("enderbotanypots.tooltip.enderpot").withStyle(ChatFormatting.GRAY);

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter level, List<Component> tooltip, TooltipFlag flagIn) {
        tooltip.add(TOOLTIP_NORMAL);

        Frequency frequency = Frequency.readFromStack(stack);
        if (frequency.hasOwner()) {
            tooltip.add(frequency.getOwnerName());
        }

        tooltip.add(frequency.getTooltip());
    }
}
