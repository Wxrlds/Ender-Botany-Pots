package eu.wxrlds.enderbotanypots.block;


import codechicken.enderstorage.api.Frequency;
import codechicken.enderstorage.manager.EnderStorageManager;
import codechicken.enderstorage.storage.EnderItemStorage;
import eu.wxrlds.enderbotanypots.EnderBotanyPots;
import net.darkhax.bookshelf.common.api.data.enchantment.EnchantmentLevel;
import net.darkhax.bookshelf.common.api.service.Services;
import net.darkhax.botanypots.common.api.context.BlockEntityContext;
import net.darkhax.botanypots.common.api.data.recipes.crop.Crop;
import net.darkhax.botanypots.common.api.data.recipes.soil.Soil;
import net.darkhax.botanypots.common.impl.BotanyPotsMod;
import net.darkhax.botanypots.common.impl.Helpers;
import net.darkhax.botanypots.common.impl.block.entity.BotanyPotBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.wrapper.InvWrapper;

import java.util.function.Supplier;

public class EnderBotanyPotBlockEntity extends BotanyPotBlockEntity {

    private Frequency frequency = new Frequency();
    private int bonemealCooldown = 0;
    private EnderItemStorage cachedEnderStorage;

    public EnderBotanyPotBlockEntity(BlockPos pos, BlockState state) {
        super((Supplier) EnderBotanyPots.ENDER_BOTANY_POT_TILE, pos, state);
    }

    // Main growth logic
    // Copied from BotanyPotBlockEntity#tickPot
    // Removed waxed check because the Ender Botany Pot does not have a waxed variant
    public static void tick(Level level, BlockPos pos, BlockState state, EnderBotanyPotBlockEntity pot) {
        if (pot.isRemoved() || pot.level == null) {
            return;
        }
        final BlockEntityContext context = pot.getRecipeContext();
        if (pot.bonemealCooldown > 0) {
            pot.bonemealCooldown--;
        }

        // Update soil
        final Soil soil = pot.getOrInvalidateSoil();
        if (soil != null) {
            soil.onTick(context, level);
        }

        // Update crop
        final Crop crop = pot.getOrInvalidateCrop();
        if (crop != null) {
            crop.onTick(context, level);

            if (pot.growCooldown.getTicks() > 0) {
                pot.growCooldown.tickDown(level);
            }

            if (pot.growCooldown.getTicks() <= 0 && crop.isGrowthSustained(context, level)) {
                pot.growthTime.tickUp(level);
                crop.onGrowthTick(context, level);

                final int requiredGrowthTicks = Helpers.getRequiredGrowthTicks(context, pot.level, crop, soil);

                if (pot.growthTime.getTicks() >= requiredGrowthTicks) {
                    pot.updateComparatorLevel(15);
                    pot.growCooldown.setTicks(5f);

                    // Auto-Harvest logic
                    if (pot.isHopper() && crop.canHarvest(context, level)) {
                        if (level instanceof ServerLevel serverLevel) {
                            final int rolls = Helpers.getLootRolls(context, level, crop, soil);
                            for (int roll = 0; roll < rolls; roll++) {
                                // Harvest to internal inventory
                                crop.onHarvest(context, level, stack -> Services.GAMEPLAY.addItem(stack, pot.getItems(), BotanyPotBlockEntity.STORAGE_SLOTS));
                            }
                            // Tool Damage Logic
                            if (BotanyPotsMod.CONFIG.get().gameplay.damage_harvest_tool && EnchantmentLevel.FIRST.get(Helpers.NEGATE_HARVEST_DAMAGE_TAG, pot.getHarvestItem()) <= 0) {
                                pot.getHarvestItem().hurtAndBreak(1, serverLevel, null, stack -> {
                                });
                            }

                            level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(pot.getBlockState()));
                        }
                        pot.growthTime.reset();
                    }
                } else {
                    pot.updateComparatorLevel(Mth.ceil(14f * (pot.growthTime.getTicks() / requiredGrowthTicks)));
                }
            }
        }
        // Move output to Ender Storage
        if (pot.isHopper()) {
            pot.exportCooldown.tickDown(level);
            if (pot.exportCooldown.getTicks() <= 0) {
                if (level instanceof ServerLevel) {
                    // Try to export to Ender Storage
                    if (pot.cachedEnderStorage == null) {
                        pot.cachedEnderStorage = EnderStorageManager.instance(false).getStorage(pot.frequency, EnderItemStorage.TYPE);
                    }
                    IItemHandler enderHandler = new InvWrapper(pot.cachedEnderStorage);
                    boolean inventoryChanged = false;

                    for (int slot : BotanyPotBlockEntity.STORAGE_SLOTS) {
                        final ItemStack stack = pot.getItem(slot);
                        if (!stack.isEmpty()) {
                            // Try insert into Ender Storage
                            ItemStack remaining = ItemHandlerHelper.insertItemStacked(enderHandler, stack, false);
                            if (remaining.getCount() != stack.getCount()) {
                                pot.setItem(slot, remaining);
                                inventoryChanged = true;
                            }
                        }
                    }
                    // If we moved items, mark pot as changed
                    if (inventoryChanged) {
                        pot.setChanged();
                    }
                }
                pot.exportCooldown.reset();
            }
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
        // Invalidate cache when it changes
        this.cachedEnderStorage = null;
        if (this.level != null) {
            // And sends that information to the client
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }

    @Override
    public void setBonemealCooldown(int cooldown) {
        // Update parent to keep internal consistency if possible
        super.setBonemealCooldown(cooldown);
        // Update our local shadow
        this.bonemealCooldown = cooldown;
    }

    @Override
    public boolean canBonemeal() {
        return this.bonemealCooldown <= 0;
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("Frequency")) {
            this.frequency = Frequency.CODEC.parse(NbtOps.INSTANCE, tag.get("Frequency")).result().orElse(new Frequency());
        }
        if (tag.contains("bonemeal_cooldown")) {
            this.bonemealCooldown = tag.getInt("bonemeal_cooldown");
        }
    }

    @Override
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        Frequency.CODEC.encodeStart(NbtOps.INSTANCE, frequency).resultOrPartial(err -> {
        }).ifPresent(freqTag -> tag.put("Frequency", freqTag));
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        saveAdditional(tag, registries);
        return tag;
    }

    // What happens when receiving a packet
    // We need this for HWYLA/Jade to show the correct owner after changing the frequency
    // TOP is running server side so this is not needed
    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt, HolderLookup.Provider lookupProvider) {
        super.onDataPacket(net, pkt, lookupProvider);
        if (pkt.getTag() != null) {
            this.loadAdditional(pkt.getTag(), lookupProvider);
        }
    }
}
