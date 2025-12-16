package eu.wxrlds.enderbotanypots.block;


import codechicken.enderstorage.api.Frequency;
import eu.wxrlds.enderbotanypots.util.EnderBotanyPotHelper;
import net.darkhax.botanypots.block.BlockBotanyPot;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
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
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new TileEnderBotanyPot();
    }

    // Places the block and uses the frequency of the items NBT
    // Overwrites the items NBT if the player is holding the item in the off-hand
    @Override
    public void setPlacedBy(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(world, pos, state, placer, stack);

        TileEntity tile = world.getBlockEntity(pos);
        if (tile instanceof TileEnderBotanyPot && placer != null) {
            TileEnderBotanyPot pot = (TileEnderBotanyPot) tile;

            // We store the original frequency to later determine if the frequency changed and to send the chat message
            Frequency originalFreq = Frequency.readFromStack(stack);
            Frequency freqToSet = originalFreq;
            boolean copiedFromHand = false;

            ItemStack mainHandStack = placer.getItemInHand(Hand.MAIN_HAND);
            ItemStack offHandStack = placer.getItemInHand(Hand.OFF_HAND);

            // Copy the frequency of the off-hand item onto the botany pot
            if (mainHandStack.getItem() == this.asItem() && EnderBotanyPotHelper.isValidFrequencyItem(offHandStack)) {
                freqToSet = Frequency.readFromStack(offHandStack);
                copiedFromHand = true;
            }

            // Apply the frequency to the new block
            pot.setFrequency(freqToSet);

            // Notify the player that a new frequency has been set from the off-hand
            boolean isDifferent = !originalFreq.toString().equals(freqToSet.toString());
            if (copiedFromHand && isDifferent && !world.isClientSide && placer instanceof PlayerEntity) {
                sendFrequencyMessage((PlayerEntity) placer, freqToSet);
            }
        }
    }

    // Right click interaction with other Ender Storage items
    @Override
    public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
        ItemStack stack = player.getItemInHand(hand);

        // Only continues if the player is holding an Ender Pouch/Chest/Tank/Pot
        if (EnderBotanyPotHelper.isValidFrequencyItem(stack)) {
            TileEntity tile = world.getBlockEntity(pos);
            if (tile instanceof TileEnderBotanyPot) {
                TileEnderBotanyPot pot = (TileEnderBotanyPot) tile;
                Frequency currentFreq = pot.getFrequency();

                // Security Check: If the pot has an owner, only THAT owner can change it.
                if (currentFreq.hasOwner() && !player.getUUID().equals(currentFreq.getOwner())) {
                    if (!world.isClientSide) {
                        player.sendMessage(new TranslationTextComponent("enderbotanypots.chat.not_owner").withStyle(TextFormatting.RED), Util.NIL_UUID);
                    }
                    return ActionResultType.FAIL;
                }

                Frequency newFreq = Frequency.readFromStack(stack);

                // Only update the frequency, if it is actually different
                if (!currentFreq.toString().equals(newFreq.toString())) {
                    if (!world.isClientSide) {
                        pot.setFrequency(newFreq);
                        sendFrequencyMessage(player, newFreq);
                    }
                }

                return ActionResultType.sidedSuccess(world.isClientSide);
            }
        }

        return super.use(state, world, pos, player, hand, hit);
    }

    // Helper to send a formatted chat message when the frequency changes
    private void sendFrequencyMessage(PlayerEntity player, Frequency freq) {
        TranslationTextComponent msg = new TranslationTextComponent("enderbotanypots.chat.frequency_changed");
        msg.append(new StringTextComponent(": "));
        msg.append(freq.getTooltip());

        if (freq.hasOwner()) {
            msg.append(new StringTextComponent(" ("));
            msg.append(freq.getOwnerName());
            msg.append(new StringTextComponent(")"));
        }

        player.sendMessage(msg, Util.NIL_UUID);
    }

    // Adds frequency and owner information as well as a general small text
    private static final ITextComponent TOOLTIP_NORMAL = new TranslationTextComponent("enderbotanypots.tooltip.enderpot").withStyle(TextFormatting.GRAY);

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable IBlockReader worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        tooltip.add(TOOLTIP_NORMAL);

        Frequency frequency = Frequency.readFromStack(stack);
        if (frequency.hasOwner()) {
            tooltip.add(frequency.getOwnerName());
        }

        tooltip.add(frequency.getTooltip());
    }
}
