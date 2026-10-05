package me.keano.azurite.modules.kits.commands.kitadmin;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.framework.commands.Command;
import me.keano.azurite.modules.kits.commands.kitadmin.args.*;

import java.util.Collections;
import java.util.List;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class KitAdminCommand extends Command {

    public KitAdminCommand(CommandManager manager) {
        super(
                manager,
                "kitadmin"
        );
        this.setPermissible("azurite.kitadmin");
        this.handleArguments(
                new KitAdminCreateArg(manager),
                new KitAdminDeleteArg(manager),
                new KitAdminSetItemsArg(manager),
                new KitAdminApplyArg(manager),
                new KitAdminSetAdminCooldownArg(manager),
                new KitAdminSetNameArg(manager),
                new KitAdminListArg(manager)
        );
    }

    @Override
    public List<String> aliases() {
        return Collections.singletonList(
                "kitsadmin"
        );
    }

    @Override
    public List<String> usage() {
        return getLanguageConfig().getStringList("KIT_ADMIN_COMMAND.USAGE");
    }
}