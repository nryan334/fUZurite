package me.keano.azurite.modules.teams.commands.team.args;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.commands.Argument;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;

/**
 * /f editor - Opens the GUI faction editor for system teams.
 */
public class TeamEditorArg extends Argument {

    public TeamEditorArg(CommandManager manager) {
        super(
                manager,
                Collections.singletonList("editor")
        );
        this.setPermissible("azurite.systeam");
    }

    @Override
    public String usage() {
        return "/f editor";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sendMessage(sender, Config.PLAYER_ONLY);
            return;
        }

        Player player = (Player) sender;
        getInstance().getFactionEditorManager().openMainMenu(player);
    }
}
