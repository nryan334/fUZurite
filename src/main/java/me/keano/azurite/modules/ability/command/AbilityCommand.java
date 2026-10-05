package me.keano.azurite.modules.ability.command;

import me.keano.azurite.modules.ability.Ability;
import me.keano.azurite.modules.ability.menu.AbilityToggleMenu;
import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.commands.Command;
import me.keano.azurite.modules.framework.commands.extra.TabCompletion;
import me.keano.azurite.utils.ItemUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class AbilityCommand extends Command {

    public AbilityCommand(CommandManager manager) {
        super(
                manager,
                "ability"
        );
        this.setPermissible("azurite.ability");
        this.completions.add(new TabCompletion(Arrays.asList("give", "list", "toggle", "getall"), 0));
    }

    @Override
    public List<String> aliases() {
        return Collections.emptyList();
    }

    @Override
    public List<String> usage() {
        return getLanguageConfig().getStringList("ABILITY_COMMAND.USAGE");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sendUsage(sender);
            return;
        }

        switch (args[0].toLowerCase()) {
            case "give":
                if (args.length < 4) {
                    sendMessage(sender, getLanguageConfig().getString("ABILITY_COMMAND.ABILITY_GIVE.USAGE"));
                    return;
                }

                Player target = Bukkit.getPlayer(args[1]);
                Ability ability = getInstance().getAbilityManager().getAbility(args[2]);
                Integer amount = getInt(args[3]);

                if (target == null) {
                    sendMessage(sender, Config.PLAYER_NOT_FOUND
                            .replace("%player%", args[1])
                    );
                    return;
                }

                if (ability == null) {
                    sendMessage(sender, getLanguageConfig().getString("ABILITY_COMMAND.ABILITY_GIVE.NOT_FOUND")
                            .replace("%ability%", args[1])
                    );
                    return;
                }

                if (amount == null) {
                    sendMessage(sender, Config.NOT_VALID_NUMBER
                            .replace("%number%", args[3])
                    );
                    return;
                }

                ItemStack itemStack = ability.getItem().clone();
                itemStack.setAmount(amount);
                ItemUtils.giveItem(target, itemStack, target.getLocation());
                sendMessage(sender, getLanguageConfig().getString("ABILITY_COMMAND.ABILITY_GIVE.GAVE")
                        .replace("%player%", target.getName())
                        .replace("%amount%", String.valueOf(amount))
                        .replace("%ability%", ability.getName())
                );
                return;

            case "list":
                for (String s : getLanguageConfig().getStringList("ABILITY_COMMAND.ABILITY_LIST.ABILITIES")) {
                    if (!s.equalsIgnoreCase("%abilities%")) {
                        sendMessage(sender, s);
                        continue;
                    }

                    for (Ability abili : getInstance().getAbilityManager().getAbilities().values()) {
                        sendMessage(sender, getLanguageConfig().getString("ABILITY_COMMAND.ABILITY_LIST.ABILITY_FORMAT")
                                .replace("%ability%", abili.getName().replaceAll(" ", ""))
                                .replace("%cooldown%", String.valueOf(abili.getAbilityCooldown().getSeconds()))
                        );
                    }
                }
                return;

            case "toggle":
                if (!(sender instanceof Player)) {
                    sendMessage(sender, Config.PLAYER_ONLY);
                    return;
                }

                Player player = (Player) sender;
                new AbilityToggleMenu(getInstance().getMenuManager(), player).open();
                return;

            case "getall":
                if (!(sender instanceof Player)) {
                    sendMessage(sender, Config.PLAYER_ONLY);
                    return;
                }

                for (Ability add : getInstance().getAbilityManager().getAbilities().values()) {
                    ((Player) sender).getInventory().addItem(add.getItem());
                }
                return;
        }

        sendUsage(sender);
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 3) {
            String string = args[args.length - 1];
            return getInstance().getAbilityManager().getAbilities().keySet()
                    .stream()
                    .filter(s -> s.regionMatches(true, 0, string, 0, string.length()))
                    .collect(Collectors.toList());
        }

        return super.tabComplete(sender, args);
    }
}