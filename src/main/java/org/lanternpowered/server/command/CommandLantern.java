package org.lanternpowered.server.command;

import org.spongepowered.api.command.spec.CommandSpec.Builder;
import org.spongepowered.api.plugin.PluginContainer;

import javax.annotation.Nullable;

public class CommandLantern extends CommandProvider {

    public CommandLantern(@Nullable Integer opPermissionLevel, String name, String... aliases) {
        super(opPermissionLevel, name, aliases);
    }

    @Override
    public void completeSpec(PluginContainer pluginContainer, Builder specBuilder) {

    }

}
