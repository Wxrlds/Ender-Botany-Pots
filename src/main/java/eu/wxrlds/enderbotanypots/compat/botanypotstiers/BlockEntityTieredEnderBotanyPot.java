package eu.wxrlds.enderbotanypots.compat.botanypotstiers;

import com.ultramega.botanypotstiers.PotTiers;
import eu.wxrlds.enderbotanypots.block.BlockEntityEnderBotanyPot;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class BlockEntityTieredEnderBotanyPot extends BlockEntityEnderBotanyPot {

    private final PotTiers tier;

    public BlockEntityTieredEnderBotanyPot(PotTiers tier, BlockPos pos, BlockState state) {
        super(pos, state);
        this.tier = tier;
        this.refreshRandom();
    }


    // Main growth logic
    public static void tick(Level level, BlockPos pos, BlockState state, BlockEntityEnderBotanyPot basePot) {
        if (basePot instanceof BlockEntityTieredEnderBotanyPot pot) {
            // Tick the Pot
            BlockEntityTieredEnderBotanyPot.tickPot(level, pos, state, pot);

            // Tick the Pot again, but apply the speed boost
            // This is just copy and paste from original Botany Pots
            if (!pot.doneGrowing && pot.areGrowthConditionsMet()) {
                // Speed minus one because the original tickPot already increased the growth by one
                pot.growthTime += pot.tier.getSpeed() - 1;

                pot.prevComparatorLevel = pot.comparatorLevel;
                pot.comparatorLevel = Mth.floor(15f * ((float) pot.growthTime / pot.getInventory().getRequiredGrowthTime()));

                final boolean finishedGrowing = pot.growthTime >= pot.getInventory().getRequiredGrowthTime();

                if (pot.doneGrowing != finishedGrowing) {
                    pot.doneGrowing = finishedGrowing;
                    pot.markDirty();
                }
            }

            // Custom auto-harvest logic for Ender Storage.
            // We need to overwrite it since the auto-harvest
            // logic is tied to the pot being a hopper
            if (!level.isClientSide && pot.areGrowthConditionsMet() && pot.isCropHarvestable()) {
                pot.attemptEnderHarvest(pot.tier.getMultiplier());
            }
        }


    }
}
