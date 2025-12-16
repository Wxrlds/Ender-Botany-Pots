package eu.wxrlds.enderbotanypots.compat.top;

import codechicken.enderstorage.api.Frequency;
import eu.wxrlds.enderbotanypots.EnderBotanyPots;
import eu.wxrlds.enderbotanypots.block.BlockEnderBotanyPot;
import eu.wxrlds.enderbotanypots.block.TileEnderBotanyPot;
import mcjty.theoneprobe.api.*;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

import java.util.function.Function;

public class EnderBotanyPotsTOPPlugin implements Function<ITheOneProbe, Void>, IProbeInfoProvider {

    @Override
    public Void apply(ITheOneProbe probe) {
        probe.registerProvider(this);
        return null;
    }

    @Override
    public String getID() {
        return EnderBotanyPots.MOD_ID + ":top_plugin";
    }

    @Override
    public void addProbeInfo(ProbeMode mode, IProbeInfo info, PlayerEntity player, World world, BlockState state, IProbeHitData hit) {
        if (state.getBlock() instanceof BlockEnderBotanyPot) {
            TileEntity tile = world.getBlockEntity(hit.getPos());
            if (tile instanceof TileEnderBotanyPot) {
                TileEnderBotanyPot pot = (TileEnderBotanyPot) tile;
                Frequency freq = pot.getFrequency();

                // Frequency Line
                TranslationTextComponent freqText = new TranslationTextComponent("enderbotanypots.tooltip.frequency");
                freqText.append(new StringTextComponent(": "));
                freqText.append(freq.getTooltip());
                info.text(freqText);

                // Owner Line
                if (freq.hasOwner()) {
                    TranslationTextComponent ownerText = new TranslationTextComponent("enderbotanypots.tooltip.owner");
                    ownerText.append(new StringTextComponent(": "));
                    ownerText.append(freq.getOwnerName());
                    info.text(ownerText);
                }
            }
        }
    }
}