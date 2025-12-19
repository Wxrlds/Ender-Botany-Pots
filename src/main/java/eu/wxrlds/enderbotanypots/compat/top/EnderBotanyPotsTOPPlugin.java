package eu.wxrlds.enderbotanypots.compat.top;


import codechicken.enderstorage.api.Frequency;
import eu.wxrlds.enderbotanypots.EnderBotanyPots;
import eu.wxrlds.enderbotanypots.block.EnderBotanyPotBlock;
import eu.wxrlds.enderbotanypots.block.EnderBotanyPotBlockEntity;
import mcjty.theoneprobe.api.*;
import net.minecraft.network.chat.Component;
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
        if (state.getBlock() instanceof EnderBotanyPotBlock) {
            BlockEntity tile = level.getBlockEntity(hit.getPos());
            if (tile instanceof EnderBotanyPotBlockEntity pot) {
                Frequency freq = pot.getFrequency();

                var freqText = Component.translatable("enderbotanypots.tooltip.frequency")
                        .append(Component.literal(": "))
                        .append(freq.getTooltip());
                info.text(freqText);

                if (freq.hasOwner()) {
                    var ownerText = Component.translatable("enderbotanypots.tooltip.owner")
                            .append(Component.literal(": "));
                    freq.ownerName().ifPresent(ownerText::append);
                    info.text(ownerText);
                }
            }
        }
    }
}