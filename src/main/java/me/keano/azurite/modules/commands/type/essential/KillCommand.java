package me.keano.azurite.modules.commands.type.essential;

import com.google.common.base.Functions;
import com.google.common.collect.ImmutableMap;
import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.commands.Command;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class KillCommand extends Command {

    public KillCommand(CommandManager manager) {
        super(
                manager,
                "kill"
        );
        this.setPermissible("azurite.kill");
    }

    @Override
    public List<String> aliases() {
        return Collections.emptyList();
    }

    @Override
    public List<String> usage() {
        return getLanguageConfig().getStringList("KILL_COMMAND.USAGE");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                EntityDamageEvent event = new EntityDamageEvent(
                        player, EntityDamageEvent.DamageCause.SUICIDE,
                        new EnumMap<>(ImmutableMap.of(EntityDamageEvent.DamageModifier.BASE, 100D)),
                        new EnumMap<>(ImmutableMap.of(EntityDamageEvent.DamageModifier.BASE, Functions.constant(-0.0D))));

                player.setLastDamageCause(event);
                player.setHealth(0.0D);

                sendMessage(sender, getLanguageConfig().getString("KILL_COMMAND.KILLED_SELF"));
                return;
            }

            sendUsage(sender);
            return;
        }

        Player target = Bukkit.getPlayer(args[0]);

        if (target == null) {
            sendMessage(sender, Config.PLAYER_NOT_FOUND
                    .replace("%player%", args[0])
            );
            return;
        }

        EntityDamageEvent event = new EntityDamageEvent(
                target, EntityDamageEvent.DamageCause.SUICIDE,
                new EnumMap<>(ImmutableMap.of(EntityDamageEvent.DamageModifier.BASE, 100D)),
                new EnumMap<>(ImmutableMap.of(EntityDamageEvent.DamageModifier.BASE, Functions.constant(-0.0D))));

        target.setLastDamageCause(event);
        target.setHealth(0);

        sendMessage(sender, getLanguageConfig().getString("KILL_COMMAND.KILLED_TARGET")
                .replace("%player%", target.getName())
        );
    }
}