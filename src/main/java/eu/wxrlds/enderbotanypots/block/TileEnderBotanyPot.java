package eu.wxrlds.enderbotanypots.block;

import codechicken.enderstorage.api.Frequency;
import codechicken.enderstorage.manager.EnderStorageManager;
import codechicken.enderstorage.storage.EnderItemStorage;
import eu.wxrlds.enderbotanypots.EnderBotanyPots;
import net.darkhax.botanypots.BotanyPotHelper;
import net.darkhax.botanypots.api.events.BotanyPotHarvestedEvent;
import net.darkhax.botanypots.block.tileentity.TileEntityBotanyPot;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.wrapper.InvWrapper;

import java.util.List;

public class TileEnderBotanyPot extends TileEntityBotanyPot {

    private Frequency frequency = new Frequency();
    private List<ItemStack> dropsCache = null;

    public TileEnderBotanyPot() {
        super();
    }

    @Override
    public TileEntityType<?> getType() {
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
    public void load(BlockState state, CompoundNBT tag) {
        super.load(state, tag);
        if (tag.contains("Frequency")) {
            frequency = new Frequency(tag.getCompound("Frequency"));
        }
        // We do not store cached drops and just accept a re-roll, just like Botany Pots does
    }


    @Override
    public CompoundNBT save(CompoundNBT tag) {
        super.save(tag);
        tag.put("Frequency", frequency.writeToNBT(new CompoundNBT()));
        return tag;
    }

    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        return new SUpdateTileEntityPacket(this.worldPosition, -1, this.getUpdateTag());
    }

    @Override
    public CompoundNBT getUpdateTag() {
        return this.save(new CompoundNBT());
    }

    // What happens when receiving a packet
    // We need this for HWYLA/Jade to show the correct owner after changing the frequency
    // TOP is running server side so this is not needed
    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
        super.onDataPacket(net, pkt);
        this.load(this.getBlockState(), pkt.getTag());
    }

    // Main growth logic
    @Override
    public void onTileTick() {
        super.onTileTick();
        // Custom auto-harvest logic for Ender Storage.
        // Since our pot is not actually a hopper pot,
        // we don't have to worry about the pot being auto-harvested by super
        if (!this.level.isClientSide && this.hasSoilAndCrop() && this.isDoneGrowing()) {
            this.attemptEnderHarvest();
        }
    }

    // This is just copy and paste from Botany Pots. We need it here for the dropsCache
    private List<ItemStack> getDrops() {
        if (this.dropsCache == null) {
            // Generate new drops from the Loot Table
            BotanyPotHarvestedEvent.LootGenerated event = new BotanyPotHarvestedEvent.LootGenerated(
                    this,
                    null,
                    BotanyPotHelper.generateDrop(this.level.random, this.getCrop())
            );

            if (!MinecraftForge.EVENT_BUS.post(event)) {
                this.dropsCache = event.getDrops();
            } else {
                this.dropsCache = NonNullList.create();
            }
        }
        return this.dropsCache;
    }

    // Attempts to harvest the crop and send it to Ender Storage
    private void attemptEnderHarvest() {
        // Fire a Botany Pot harvested event (just like the original Botany Pot does)
        if (MinecraftForge.EVENT_BUS.post(new BotanyPotHarvestedEvent.Pre(this, null))) {
            return;
        }

        // Get the Ender Storage inventory for the current frequency
        EnderItemStorage storage = EnderStorageManager.instance(false).getStorage(frequency, EnderItemStorage.TYPE);
        if (storage == null) return;

        // Generate the drops
        List<ItemStack> drops = this.getDrops();

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
            this.onCropHarvest();
            this.resetGrowthTime();
            this.dropsCache = null;
            MinecraftForge.EVENT_BUS.post(new BotanyPotHarvestedEvent.Post(this, null));
        }
    }
}