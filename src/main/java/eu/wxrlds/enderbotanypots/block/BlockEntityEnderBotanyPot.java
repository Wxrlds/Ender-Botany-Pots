package eu.wxrlds.enderbotanypots.block;


import codechicken.enderstorage.api.Frequency;
import codechicken.enderstorage.manager.EnderStorageManager;
import codechicken.enderstorage.storage.EnderItemStorage;
import eu.wxrlds.enderbotanypots.EnderBotanyPots;
import net.darkhax.botanypots.BotanyPotHelper;
import net.darkhax.botanypots.block.BlockEntityBotanyPot;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.wrapper.InvWrapper;

import java.util.List;
import java.util.Random;


public class BlockEntityEnderBotanyPot extends BlockEntityBotanyPot {
    private Frequency frequency = new Frequency();

    // We need our own random, since the one from Botany Pots is not public
    final Random rng = new Random();
    private long rngSeed;

    public BlockEntityEnderBotanyPot(BlockPos pos, BlockState state) {
        super(EnderBotanyPots.ENDER_BOTANY_POT_TILE.get(), pos, state);
        this.refreshRandom();
    }

    public void refreshRandom() {
        if (this.rng != null) {
            this.rngSeed = new Random().nextLong();
            this.rng.setSeed(this.rngSeed);
        }
    }

    @Override
    public BlockEntityType<?> getType() {
        return EnderBotanyPots.ENDER_BOTANY_POT_TILE.get();
    }

    public Frequency getFrequency() {
        return frequency;
    }

    // Sets the frequency
    public void setFrequency(Frequency frequency) {
        this.frequency = frequency;
        this.setChanged();
        if (this.level != null) {
            // And sends that information to the client
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }


    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("Frequency")) {
            frequency = new Frequency(tag.getCompound("Frequency"));
        }
        if (tag.contains("EnderRandomSeed")) {
            this.rngSeed = tag.getLong("EnderRandomSeed");
        }
    }


    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Frequency", frequency.writeToNBT(new CompoundTag()));
        tag.putLong("EnderRandomSeed", this.rngSeed);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        saveAdditional(tag);
        return tag;
    }

    // What happens when receiving a packet
    // We need this for HWYLA/Jade to show the correct owner after changing the frequency
    // TOP is running server side so this is not needed
    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        super.onDataPacket(net, pkt);
        if (pkt.getTag() != null) {
            this.load(pkt.getTag());
        }
    }


    // Main growth logic
    public static void tick(Level level, BlockPos pos, BlockState state, BlockEntityEnderBotanyPot pot) {
        // Tick the Pot
        BlockEntityEnderBotanyPot.tickPot(level, pos, state, pot);

        // Custom auto-harvest logic for Ender Storage.
        // We need to overwrite it since the auto-harvest
        // logic is tied to the pot being a hopper
        if (!level.isClientSide && pot.areGrowthConditionsMet() && pot.isCropHarvestable()) {
            pot.attemptEnderHarvest();
        }
    }


    @Override
    public void resetGrowth() {
        super.resetGrowth();
        // Rotate the seed for the next cycle
        this.refreshRandom();
        this.setChanged();
    }


    // Attempts to harvest the crop and send it to Ender Storage
    private void attemptEnderHarvest() {
        // Get the Ender Storage inventory for the current frequency
        EnderItemStorage storage = EnderStorageManager.instance(false).getStorage(frequency, EnderItemStorage.TYPE);
        if (storage == null) return;

        // Generate the drops
        this.rng.setSeed(this.rngSeed);
        List<ItemStack> drops = BotanyPotHelper.generateDrop(rng, this.level, this.getBlockPos(), this, this.getCrop());

        // If no drops were generated, we consider the harvest successful
        boolean didHarvest = drops.isEmpty();

        // Wrap the EnderStorage in an IItemHandler for easy insertion
        IItemHandler itemHandler = new InvWrapper(storage);

        for (ItemStack stack : drops) {
            ItemStack remaining = ItemHandlerHelper.insertItemStacked(itemHandler, stack, false);

            // If we managed to insert anything, we successfully harvested
            if (remaining.getCount() < stack.getCount()) {
                didHarvest = true;
            }
        }

        // Reset the crop growth if it was successful
        if (didHarvest) {
            this.resetGrowth();
        }
    }
}