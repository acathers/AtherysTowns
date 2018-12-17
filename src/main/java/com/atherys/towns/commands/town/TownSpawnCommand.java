package com.atherys.towns.commands.town;

import com.atherys.core.command.annotation.Aliases;
import com.atherys.core.command.annotation.Description;
import com.atherys.core.command.annotation.Permission;
import com.atherys.towns.AtherysTowns;
import com.atherys.towns.commands.TownsCommand;
import com.atherys.towns.messaging.TownMessage;
import com.atherys.towns.nation.Nation;
import com.atherys.towns.resident.Resident;
import com.atherys.towns.town.Town;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import javax.annotation.Nullable;
import java.util.concurrent.TimeUnit;

@Aliases({"spawn", "home"})
@Description("Used to teleport to the spawn location of your town. Must be part of a town.")
@Permission("atherystowns.town.spawn")
public class TownSpawnCommand extends TownsCommand {

    @Override
    protected CommandResult execute(Player player, CommandContext args, Resident resident,
                                    @Nullable Town town, @Nullable Nation nation) {
        if (town == null) {
            return CommandResult.empty();
        }

        TownMessage.inform(player, "You will be teleported home in ",
                AtherysTowns.getConfig().TOWN.SPAWN_DELAY, " seconds.");

        Location<World> spawn = town.getSpawn();
        Task.builder()
                .delay(AtherysTowns.getConfig().TOWN.SPAWN_DELAY, TimeUnit.SECONDS)
                .execute(() -> {
                    player.setLocationSafely(spawn);
                    TownMessage.inform(player, "You have returned to your town's spawn!");
                })
                .name("atherystowns-spawn-task-" + player.getName())
                .submit(AtherysTowns.getInstance());

        return CommandResult.success();
    }
}