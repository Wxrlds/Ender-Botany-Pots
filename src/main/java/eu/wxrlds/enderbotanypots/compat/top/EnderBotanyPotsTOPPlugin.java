package eu.wxrlds.enderbotanypots.compat.top;


import codechicken.enderstorage.api.Frequency;
import eu.wxrlds.enderbotanypots.EnderBotanyPots;
import eu.wxrlds.enderbotanypots.block.BlockEnderBotanyPot;
import eu.wxrlds.enderbotanypots.block.BlockEntityEnderBotanyPot;
import mcjty.theoneprobe.api.*;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Function;


public class EnderBotanyPotsTOPPlugin implements Function<ITheOneProbe, Void>, IProbeInfoProvider {

    @Override
    public Void apply(ITheOneProbe probe) {
        probe.registerProvider(this);
        return null;
    }

    @Override
    public ResourceLocation getID() {
        return ResourceLocation.fromNamespaceAndPath(EnderBotanyPots.MOD_ID, "top_plugin");
    }

    @Override
    public void addProbeInfo(ProbeMode mode, IProbeInfo info, Player player, Level level, BlockState state, IProbeHitData hit) {
        if (state.getBlock() instanceof BlockEnderBotanyPot) {
            BlockEntity tile = level.getBlockEntity(hit.getPos());
            if (tile instanceof BlockEntityEnderBotanyPot pot) {
                Frequency freq = pot.getFrequency();

                // Frequency Line
                TranslatableComponent freqText = new TranslatableComponent("enderbotanypots.tooltip.frequency");
                freqText.append(new TextComponent(": "));
                freqText.append(freq.getTooltip());
                info.text(freqText);

                // Owner Line
                if (freq.hasOwner()) {
                    TranslatableComponent ownerText = new TranslatableComponent("enderbotanypots.tooltip.owner");
                    ownerText.append(new TextComponent(": "));
                    ownerText.append(freq.getOwnerName());
                    info.text(ownerText);
                }
            }
        }
    }
}