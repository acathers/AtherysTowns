package com.atherys.towns.commands.town;

import com.atherys.core.command.ParameterizedCommand;
import com.atherys.core.command.annotation.Aliases;
import com.atherys.core.command.annotation.Description;
import com.atherys.core.command.annotation.Permission;
import com.atherys.towns.commands.TownsCommand;
import com.atherys.towns.messaging.TownMessage;
import com.atherys.towns.nation.Nation;
import com.atherys.towns.plot.Plot;
import com.atherys.towns.plot.PlotDefinition;
import com.atherys.towns.resident.Resident;
import com.atherys.towns.town.Town;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.Chunk;

import javax.annotation.Nullable;
import java.util.Optional;

@Aliases("claim")
@Description("Used to claim new plots for your town using the plot tool.")
@Permission("atherystowns.town.claim")
public class TownClaimCommand extends TownsCommand implements ParameterizedCommand {

    @Override
    protected CommandResult execute(Player player, CommandContext args, Resident resident,
                                    @Nullable Town town, @Nullable Nation nation) {
        PlotDefinition define;

        if (town != null) {

            if (!args.hasAny("claimChunk?")) {
                Optional<PlotDefinition> definitionOptional;

                try {
                    definitionOptional = PlotDefinition.fromPlayer(player, town);
                } catch (PlotDefinition.DefinitionNotValidException | PlotDefinition.DefinitionNotPresentException e) {
                    return CommandResult.empty();
                }

                if (definitionOptional.isPresent()) {
                    define = definitionOptional.get();
                } else {
                    TownMessage.warn(player, Text.of("Definition invalid. Claim cancelled."));
                    return CommandResult.empty();
                }

            } else {
                // claim chunk
                Optional<Chunk> chunk = player.getLocation().getExtent()
                        .getChunkAtBlock(player.getLocation().getBlockPosition());
                if (chunk.isPresent()) {
                    try {
                        Optional<PlotDefinition> defineOpt = PlotDefinition
                                .fromChunk(player, town, chunk.get());
                        if (defineOpt.isPresent()) {
                            define = defineOpt.get();
                        } else {
                            TownMessage.warn(player, Text.of("Could not create plot from chunk."));
                            return CommandResult.empty();
                        }
                    } catch (PlotDefinition.DefinitionNotValidException | PlotDefinition.DefinitionNotPresentException e) {
                        return CommandResult.empty();
                    }
                } else {
                    TownMessage.warn(player,
                            "Plugin could not find chunk. You must therefore create a plot definition.");
                    return CommandResult.empty();
                }
            }

            if (define.area() + town.getArea() > town.getMaxSize()) {
                TownMessage
                        .warn(player, "Your town cannot grow any larger than ", town.getMaxSize(),
                                " blocks in area!");
                return CommandResult.empty();
            }

            Plot p = Plot.create(define, town, "None");
            town.claimPlot(p);
            TownMessage.inform(player, "Plot Claimed.");
            return CommandResult.success();
        }
        return CommandResult.empty();
    }

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[]{
                GenericArguments.optional(GenericArguments.string(Text.of("claimChunk?")))
        };
    }
}