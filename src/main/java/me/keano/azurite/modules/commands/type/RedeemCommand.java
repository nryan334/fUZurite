package me.keano.azurite.modules.commands.type;

import lombok.Getter;
import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.commands.Command;
import me.keano.azurite.modules.users.User;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class RedeemCommand extends Command {

    private final Map<String, Redeem> redeems;

    public RedeemCommand(CommandManager manager) {
        super(
                manager,
                "redeem"
        );
        this.redeems = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        this.load();
    }

    private void load() {
        for (String key : getConfig().getConfigurationSection("REDEEMS").getKeys(false)) {
            String path = "REDEEMS." + key + ".";
            String name = getConfig().getString(path + "NAME");
            Redeem redeem = new Redeem(name, path, getConfig().getStringList(path + "COMMANDS"));
            redeems.put(name, redeem);

            if (!getMiscConfig().contains(redeem.getName() + "_REDEEM")) {
                getMiscConfig().set(redeem.getName() + "_REDEEM", redeem.getPath() + ", " + 0);
                getMiscConfig().save();
            }
        }
    }

    @Override
    public List<String> aliases() {
        return Collections.emptyList();
    }

    @Override
    public List<String> usage() {
        return getLanguageConfig().getStringList("REDEEM_COMMAND.USAGE");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sendMessage(sender, Config.PLAYER_ONLY);
            return;
        }

        if (args.length == 0) {
            sendUsage(sender);
            return;
        }

        Player player = (Player) sender;
        User user = getInstance().getUserManager().getByUUID(player.getUniqueId());
        Redeem redeem = redeems.get(args[0]);

        if (user.isRedeemed()) {
            sendMessage(sender, getLanguageConfig().getString("REDEEM_COMMAND.ALREADY_REDEEMED"));
            return;
        }

        if (redeem == null) {
            sendMessage(sender, getLanguageConfig().getString("REDEEM_COMMAND.PARTNER_NOT_FOUND"));
            return;
        }

        user.setRedeemed(true);
        user.save();

        int cur = Integer.parseInt(getMiscConfig().getString(redeem.getName() + "_REDEEM").split(", ")[1]);
        getMiscConfig().set(redeem.getName() + "_REDEEM", redeem.getPath() + ", " + (cur + 1));
        getMiscConfig().save();

        for (String s : getLanguageConfig().getStringList("REDEEM_COMMAND.REDEEMED_BROADCAST")) {
            Bukkit.broadcastMessage(s
                    .replace("%player%", player.getName())
                    .replace("%partner%", redeem.getName())
            );
        }

        List<Command> toRun = calcRewards(redeem);

        for (Command command : toRun) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.getCommand()
                    .replace("%player%", player.getName())
            );
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        String string = args[args.length - 1];
        return redeems.values()
                .stream()
                .map(Redeem::getName)
                .filter(s -> s.regionMatches(true, 0, string, 0, string.length()))
                .collect(Collectors.toList());
    }

    private List<Command> calcRewards(Redeem redeem) {
        boolean runAll = true;

        for (Command command : redeem.getCommands()) {
            if (command.getChance() != 100) {
                runAll = false;
                break;
            }
        }

        if (runAll) {
            return redeem.getCommands();
        }

        List<Command> commands = new ArrayList<>();

        for (Command command : redeem.getCommands()) {
            if (command.isAlways()) {
                commands.add(command);
                continue;
            }

            for (int i = 0; i < command.getChance(); i++) {
                commands.add(command);
            }
        }

        int random = ThreadLocalRandom.current().nextInt(commands.size());
        return Collections.singletonList(commands.get(random));
    }

    @Getter
    private static class Redeem {

        private final String name;
        private final String path;
        private final List<RedeemCommand.Command> commands;

        public Redeem(String name, String path, List<String> commands) {
            this.name = name;
            this.path = path;
            this.commands = commands.stream().map(s -> {
                String[] split = s.split(", ");
                return new RedeemCommand.Command(split[0], Double.parseDouble(split[1].replace("%", "")));
            }).collect(Collectors.toList());
        }
    }

    @Getter
    private static class Command {

        private final String command;
        private final double chance;
        private final boolean always;

        public Command(String command, double chance) {
            this.command = command;
            this.chance = chance;
            this.always = chance <= 0;
        }
    }
}