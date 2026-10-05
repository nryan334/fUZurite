package me.keano.azurite.modules.teams.commands.systeam;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.framework.commands.Command;
import me.keano.azurite.modules.teams.commands.systeam.args.*;

import java.util.Arrays;
import java.util.List;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class SysTeamCommand extends Command {

    public SysTeamCommand(CommandManager manager) {
        super(
                manager,
                "systemteam"
        );
        this.setPermissible("azurite.systeam");
        this.handleArguments(
                new SysTeamDeleteArg(manager),
                new SysTeamCreateArg(manager),
                new SysTeamClaimArg(manager),
                new SysTeamSetHqArg(manager),
                new SysTeamUnclaimArg(manager),
                new SysTeamAbilityArg(manager),
                new SysTeamListArg(manager),
                new SysTeamSetColorArg(manager)
        );
    }

    @Override
    public List<String> aliases() {
        return Arrays.asList(
                "systeam",
                "sysfac",
                "sysfaction",
                "st",
                "sf"
        );
    }

    @Override
    public List<String> usage() {
        return getLanguageConfig().getStringList("SYSTEM_TEAM_COMMAND.USAGE");
    }
}