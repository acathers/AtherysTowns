package com.atherys.towns.commands.town.set;

import com.atherys.core.command.ParameterizedCommand;
import com.atherys.core.command.annotation.Aliases;
import com.atherys.core.command.annotation.Description;
import com.atherys.core.command.annotation.Permission;
import com.atherys.towns.commands.TownsCommand;
import com.atherys.towns.managers.ResidentManager;
import com.atherys.towns.messaging.TownMessage;
import com.atherys.towns.nation.Nation;
import com.atherys.towns.permissions.ranks.TownRank;
import com.atherys.towns.permissions.ranks.TownRankRegistry;
import com.atherys.towns.permissions.ranks.TownRanks;
import com.atherys.towns.resident.Resident;
import com.atherys.towns.town.Town;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import javax.annotation.Nullable;
import java.util.Optional;

@Aliases("rank")
@Description("Used to change the rank of a resident in the town.")
@Permission("atherystowns.town.set.rank")
public class TownSetRankCommand extends TownsCommand implements ParameterizedCommand {

    @Override
    protected CommandResult execute(Player player, CommandContext args, Resident resident,
                                    @Nullable Town town, @Nullable Nation nation) {
        if (town == null) {
            return CommandResult.empty();
        }

        Optional<Player> target = args.getOne("player");
        if (!target.isPresent()) {
            TownMessage.warn(player, "You must provide a valid player.");
            return CommandResult.empty();
        }

        Optional<Resident> targetResOpt = ResidentManager.getInstance()
                .get(target.get().getUniqueId());
        if (!targetResOpt.isPresent()) {
            TownMessage.warn(player, "You must provide a valid resident.");
            return CommandResult.empty();
        }

        Resident targetRes = targetResOpt.get();
        if (!targetRes.getTown().isPresent() || !town.equals(targetRes.getTown().get())) {
            TownMessage.warn(player, "The specified resident must be part of your town.");
            return CommandResult.empty();
        }

        String rankName = (String) args.getOne("newRank").orElse(targetRes.getTownRank().getId());
        Optional<TownRank> rank = TownRankRegistry.getInstance().getById(rankName);
        if (rank.isPresent()) {
            if (rank.get().equals(TownRanks.MAYOR)) {
                TownMessage.warn(player,
                        "You cannot set the town mayor using this command. Please use '/t set mayor'");
                return CommandResult.empty();
            }

            targetRes.setTownRank(rank.get());
            town.informResidents(
                    Text.of(targetRes.getName(), " has been given the rank of ", rank.get().getName()));
        }

        return CommandResult.success();
    }

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[]{
                GenericArguments.player(Text.of("player")),
                GenericArguments.string(Text.of("newRank"))
        };
    }
}