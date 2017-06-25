package org.lanternpowered.server.command;

import static org.spongepowered.api.util.SpongeApiTranslationHelper.t;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec.Builder;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.World;

public class CommandSpawn extends CommandProvider {

    public CommandSpawn() {
        super(1,"spawn");
    }

    @Override
    public void completeSpec(PluginContainer pluginContainer, Builder specBuilder) {
        specBuilder
                .arguments(GenericArguments.flags()
                        .valueFlag(GenericArguments.world(CommandHelper.WORLD_KEY), "-world", "w")
                        .valueFlag(GenericArguments.playerOrSource(Text.of("target")), "-target", "t")
                        .buildWith(GenericArguments.none()))
                .executor((src, args) -> {
                    World world = CommandHelper.getWorld(src, args);
                    Player player = args.<Player>getOne("target").orElse(null);

                    if (player == null) {
                        throw new CommandException(t("commands.spawnteleport.failed"));
                    }

                    player.setLocation(world.getSpawnLocation());
                    src.sendMessage(t("commands.spawnteleport.success.self", player.getName(), world.getName()));
                    if (!src.equals(player)) {
                        src.sendMessage(t("commands.spawnteleport.success.other", world.getName()));
                    }

                    return CommandResult.success();
                });
    }

}
